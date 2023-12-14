package nl.tudelft.sem.template.authentication.domain.user;

import lombok.Getter;

import java.util.UUID;

/**
 * A DDD domain event that indicated a user was created.
 */
@Getter
public class UserWasCreatedEvent {
    private final transient UUID id;

    public UserWasCreatedEvent(UUID id) {
        this.id = id;
    }

}
