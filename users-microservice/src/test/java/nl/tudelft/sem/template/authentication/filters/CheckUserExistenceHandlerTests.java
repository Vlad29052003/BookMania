package nl.tudelft.sem.template.authentication.filters;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import nl.tudelft.sem.template.authentication.domain.user.Username;
import nl.tudelft.sem.template.authentication.handlers.CheckUserExistenceHandler;
import nl.tudelft.sem.template.authentication.handlers.Handler;
import nl.tudelft.sem.template.authentication.models.FilterBookRequestModel;
import nl.tudelft.sem.template.authentication.strategies.Strategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

@ActiveProfiles("test")
public class CheckUserExistenceHandlerTests {
    private transient UserRepository userRepository;
    private transient CheckUserExistenceHandler checkUserExistenceHandler;
    private transient Handler next;
    private transient Username username;
    private transient Book book;

    /**
     * Sets up the testing environment.
     */
    @BeforeEach
    public void setUp() {
        this.userRepository = mock(UserRepository.class);
        this.next = mock(Handler.class);
        Strategy strategy = mock(Strategy.class);
        this.checkUserExistenceHandler = new CheckUserExistenceHandler(userRepository);
        checkUserExistenceHandler.setNext(next);
        checkUserExistenceHandler.setStrategy(strategy);
        when(strategy.getUnauthorizedErrorMessage()).thenReturn("testMessage");

        this.username = new Username("user");

        this.book = new Book();
        book.setId(UUID.randomUUID());
    }


    @Test
    public void testExists() {
        FilterBookRequestModel  filterBookRequestModel =
                new FilterBookRequestModel(username, Authority.AUTHOR, book);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(new AppUser()));
        checkUserExistenceHandler.filter(filterBookRequestModel);
        verify(next, times(1)).filter(filterBookRequestModel);
    }

    @Test
    public void testInexistent() {
        FilterBookRequestModel  filterBookRequestModel =
                new FilterBookRequestModel(username, Authority.AUTHOR, book);
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> checkUserExistenceHandler.filter(filterBookRequestModel))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("401 UNAUTHORIZED \"User does not exist!\"")
                .satisfies(ex -> {
                    assertThat(((ResponseStatusException) ex).getStatus())
                            .isEqualTo(HttpStatus.UNAUTHORIZED);
                });

        verify(next, times(0)).filter(any());
    }
}
