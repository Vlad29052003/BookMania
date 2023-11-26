package nl.tudelft.sem.template.authentication.domain.user;

import lombok.EqualsAndHashCode;

/**
 * A DDD value object representing a NetID in our domain.
 */
@EqualsAndHashCode
public class NetId {
    private final transient String netIdValue;

    /**
     * NetId constructor.
     *
     * @param netId the username
     */
    public NetId(String netId) {
        if (netId == null || !netId.matches("^[a-zA-z][a-zA-Z0-9]*$")) {
            throw new IllegalArgumentException("Illegal username:"
                    + " the username should start with a letter and not contain any special characters!");
        }
        this.netIdValue = netId;
    }

    @Override
    public String toString() {
        return netIdValue;
    }
}
