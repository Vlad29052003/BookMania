package nl.tudelft.sem.template.authentication.domain.user;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * A DDD value object representing a NetID in our domain.
 */
@EqualsAndHashCode
@Getter
public class Username {
    private final String usernameValue;

    /**
     * Username constructor.
     *
     * @param username the username
     */
    public Username(String username) {
        if (username == null || !username.matches("^[a-zA-z][a-zA-Z0-9]*$")) {
            throw new IllegalArgumentException("Illegal username:"
                    + " the username should start with a letter and not contain any special characters!");
        }
        this.usernameValue = username;
    }

    @Override
    public String toString() {
        return usernameValue;
    }
}
