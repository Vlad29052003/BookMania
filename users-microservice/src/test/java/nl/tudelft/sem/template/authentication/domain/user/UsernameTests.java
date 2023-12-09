package nl.tudelft.sem.template.authentication.domain.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

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
}
