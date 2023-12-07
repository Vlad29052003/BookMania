package nl.tudelft.sem.template.authentication.domain.user;

import lombok.Getter;

/**
 * A DDD domain event that indicated a user was created.
 */
@Getter
public class UserWasCreatedEvent {
    private final transient Username username;

    public UserWasCreatedEvent(Username username) {
        this.username = username;
    }

}
