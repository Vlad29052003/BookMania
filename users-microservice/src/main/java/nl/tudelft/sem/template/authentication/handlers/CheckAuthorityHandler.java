package nl.tudelft.sem.template.authentication.handlers;

import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import nl.tudelft.sem.template.authentication.models.FilterBookRequestModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class CheckAuthorityHandler extends AbstractHandler {

    /**
     * Creates a new CheckAuthorityHandler object.
     *
     * @param userRepository is the UserRepository.
     */
    public CheckAuthorityHandler(UserRepository userRepository) {
        super(userRepository);
    }

    @Override
    public void filter(FilterBookRequestModel filterBookRequestModel) {
        if (this.getStrategy().getAllowedAuthorities().contains(filterBookRequestModel.getUserAuthority())) {
            super.getHandler().filter(filterBookRequestModel);
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, super.getStrategy().getUnauthorizedErrorMessage());
        }
    }
}
