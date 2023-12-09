package nl.tudelft.sem.template.authentication.domain.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.ArrayList;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserEntityTests {
    private transient AppUser user1;
    private transient AppUser user2;
    private transient AppUser user3;

    /**
     * Sets up the testing environment.
     */
    @BeforeEach
    public void setUp() {
        this.user1 = new AppUser(new Username("user1"), "email1", new HashedPassword("hash"));
        this.user2 = new AppUser(new Username("user2"), "email2", new HashedPassword("hash"));
        this.user3 = new AppUser(new Username("user3"), "email3", new HashedPassword("hash"));

        UUID id1 = UUID.randomUUID();
        UUID id3 = UUID.randomUUID();
        while (id3.equals(id1)) {
            id3 = UUID.randomUUID();
        }

        this.user1.setId(id1);
        this.user2.setId(id1);
        this.user3.setId(id3);
    }

    @Test
    public void testEmptyConstructor() {
        AppUser test  = new AppUser();
        assertNotEquals(test, null);
    }

    @Test
    public void testConstructor() {
        assertNotEquals(user1, null);
        assertEquals(new Username("user1"), user1.getUsername());
        assertEquals("email1", user1.getEmail());
        assertEquals(new HashedPassword("hash"), user1.getPassword());
        assertEquals(new ArrayList<>(), user1.getFavouriteGenres());
        assertEquals(new ArrayList<>(), user1.getFollows());
        assertEquals(new ArrayList<>(), user1.getFollowedBy());
        assertEquals(Authority.REGULAR_USER, user1.getAuthority());
        assertFalse(user1.isDeactivated());
    }

    @Test
    public void testEquals() {
        assertEquals(user1, user1);
        assertEquals(user1, user2);
        assertNotEquals(user1, user3);
        assertNotEquals(user1, null);
        assertFalse(user1.equals(new ArrayList<>()));
    }

    @Test
    public void testHash() {
        assertEquals(user1.hashCode(), user2.hashCode());
    }
}
