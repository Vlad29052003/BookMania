package nl.tudelft.sem.template.authentication.domain.user;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
public class UserEntityTests {
    private transient AppUser user1;
    private transient AppUser user2;
    private transient AppUser user3;

    /**
     * Sets up the testing environment.
     */
    @BeforeEach
    public void setUp() {
        this.user1 = new AppUser(new Username("user1"), "email1@gmail.com", new HashedPassword("hash"));
        this.user2 = new AppUser(new Username("user2"), "email2@gmail.com", new HashedPassword("hash"));
        this.user3 = new AppUser(new Username("user3"), "email3@gmail.com", new HashedPassword("hash"));

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
        assertThat(test).isNotEqualTo(null);
    }

    @Test
    public void testConstructor() {
        assertThat(user1).isNotEqualTo(null);
        assertThat(user1.getUsername()).isEqualTo(new Username("user1"));
        assertThat(user1.getEmail()).isEqualTo("email1@gmail.com");
        assertThat(user1.getPassword()).isEqualTo(new HashedPassword("hash"));
        assertThat(user1.getFavouriteGenres()).isEqualTo(new ArrayList<>());
        assertThat(user1.getFollows()).isEqualTo(new ArrayList<>());
        assertThat(user1.getFollowedBy()).isEqualTo(new ArrayList<>());
        assertThat(user1.getAuthority()).isEqualTo(Authority.REGULAR_USER);
        assertThat(user1.isDeactivated()).isFalse();
    }

    @Test
    public void testSetEmail() {
        assertThat(user1.getEmail()).isEqualTo("email1@gmail.com");
        assertThatThrownBy(() -> user1.setEmail("badEmail.com")).isInstanceOf(IllegalArgumentException.class);
        user1.setEmail("example@email.com");
        assertThat(user1.getEmail()).isEqualTo("example@email.com");
    }

    @Test
    public void testEquals() {
        assertThat(user1).isEqualTo(user1);
        assertThat(user1).isEqualTo(user2);
        assertThat(user1).isNotEqualTo(user3);
        assertThat(user1).isNotEqualTo(null);
        assertThat(user1).isNotEqualTo(new ArrayList<>());
    }

    @Test
    public void testSetter() {
        user1.setFollowedBy(List.of(user2));
        user1.setFollows(List.of(user3));
        assertThat(user1.getFollows()).isEqualTo(List.of(user3));
        assertThat(user1.getFollowedBy()).isEqualTo(List.of(user2));
    }

    @Test
    public void testHash() {
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
        assertThat(user1.hashCode()).isEqualTo(Objects.hash(user1.getId()));
    }

    @Test
    public void testToString() {
        assertThat(user1.toString()).isNotEqualTo(user2.toString());
        assertThat(user1.toString()).isEqualTo(user1.toString());
    }

    @Test
    public void testRecordUserWasCreated() {
        user1.recordUserWasCreated();
        assertThat(user1.getDomainEventsSize()).isEqualTo(1);
    }


    @Test
    public void testFollow() {
        assertThat(user1.getFollows()).isEqualTo(new ArrayList<>());
        user1.follow(user2);
        assertThat(user1.getFollows()).isEqualTo(List.of(user2));
    }

    @Test
    public void testUnfollow() {
        assertThat(user1.getFollows()).isEqualTo(new ArrayList<>());
        user1.follow(user2);
        assertThat(user1.getFollows()).isEqualTo(List.of(user2));
        user1.unfollow(user2);
        assertThat(user1.getFollows()).isEqualTo(new ArrayList<>());
    }

    @Test
    public void testHashedPassword() {
        HashedPassword hashedPassword = new HashedPassword("hash");

        assertThat(hashedPassword.toString()).isEqualTo("hash");
    }
}
