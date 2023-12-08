package nl.tudelft.sem.template.authentication.domain.user;

import java.io.Serial;

/**
 * Exception to indicate the NetID is already in use.
 */
public class UsernameAlreadyInUseException extends Exception {
    @Serial
    private static final long serialVersionUID = -3387516993124229948L;
    
    public UsernameAlreadyInUseException(Username username) {
        super(username + " username is already in use!");
    }
}
