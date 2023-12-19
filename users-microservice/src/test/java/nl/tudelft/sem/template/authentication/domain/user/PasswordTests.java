package nl.tudelft.sem.template.authentication.domain.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class PasswordTests {
    @Test
    public void testConstructor() {
        assertThrows(IllegalArgumentException.class, () -> new Password(null));
        assertThrows(IllegalArgumentException.class, () -> new Password("badPassword"));
        Password password = new Password("ExamplePassword123!");
        assertNotNull(password);
    }

    @Test
    public void testToString() {
        Password password = new Password("ExamplePassword123!");
        assertEquals("ExamplePassword123!", password.toString());
    }
}
