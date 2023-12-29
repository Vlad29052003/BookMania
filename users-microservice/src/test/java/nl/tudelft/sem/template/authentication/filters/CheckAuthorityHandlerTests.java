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
import nl.tudelft.sem.template.authentication.authentication.JwtService;
import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.models.FilterBookRequestModel;
import nl.tudelft.sem.template.authentication.strategies.Strategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class CheckAuthorityHandlerTests {
    private transient JwtService jwtService;
    private transient CheckAuthorityHandler checkAuthorityHandler;
    private transient Handler next;
    private transient FilterBookRequestModel filterBookRequestModel;

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
        when(strategy.getAllowedAuthorities()).thenReturn(List.of(Authority.AUTHOR, Authority.ADMIN));

        Book book = new Book();
        book.setId(UUID.randomUUID());
        this.filterBookRequestModel = new FilterBookRequestModel(book, "bearer token");
    }

    @Test
    public void testAdmin() {
        when(jwtService.extractAuthorization(any())).thenReturn(Authority.ADMIN);
        checkAuthorityHandler.filter(filterBookRequestModel);
        verify(next, times(1)).filter(filterBookRequestModel);
    }

    @Test
    public void testAuthor() {
        when(jwtService.extractAuthorization(any())).thenReturn(Authority.AUTHOR);
        checkAuthorityHandler.filter(filterBookRequestModel);
        verify(next, times(1)).filter(filterBookRequestModel);
    }

    @Test
    public void testRegularUser() {
        when(jwtService.extractAuthorization(any())).thenReturn(Authority.REGULAR_USER);
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
