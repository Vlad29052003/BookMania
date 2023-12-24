package nl.tudelft.sem.template.authentication.filters;

import nl.tudelft.sem.template.authentication.authentication.JwtService;
import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
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
    public void filter(Book book, String bearerToken) {
        Authority authority = jwtService.extractAuthorization(bearerToken);
        if (authority.equals(Authority.AUTHOR) || authority.equals(Authority.ADMIN)) {
            super.getHandler().filter(book, bearerToken);
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, super.getStrategy().getUnauthorizedErrorMessage());
        }
    }
}
