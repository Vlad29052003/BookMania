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
import nl.tudelft.sem.template.authentication.authentication.JwtService;
import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import nl.tudelft.sem.template.authentication.domain.user.Username;
import nl.tudelft.sem.template.authentication.strategy.Strategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class CheckUserExistenceHandlerTests {
    private transient JwtService jwtService;
    private transient UserRepository userRepository;
    private transient CheckUserExistenceHandler checkUserExistenceHandler;
    private transient Handler next;
    private transient Strategy strategy;

    /**
     * Sets up the testing environment.
     */
    @BeforeEach
    public void setUp() {
        this.jwtService = mock(JwtService.class);
        this.userRepository = mock(UserRepository.class);
        this.next = mock(Handler.class);
        this.strategy = mock(Strategy.class);
        this.checkUserExistenceHandler = new CheckUserExistenceHandler(jwtService, userRepository);
        checkUserExistenceHandler.setNext(next);
        checkUserExistenceHandler.setStrategy(strategy);
        when(strategy.getUnauthorizedErrorMessage()).thenReturn("testMessage");
    }


    @Test
    public void testExists() {
        Book book = new Book();
        book.setId(UUID.randomUUID());
        String token = "token";
        String username = "user";
        when(jwtService.extractUsername(any())).thenReturn(username);
        when(userRepository.findByUsername(new Username(username))).thenReturn(Optional.of(new AppUser()));
        checkUserExistenceHandler.filter(book, token);
        verify(next, times(1)).filter(book, token);
    }

    @Test
    public void testInexistent() {
        Book book = new Book();
        book.setId(UUID.randomUUID());
        String token = "token";
        String username = "user";
        when(jwtService.extractUsername(any())).thenReturn(username);
        when(userRepository.findByUsername(new Username(username))).thenReturn(Optional.empty());
        assertThatThrownBy(() -> checkUserExistenceHandler.filter(book, token))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("401 UNAUTHORIZED \"User does not exist!\"")
                .satisfies(ex -> {
                    assertThat(((ResponseStatusException) ex).getStatus())
                            .isEqualTo(HttpStatus.UNAUTHORIZED);
                });

        verify(next, times(0)).filter(any(), any());
    }
}
