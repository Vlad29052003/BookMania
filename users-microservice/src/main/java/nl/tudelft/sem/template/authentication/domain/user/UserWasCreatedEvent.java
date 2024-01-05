package nl.tudelft.sem.template.authentication.domain.user;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

/**
 * A DDD domain event that indicated a user was created.
 */
@Getter
public class UserWasCreatedEvent {

    private final transient AppUser user;

    public UserWasCreatedEvent(AppUser user) {
        this.user = user;
    }
}
