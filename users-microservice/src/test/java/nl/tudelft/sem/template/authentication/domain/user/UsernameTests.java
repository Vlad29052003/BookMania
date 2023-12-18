package nl.tudelft.sem.template.authentication.domain.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class UsernameTests {
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
