package nl.tudelft.sem.template.authentication.chainOfResponsibilityTests;

import nl.tudelft.sem.template.authentication.authentication.JwtService;
import nl.tudelft.sem.template.authentication.chainOfResponsibility.CheckAuthorHandler;
import nl.tudelft.sem.template.authentication.chainOfResponsibility.Handler;
import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.domain.user.HashedPassword;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import nl.tudelft.sem.template.authentication.domain.user.Username;
import nl.tudelft.sem.template.authentication.strategy.Strategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CheckAuthorHandlerTests {
    private transient JwtService jwtService;
    private transient UserRepository userRepository;
    private transient CheckAuthorHandler checkAuthorHandler;
    private transient Handler next;
    private transient Strategy strategy;
    private transient AppUser user;
    private transient Username username;
    private transient Book book;
    private transient String token;

    @BeforeEach
    public void setUp() {
        this.jwtService = mock(JwtService.class);
        this.userRepository = mock(UserRepository.class);
        this.next = mock(Handler.class);
        this.strategy = mock(Strategy.class);
        this.checkAuthorHandler = new CheckAuthorHandler(jwtService, userRepository);
        checkAuthorHandler.setNext(next);
        checkAuthorHandler.setStrategy(strategy);
        when(strategy.getUnauthorizedErrorMessage()).thenReturn("testMessage");

        this.username = new Username("user");
        this.user = new AppUser(username, "email@email.com", new HashedPassword("hash"));
        this.user.setName("author");

        this.book = new Book();
        this.book.setAuthors(List.of("author1", "author"));
        this.book.setId(UUID.randomUUID());

        this.token = "token";


        when(jwtService.extractUsername(any())).thenReturn(username.toString());
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
    }


    @Test
    public void testAdmin() {
        user.setAuthority(Authority.ADMIN);
        book.setAuthors(List.of("a"));
        checkAuthorHandler.filter(book, token);

        verify(strategy, times(1)).passToService(book);
        verify(next, times(0)).filter(book, token);
    }

    @Test
    public void testAuthor() {
        user.setAuthority(Authority.AUTHOR);
        checkAuthorHandler.filter(book, token);

        verify(strategy, times(1)).passToService(book);
        verify(next, times(0)).filter(book, token);
    }

    @Test
    public void testNotAuthorOfBook() {
        user.setAuthority(Authority.AUTHOR);
        book.setAuthors(List.of("other author"));

        assertThatThrownBy(() -> checkAuthorHandler.filter(book, token))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("401 UNAUTHORIZED")
                .satisfies(ex -> {
                    assertThat(((ResponseStatusException) ex).getStatus())
                            .isEqualTo(HttpStatus.UNAUTHORIZED);
                });

        verify(strategy, times(0)).passToService(book);
        verify(next, times(0)).filter(any(), any());
    }
}
