package nl.tudelft.sem.template.authentication.filters;

import nl.tudelft.sem.template.authentication.authentication.JwtService;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import nl.tudelft.sem.template.authentication.domain.user.Username;
import nl.tudelft.sem.template.authentication.models.FilterBookRequestModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class CheckAuthorHandler extends AbstractHandler {
    private final transient JwtService jwtService;
    private final transient UserRepository userRepository;

    /**
     * Creates a new AbstractHandler object.
     *
     * @param jwtService is the JwtService.
     * @param userRepository is the UserRepository.
     */
    public CheckAuthorHandler(JwtService jwtService, UserRepository userRepository) {
        super();
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    public void filter(FilterBookRequestModel filterBookRequestModel) {
        AppUser appUser = userRepository
                .findByUsername(new Username(jwtService.extractUsername(filterBookRequestModel.getBearerToken()))).get();
        boolean isAuthorOfBook = appUser.getAuthority().equals(Authority.ADMIN)
                || filterBookRequestModel.getBook().getAuthors().contains(appUser.getName());
        if (!isAuthorOfBook) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, super.getStrategy().getNotAuthorErrorMessage());
        } else {
            super.getStrategy().passToService(filterBookRequestModel.getBook());
        }
    }
}
