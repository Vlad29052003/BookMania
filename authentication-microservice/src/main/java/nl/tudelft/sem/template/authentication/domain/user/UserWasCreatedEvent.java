package nl.tudelft.sem.template.authentication.domain.user;

/**
 * A DDD domain event that indicated a user was created.
 */
public class UserWasCreatedEvent {
    private final transient Username username;

    public UserWasCreatedEvent(Username username) {
        this.username = username;
    }

    public Username getNetId() {
        return this.username;
    }
}
