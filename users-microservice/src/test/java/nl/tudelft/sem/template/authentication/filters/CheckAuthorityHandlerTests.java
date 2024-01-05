package nl.tudelft.sem.template.authentication.filters;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import nl.tudelft.sem.template.authentication.domain.user.Username;
import nl.tudelft.sem.template.authentication.handlers.CheckAuthorityHandler;
import nl.tudelft.sem.template.authentication.handlers.Handler;
import nl.tudelft.sem.template.authentication.models.FilterBookRequestModel;
import nl.tudelft.sem.template.authentication.strategies.Strategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class CheckAuthorityHandlerTests {
    private transient CheckAuthorityHandler checkAuthorityHandler;
    private transient Handler next;
    private transient Book book;

    /**
     * Sets up the testing environment.
     */
    @BeforeEach
    public void setUp() {
        UserRepository userRepository = mock(UserRepository.class);
        this.next = mock(Handler.class);
        Strategy strategy = mock(Strategy.class);
        this.checkAuthorityHandler = new CheckAuthorityHandler(userRepository);
        checkAuthorityHandler.setNext(next);
        checkAuthorityHandler.setStrategy(strategy);
        when(strategy.getUnauthorizedErrorMessage()).thenReturn("testMessage");
        when(strategy.getAllowedAuthorities()).thenReturn(List.of(Authority.AUTHOR, Authority.ADMIN));

        this.book = new Book();
        book.setId(UUID.randomUUID());
    }

    @Test
    public void testAdmin() {
        FilterBookRequestModel  filterBookRequestModel =
                new FilterBookRequestModel(new Username("user"), Authority.ADMIN, book);
        checkAuthorityHandler.filter(filterBookRequestModel);
        verify(next, times(1)).filter(filterBookRequestModel);
    }

    @Test
    public void testAuthor() {
        FilterBookRequestModel  filterBookRequestModel =
                new FilterBookRequestModel(new Username("user"), Authority.AUTHOR, book);
        checkAuthorityHandler.filter(filterBookRequestModel);
        verify(next, times(1)).filter(filterBookRequestModel);
    }

    @Test
    public void testRegularUser() {
        FilterBookRequestModel  filterBookRequestModel =
                new FilterBookRequestModel(new Username("user"), Authority.REGULAR_USER, book);
        assertThatThrownBy(() -> checkAuthorityHandler.filter(filterBookRequestModel))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("401 UNAUTHORIZED \"testMessage\"")
                .satisfies(ex -> {
                    assertThat(((ResponseStatusException) ex).getStatus())
                            .isEqualTo(HttpStatus.UNAUTHORIZED);
                });

        verify(next, times(0)).filter(any());
    }
}
