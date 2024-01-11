package nl.tudelft.sem.template.authentication.handlers;

import static nl.tudelft.sem.template.authentication.application.Constants.NO_SUCH_USER;

import java.util.Optional;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import nl.tudelft.sem.template.authentication.models.FilterBookRequestModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class CheckUserExistenceHandler extends AbstractHandler {

    /**
     * Creates a new CheckUserExistenceHandler object.
     *
     * @param userRepository is the UserRepository.
     */
    public CheckUserExistenceHandler(UserRepository userRepository) {
        super(userRepository);
    }

    @Override
    public void filter(FilterBookRequestModel filterBookRequestModel) {
        Optional<AppUser> optUser = super.getUserRepository()
                .findByUsername(filterBookRequestModel.getUsername());
        if (optUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, NO_SUCH_USER);
        }
        super.getHandler().filter(filterBookRequestModel);
    }
}
