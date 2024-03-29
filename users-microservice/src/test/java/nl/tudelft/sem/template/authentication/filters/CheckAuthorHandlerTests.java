package nl.tudelft.sem.template.authentication.filters;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.domain.user.HashedPassword;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import nl.tudelft.sem.template.authentication.domain.user.Username;
import nl.tudelft.sem.template.authentication.handlers.CheckAuthorHandler;
import nl.tudelft.sem.template.authentication.handlers.Handler;
import nl.tudelft.sem.template.authentication.models.FilterBookRequestModel;
import nl.tudelft.sem.template.authentication.strategies.Strategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

@ActiveProfiles("test")
public class CheckAuthorHandlerTests {
    private transient CheckAuthorHandler checkAuthorHandler;
    private transient Handler next;
    private transient Strategy strategy;
    private transient AppUser user;
    private transient Book book;

    /**
     * Sets up the testing environment.
     */
    @BeforeEach
    public void setUp() {
        UserRepository userRepository = mock(UserRepository.class);
        this.next = mock(Handler.class);
        this.strategy = mock(Strategy.class);
        this.checkAuthorHandler = new CheckAuthorHandler(userRepository);
        checkAuthorHandler.setNext(next);
        checkAuthorHandler.setStrategy(strategy);
        when(strategy.getUnauthorizedErrorMessage()).thenReturn("testMessage");

        Username username = new Username("user");
        this.user = new AppUser(username, "email@email.com", new HashedPassword("hash"));
        this.user.setName("author");

        this.book = new Book();
        this.book.setAuthors(List.of("author1", "author"));
        this.book.setId(UUID.randomUUID());

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
    }


    @Test
    public void testAdmin() {
        FilterBookRequestModel  filterBookRequestModel =
                new FilterBookRequestModel(new Username("user"), Authority.ADMIN, book);
        user.setAuthority(Authority.ADMIN);
        book.setAuthors(List.of("a"));
        checkAuthorHandler.filter(filterBookRequestModel);

        verify(strategy, times(1)).passToService(filterBookRequestModel);
        verify(next, times(0)).filter(filterBookRequestModel);
    }

    @Test
    public void testAuthor() {
        FilterBookRequestModel  filterBookRequestModel =
                new FilterBookRequestModel(new Username("user"), Authority.AUTHOR, book);
        user.setAuthority(Authority.AUTHOR);
        checkAuthorHandler.filter(filterBookRequestModel);

        verify(strategy, times(1)).passToService(filterBookRequestModel);
        verify(next, times(0)).filter(filterBookRequestModel);
    }

    @Test
    public void testNotAuthorOfBook() {
        FilterBookRequestModel  filterBookRequestModel =
                new FilterBookRequestModel(new Username("user"), Authority.AUTHOR, book);
        user.setAuthority(Authority.AUTHOR);
        book.setAuthors(List.of("other author"));

        assertThatThrownBy(() -> checkAuthorHandler.filter(filterBookRequestModel))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("401 UNAUTHORIZED")
                .satisfies(ex -> {
                    assertThat(((ResponseStatusException) ex).getStatus())
                            .isEqualTo(HttpStatus.UNAUTHORIZED);
                });

        verify(strategy, times(0)).passToService(filterBookRequestModel);
        verify(next, times(0)).filter(any());
    }
}
