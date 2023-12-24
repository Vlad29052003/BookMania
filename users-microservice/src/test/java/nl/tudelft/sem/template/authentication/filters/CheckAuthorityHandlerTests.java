package nl.tudelft.sem.template.authentication.filters;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import nl.tudelft.sem.template.authentication.authentication.JwtService;
import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.strategy.Strategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class CheckAuthorityHandlerTests {
    private transient JwtService jwtService;
    private transient CheckAuthorityHandler checkAuthorityHandler;
    private transient Handler next;

    /**
     * Sets up the testing environment.
     */
    @BeforeEach
    public void setUp() {
        this.jwtService = mock(JwtService.class);
        this.next = mock(Handler.class);
        Strategy strategy = mock(Strategy.class);
        this.checkAuthorityHandler = new CheckAuthorityHandler(jwtService);
        checkAuthorityHandler.setNext(next);
        checkAuthorityHandler.setStrategy(strategy);
        when(strategy.getUnauthorizedErrorMessage()).thenReturn("testMessage");
    }

    @Test
    public void testAdmin() {
        Book book = new Book();
        book.setId(UUID.randomUUID());
        String token = "token";
        when(jwtService.extractAuthorization(any())).thenReturn(Authority.ADMIN);
        checkAuthorityHandler.filter(book, token);
        verify(next, times(1)).filter(book, token);
    }

    @Test
    public void testAuthor() {
        Book book = new Book();
        book.setId(UUID.randomUUID());
        String token = "token";
        when(jwtService.extractAuthorization(any())).thenReturn(Authority.AUTHOR);
        checkAuthorityHandler.filter(book, token);
        verify(next, times(1)).filter(book, token);
    }

    @Test
    public void testRegularUser() {
        Book book = new Book();
        book.setId(UUID.randomUUID());
        String token = "token";
        when(jwtService.extractAuthorization(any())).thenReturn(Authority.REGULAR_USER);
        assertThatThrownBy(() -> checkAuthorityHandler.filter(book, token))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("401 UNAUTHORIZED \"testMessage\"")
                .satisfies(ex -> {
                    assertThat(((ResponseStatusException) ex).getStatus())
                            .isEqualTo(HttpStatus.UNAUTHORIZED);
                });

        verify(next, times(0)).filter(any(), any());
    }
}
