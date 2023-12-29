package nl.tudelft.sem.template.authentication.filters;

import nl.tudelft.sem.template.authentication.authentication.JwtService;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.models.FilterBookRequestModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class CheckAuthorityHandler extends AbstractHandler {
    private final transient JwtService jwtService;

    /**
     * Creates a new CheckAuthorityHandler object.
     *
     * @param jwtService is the JwtService.
     */
    public CheckAuthorityHandler(JwtService jwtService) {
        super();
        this.jwtService = jwtService;
    }

    @Override
    public void filter(FilterBookRequestModel filterBookRequestModel) {
        Authority authority = jwtService.extractAuthorization(filterBookRequestModel.getBearerToken());
        if (this.getStrategy().getAllowedAuthorities().contains(authority)) {
            super.getHandler().filter(filterBookRequestModel);
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, super.getStrategy().getUnauthorizedErrorMessage());
        }
    }
}
