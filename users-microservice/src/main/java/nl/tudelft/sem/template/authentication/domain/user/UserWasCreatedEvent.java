package nl.tudelft.sem.template.authentication.domain.user;

import lombok.Getter;

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
