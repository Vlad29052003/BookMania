package nl.tudelft.sem.template.authentication.filters;

import static nl.tudelft.sem.template.authentication.domain.user.UserService.NO_SUCH_USER;

import java.util.Optional;
import nl.tudelft.sem.template.authentication.authentication.JwtService;
import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import nl.tudelft.sem.template.authentication.domain.user.Username;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class CheckUserExistenceHandler extends AbstractHandler {
    private final transient JwtService jwtService;
    private final transient UserRepository userRepository;

    /**
     * Creates a new CheckUserExistenceHandler object.
     *
     * @param jwtService is the JwtService.
     * @param userRepository is the UserRepository.
     */
    public CheckUserExistenceHandler(JwtService jwtService, UserRepository userRepository) {
        super();
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    public void filter(Book book, String bearerToken) {
        Optional<AppUser> optUser = userRepository.findByUsername(new Username(jwtService.extractUsername(bearerToken)));
        if (optUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, NO_SUCH_USER);
        }
        super.getHandler().filter(book, bearerToken);
    }
}
