package nl.tudelft.sem.template.authentication.domain.user;

/**
 * Exception to indicate there is no user having this id.
 */
public class EmailNotFoundException extends Exception {
    static final long serialVersionUID = -3387516993124229948L;

    public EmailNotFoundException(String email) {
        super(email);
    }
}
