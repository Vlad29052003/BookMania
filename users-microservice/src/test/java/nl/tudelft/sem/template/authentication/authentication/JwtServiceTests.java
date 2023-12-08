package nl.tudelft.sem.template.authentication.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.ExpiredJwtException;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import nl.tudelft.sem.template.authentication.domain.providers.TimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

public class JwtServiceTests {
    private transient TimeProvider timeProvider1;
    private transient TimeProvider timeProvider2;
    private transient Instant mockedTime1 = Instant.parse("2021-12-31T13:25:34.00Z");
    private transient Instant mockedTime2 = Instant.parse("2020-12-31T13:25:34.00Z");
    private transient Instant mockedTime3 = Instant.parse("3000-12-31T13:25:34.00Z");
    JwtTokenGenerator jwtTokenGenerator;
    private final transient String secret = "testSecret123";
    JwtService jwtService;
    private transient UserDetails user;

    /**
     * Sets up the testing environment.
     *
     * @throws NoSuchFieldException if there is no such field
     * @throws IllegalAccessException if there is no access
     */
    @BeforeEach
    public void setup() throws NoSuchFieldException, IllegalAccessException {
        timeProvider1 = mock(TimeProvider.class);
        jwtTokenGenerator = new JwtTokenGenerator(timeProvider1);

        timeProvider2 = mock(TimeProvider.class);
        jwtService = new JwtService(timeProvider2);

        this.injectSecret(secret);
        user = new User("vlad", "someHash", new ArrayList<>());
    }

    @Test
    public void extractUserTest() {
        when(timeProvider1.getCurrentTime()).thenReturn(mockedTime3);
        when(timeProvider2.getCurrentTime()).thenReturn(mockedTime3);
        String token = jwtTokenGenerator.generateToken(user);

        assertEquals(jwtService.extractUsername(token), user.getUsername());
    }

    @Test
    public void generatedTokenNotExpired() {
        when(timeProvider1.getCurrentTime()).thenReturn(mockedTime3);
        when(timeProvider2.getCurrentTime()).thenReturn(mockedTime2);
        String token = jwtTokenGenerator.generateToken(user);

        assertFalse(jwtService.isTokenExpired(token));
    }

    @Test
    public void generatedTokenIsExpired() {
        when(timeProvider1.getCurrentTime()).thenReturn(mockedTime2);
        when(timeProvider2.getCurrentTime()).thenReturn(mockedTime1);
        String token = jwtTokenGenerator.generateToken(user);

        assertThrows(ExpiredJwtException.class, () -> {
            jwtService.isTokenExpired(token);
        });
    }

    private void injectSecret(String secret) throws NoSuchFieldException, IllegalAccessException {
        Field declaredField = jwtTokenGenerator.getClass().getDeclaredField("jwtSecret");
        declaredField.setAccessible(true);
        declaredField.set(jwtTokenGenerator, secret);

        Field declaredField2 = jwtService.getClass().getDeclaredField("jwtSecret");
        declaredField2.setAccessible(true);
        declaredField2.set(jwtService, secret);
    }
}
