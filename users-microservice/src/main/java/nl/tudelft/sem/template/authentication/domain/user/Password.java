package nl.tudelft.sem.template.authentication.domain.user;

import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;

/**
 * A DDD value object representing a password in our domain.
 */
@EqualsAndHashCode
public class Password {
    private final transient String passwordValue;

    private static final Pattern pattern =
            Pattern.compile("^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[!@#$%^&*()_+{}\\[\\]:;<>,.?~\\\\-]).{8,}$");

    /**
     * Password constructor.
     *
     * @param password the password text
     */
    public Password(String password) {
        /* Validate input - the password should have:
            - a length of at least 8 characters
            - at least one uppercase letter
            - at least one lowercase letter
            - at least one digit
            - at least one special character
        */
        if (password != null && pattern.matcher(password).matches()) {
            this.passwordValue = password;
        } else {
            throw new IllegalArgumentException("""
        Illegal password: the password should have a length of at least 8 characters and should contain the following:
        - at least one uppercase letter
        - at least one lowercase letter
        - at least one digit
        - at least one special character""");
        }
    }

    @Override
    public String toString() {
        return passwordValue;
    }
}
