package nl.tudelft.sem.template.authentication.domain.user;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * A DDD value object representing a hashed password in our domain.
 */
@Getter
@EqualsAndHashCode
public class HashedPassword {
    private final String hash;

    public HashedPassword(String hash) {
        // Validate input
        this.hash = hash;
    }

    @Override
    public String toString() {
        return hash;
    }
}
