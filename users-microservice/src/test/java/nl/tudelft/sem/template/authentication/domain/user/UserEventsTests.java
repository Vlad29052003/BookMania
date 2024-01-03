package nl.tudelft.sem.template.authentication.domain.user;

import org.h2.engine.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class UserEventsTests {

    @Test
    public void testUserWasCreatedEvent() {
        AppUser user = new AppUser(new Username("username"), "email", new HashedPassword("password"));
        user.setId(UUID.randomUUID());

        UserWasCreatedEvent userWasCreatedEvent = new UserWasCreatedEvent(user);

        AppUser user2 = new AppUser(new Username("user2"), "email.com", new HashedPassword("password"));
        user2.setId(UUID.randomUUID());

        while (user2.getId().equals(user.getId())) {
            user2.setId(UUID.randomUUID());
        }

        UserWasCreatedEvent userWasCreatedEvent2 = new UserWasCreatedEvent(user2);

        assertNotEquals(userWasCreatedEvent, userWasCreatedEvent2);
    }
}
