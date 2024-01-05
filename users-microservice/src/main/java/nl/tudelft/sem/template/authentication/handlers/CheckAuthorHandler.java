package nl.tudelft.sem.template.authentication.handlers;

import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import nl.tudelft.sem.template.authentication.models.FilterBookRequestModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class CheckAuthorHandler extends AbstractHandler {

    /**
     * Creates a new CheckAuthorHandler object.
     *
     * @param userRepository is the UserRepository.
     */
    public CheckAuthorHandler(UserRepository userRepository) {
        super(userRepository);
    }

    @Override
    public void filter(FilterBookRequestModel filterBookRequestModel) {
        AppUser appUser = super.getUserRepository()
                .findByUsername(filterBookRequestModel.getUsername()).get();
        boolean isAuthorOfBook = appUser.getAuthority().equals(Authority.ADMIN)
                || filterBookRequestModel.getBook().getAuthors().contains(appUser.getName());
        if (!isAuthorOfBook) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, super.getStrategy().getNotAuthorErrorMessage());
        } else {
            super.getStrategy().passToService(filterBookRequestModel);
        }
    }
}
