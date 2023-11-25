package nl.tudelft.sem.template.authentication.domain.user;

/**
 * Exception to indicate there is no user having this id.
 */
public class EmailNotFoundException extends Exception{
    public EmailNotFoundException(String email) {
            super(email);
        }
}
