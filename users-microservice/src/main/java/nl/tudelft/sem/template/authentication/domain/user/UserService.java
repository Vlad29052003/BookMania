package nl.tudelft.sem.template.authentication.domain.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import nl.tudelft.sem.template.authentication.authentication.JwtService;
import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.book.BookRepository;
import nl.tudelft.sem.template.authentication.domain.book.Genre;
import nl.tudelft.sem.template.authentication.domain.report.ReportRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * A DDD service for a user.
 */
@Service
public class UserService {
    private final transient UserRepository userRepository;
    private final transient ReportRepository reportRepository;
    private final transient BookRepository bookRepository;
    public final transient JwtService jwtService;

    public static final String NO_SUCH_USER = "User does not exist!";

    /**
     * Instantiates a new UserService.
     *
     * @param userRepository the user repository
     */
    public UserService(UserRepository userRepository,
                       ReportRepository reportRepository,
                       BookRepository bookRepository,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.reportRepository = reportRepository;
        this.bookRepository = bookRepository;
        this.jwtService = jwtService;
    }

    /**
     * Get the details of a user.
     *
     * @param username the username
     * @return an AppUser containing all the information
     */
    public AppUser getUserByNetId(Username username) {
        Optional<AppUser> optionalAppUser = userRepository.findByUsername(username);
        if (optionalAppUser.isEmpty()) {
            throw new UsernameNotFoundException(NO_SUCH_USER);
        }

        return optionalAppUser.get();
    }

    /**
     * Update the name of an existing user.
     *
     * @param username the username
     * @param name     the name of the user
     * @throws UsernameNotFoundException if the given username doesn't exist
     */
    public void updateName(Username username, String name) throws UsernameNotFoundException {
        Optional<AppUser> optionalAppUser = userRepository.findByUsername(username);
        if (optionalAppUser.isEmpty()) {
            throw new UsernameNotFoundException(NO_SUCH_USER);
        }

        AppUser user = optionalAppUser.get();
        user.setName(name);

        userRepository.saveAndFlush(user);
    }

    /**
     * Update the bio of an existing user.
     *
     * @param username the username
     * @param bio      the bio of the user
     * @throws UsernameNotFoundException if the given username doesn't exist
     */
    public void updateBio(Username username, String bio) throws UsernameNotFoundException {
        Optional<AppUser> optionalAppUser = userRepository.findByUsername(username);
        if (optionalAppUser.isEmpty()) {
            throw new UsernameNotFoundException(NO_SUCH_USER);
        }

        AppUser user = optionalAppUser.get();
        user.setBio(bio);

        userRepository.saveAndFlush(user);
    }

    /**
     * Update the profile picture of an existing user.
     *
     * @param username the username
     * @param picture  the profile photo of the user
     * @throws UsernameNotFoundException if the given username doesn't exist
     */
    public void updatePicture(Username username, byte[] picture) throws UsernameNotFoundException {
        Optional<AppUser> optionalAppUser = userRepository.findByUsername(username);
        if (optionalAppUser.isEmpty()) {
            throw new UsernameNotFoundException(NO_SUCH_USER);
        }

        AppUser user = optionalAppUser.get();
        user.setPicture(picture);

        userRepository.saveAndFlush(user);
    }

    /**
     * Update the location of an existing user.
     *
     * @param username the username
     * @param location the location of the user
     * @throws UsernameNotFoundException if the given username doesn't exist
     */
    public void updateLocation(Username username, String location) throws UsernameNotFoundException {
        Optional<AppUser> optionalAppUser = userRepository.findByUsername(username);
        if (optionalAppUser.isEmpty()) {
            throw new UsernameNotFoundException(NO_SUCH_USER);
        }

        AppUser user = optionalAppUser.get();
        user.setLocation(location);

        userRepository.saveAndFlush(user);
    }

    /**
     * Update the list of favourite genres of an existing user.
     *
     * @param username        the username
     * @param favouriteGenres the list of favourite genres of the user
     * @throws UsernameNotFoundException if the given username doesn't exist
     */
    public void updateFavouriteGenres(Username username, List<Genre> favouriteGenres) throws UsernameNotFoundException {
        Optional<AppUser> optionalAppUser = userRepository.findByUsername(username);
        if (optionalAppUser.isEmpty()) {
            throw new UsernameNotFoundException(NO_SUCH_USER);
        }

        AppUser user = optionalAppUser.get();
        user.setFavouriteGenres(favouriteGenres);

        userRepository.saveAndFlush(user);
    }

    /**
     * Update the favourite book of an existing user.
     *
     * @param username        the username
     * @param favouriteBookId the id of the favourite book
     * @throws UsernameNotFoundException if the given username doesn't exist
     */
    public void updateFavouriteBook(Username username, String favouriteBookId) throws UsernameNotFoundException {
        Optional<AppUser> optionalAppUser = userRepository.findByUsername(username);
        if (optionalAppUser.isEmpty()) {
            throw new UsernameNotFoundException(NO_SUCH_USER);
        }

        Optional<Book> optionalBook = bookRepository.findById(UUID.fromString(favouriteBookId));
        if (optionalBook.isEmpty()) {
            throw new IllegalArgumentException("The book with the given ID does not exist!");
        }

        AppUser user = optionalAppUser.get();
        Book book = optionalBook.get();

        user.setFavouriteBook(book);
        userRepository.saveAndFlush(user);
    }

    /**
     * (Un)bans a user.
     *
     * @param username    username of the user that should be (un)banned.
     * @param bearerToken jwt token.
     */
    @Transactional
    public void updateBannedStatus(Username username, String bearerToken) {
        if (!getAuthority(bearerToken).equals(Authority.ADMIN) || expired(bearerToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Only admins can ban / unban a user!");
        }

        Optional<AppUser> optionalAppUser = userRepository.findByUsername(username);
        if (optionalAppUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User does not exist!");
        }

        AppUser user = optionalAppUser.get();
        if (user.isDeactivated()) {
            user.setDeactivated(false);
        } else {
            user.setDeactivated(true);
            reportRepository.deleteByUserId(user.getId());
        }

        userRepository.saveAndFlush(user);
    }

    /**
     * Delete a user.
     *
     * @param username the username
     * @throws UsernameNotFoundException if the given username doesn't exist
     */
    public void delete(Username username) {
        Optional<AppUser> optionalAppUser = userRepository.findByUsername(username);
        if (optionalAppUser.isEmpty()) {
            throw new UsernameNotFoundException(NO_SUCH_USER);
        }

        AppUser user = optionalAppUser.get();

        userRepository.delete(user);
    }

    private Authority getAuthority(String bearerToken) {
        return jwtService.extractAuthorization(bearerToken.substring(7));
    }

    private boolean expired(String bearerToken) {
        return jwtService.isTokenExpired(bearerToken);
    }
}
