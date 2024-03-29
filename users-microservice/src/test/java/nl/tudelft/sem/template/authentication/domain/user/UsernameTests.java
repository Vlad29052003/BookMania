package nl.tudelft.sem.template.authentication.domain.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
public class UsernameTests {
    @Test
    public void validUsername() {
        Username username = new Username("Gabs23");
        assertEquals(username.toString(), "Gabs23");
    }

    @Test
    public void exceptionUsername() {
        assertThrows(IllegalArgumentException.class, () -> new Username(null));
        assertThrows(IllegalArgumentException.class, () -> new Username("abcAbc#"));
    }

    @Test
    public void testConstructor() {
        assertThrows(IllegalArgumentException.class, () -> new Username(null));
        assertThrows(IllegalArgumentException.class, () -> new Username("1user"));
        Username user = new Username("GoodUsernameExample123");
        assertNotNull(user);
    }

    @Test
    public void testToString() {
        Username user = new Username("GoodUsernameExample123");
        assertEquals("GoodUsernameExample123", user.toString());
    }
}
