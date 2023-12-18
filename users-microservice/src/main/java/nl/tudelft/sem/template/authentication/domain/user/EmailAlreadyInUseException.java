package nl.tudelft.sem.template.authentication.domain.user;

import java.io.Serial;

/**
 * Exception to indicate the email is already in use.
 */
public class EmailAlreadyInUseException extends Exception {
    @Serial
    private static final long serialVersionUID = -6327576881329242235L;

    public EmailAlreadyInUseException(String email) {
        super(email + " email is already in use!");
    }
}
