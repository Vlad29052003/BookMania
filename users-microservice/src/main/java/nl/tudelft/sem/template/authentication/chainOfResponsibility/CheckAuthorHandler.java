package nl.tudelft.sem.template.authentication.chainOfResponsibility;

import nl.tudelft.sem.template.authentication.authentication.JwtService;
import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import nl.tudelft.sem.template.authentication.domain.user.Username;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class CheckAuthorHandler extends AbstractHandler {
    private final transient JwtService jwtService;
    private final transient UserRepository userRepository;

    public CheckAuthorHandler(JwtService jwtService, UserRepository userRepository) {
        super();
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    public void filter(Book book, String bearerToken) {
        AppUser appUser = userRepository.findByUsername(new Username(jwtService.extractUsername(bearerToken))).get();
        boolean isAuthorOfBook = appUser.getAuthority().equals(Authority.ADMIN) || book.getAuthors().contains(appUser.getName());
        if (!isAuthorOfBook) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, super.getStrategy().getNotAuthorErrorMessage());
        } else {
            super.getStrategy().passToService(book);
        }
    }
}
