package nl.tudelft.sem.template.authentication.domain.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import nl.tudelft.sem.template.authentication.application.user.UserEventsListener;
import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.book.BookRepository;
import nl.tudelft.sem.template.authentication.domain.book.Genre;
import nl.tudelft.sem.template.authentication.domain.report.ReportRepository;
import nl.tudelft.sem.template.authentication.models.UserModel;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
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

    private final transient UserEventsListener userEventsListener;
    public static final String NO_SUCH_USER = "User does not exist!";

    /**
     * Instantiates a new UserService.
     *
     * @param userRepository   the user repository.
     * @param reportRepository the report repository.
     * @param bookRepository   the book repository.
     */
    public UserService(UserRepository userRepository,
                       ReportRepository reportRepository,
                       BookRepository bookRepository, UserEventsListener userEventsListener) {
        this.userRepository = userRepository;
        this.reportRepository = reportRepository;
        this.bookRepository = bookRepository;
        this.userEventsListener = userEventsListener;
    }

    /**
     * Get the details of a user.
     *
     * @param username the username
     * @return an AppUser containing all the information
     */
    public AppUser getUserByUsername(Username username) {
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
     * @param username  username of the user that should be (un)banned.
     * @param toBan     (de-)activated status we wish the user to have after the request.
     * @param authority jwt token.
     */
    @Transactional
    public void updateBannedStatus(Username username, boolean toBan, String authority) {
        if (!authority.equals(Authority.ADMIN.toString())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Only admins can ban / unban a user!");
        }

        Optional<AppUser> optionalAppUser = userRepository.findByUsername(username);
        if (optionalAppUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User does not exist!");
        }

        AppUser user = optionalAppUser.get();
        if (user.isDeactivated() == toBan) {
            String banned = toBan ? "banned" : "not banned";
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is already " + banned + "!");
        }

        if (user.isDeactivated()) {
            user.setDeactivated(false);
            userRepository.saveAndFlush(user);
            return;
        }
        user.setDeactivated(true);
        reportRepository.deleteByUserId(user.getId().toString());
        userRepository.saveAndFlush(user);
    }

    /**
     * Update the username of an existing user.
     *
     * @param username    the username
     * @param newUsername the new username
     * @throws UsernameNotFoundException if the given username doesn't exist
     */
    public void updateUsername(Username username, String newUsername)
            throws UsernameNotFoundException, UsernameAlreadyInUseException, IllegalArgumentException {
        Optional<AppUser> optionalAppUser = userRepository.findByUsername(username);
        if (optionalAppUser.isEmpty()) {
            throw new UsernameNotFoundException(NO_SUCH_USER);
        }

        Username nun = new Username(newUsername);
        try {
            AppUser user = optionalAppUser.get();
            user.setUsername(nun);

            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            throw new UsernameAlreadyInUseException(nun);
        }
    }

    /**
     * Update the email of an existing user.
     *
     * @param username the username
     * @param newEmail the new email
     * @throws UsernameNotFoundException if the given username doesn't exist
     */
    public void updateEmail(Username username, String newEmail)
            throws UsernameNotFoundException, IllegalArgumentException, EmailAlreadyInUseException {
        Optional<AppUser> optionalAppUser = userRepository.findByUsername(username);
        if (optionalAppUser.isEmpty()) {
            throw new UsernameNotFoundException(NO_SUCH_USER);
        }

        try {
            AppUser user = optionalAppUser.get();
            user.setEmail(newEmail);
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            throw new EmailAlreadyInUseException(newEmail);
        }
    }

    /**
     * Update the password of an existing user.
     *
     * @param username    the username
     * @param newPassword the new password
     * @throws UsernameNotFoundException if the given username doesn't exist
     */
    public void updatePassword(Username username, HashedPassword newPassword) throws UsernameNotFoundException {
        Optional<AppUser> optionalAppUser = userRepository.findByUsername(username);
        if (optionalAppUser.isEmpty()) {
            throw new UsernameNotFoundException(NO_SUCH_USER);
        }

        AppUser user = optionalAppUser.get();
        user.setPassword(newPassword);
        userRepository.saveAndFlush(user);
    }

    /**
     * Delete a user.
     *
     * @param username the username
     * @throws UsernameNotFoundException if the given username doesn't exist
     */
    public void delete(Username username, Username requesterUsername) {
        Optional<AppUser> optionalAppUser = userRepository.findByUsername(username);
        if (optionalAppUser.isEmpty()) {
            throw new UsernameNotFoundException(NO_SUCH_USER);
        }
        Optional<AppUser> optionalRequester = userRepository.findByUsername(requesterUsername);
        if (optionalRequester.isEmpty()) {
            throw new UsernameNotFoundException("Requester does not exist!");
        }

        AppUser user = optionalAppUser.get();
        AppUser requester = optionalRequester.get();

        userEventsListener.onUserWasDeleted(new UserWasDeletedEvent(user, requester.getId()));
        userRepository.deleteConnectionsByUserId(user.getId().toString());
        userRepository.delete(user);
    }

    /**
     * Updates the privacy settings of a user.
     *
     * @param username  the username
     * @param isPrivate new privacy setting
     * @throws UsernameNotFoundException if the given username doesn't exist
     */
    public void updatePrivacy(Username username, boolean isPrivate) {
        Optional<AppUser> optionalAppUser = userRepository.findByUsername(username);
        if (optionalAppUser.isEmpty()) {
            throw new UsernameNotFoundException(NO_SUCH_USER);
        }

        AppUser user = optionalAppUser.get();
        user.setPrivate(isPrivate);

        userRepository.saveAndFlush(user);
    }

    /**
     * Gets the user details for a specific user.
     *
     * @param userId is the user to get the details for.
     * @return a UserModel object with the details.
     */
    public UserModel getUserDetails(UUID userId) {
        Optional<AppUser> optionalAppUser = userRepository.findById(userId);
        if (optionalAppUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, NO_SUCH_USER);
        }

        AppUser user = optionalAppUser.get();
        return new UserModel(user);
    }


    private Pair<AppUser, AppUser> extractUsersFromUsernames(Username a, Username b) {
        Optional<AppUser> optionalAppUser = userRepository.findByUsername(a);
        Optional<AppUser> optionalAppUser2 = userRepository.findByUsername(b);
        if (optionalAppUser.isEmpty() || optionalAppUser2.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, NO_SUCH_USER);
        }

        AppUser user = optionalAppUser.get();
        AppUser userToFollow = optionalAppUser2.get();
        return Pair.of(user, userToFollow);
    }

    /**
     * Follows a user.
     *
     * @param username the username of the user that wants to follow.
     * @param toFollow the username of the user that should be followed.
     */
    @Transactional
    public void followUser(Username username, Username toFollow) {
        Pair<AppUser, AppUser> users = extractUsersFromUsernames(username, toFollow);
        users.getFirst().follow(users.getSecond());
        userRepository.saveAndFlush(users.getFirst());
        userRepository.saveAndFlush(users.getSecond());
    }

    /**
     * Unfollows a user.
     *
     * @param username   the username of the user that wants to unfollow.
     * @param toUnfollow the username of the user that should be unfollowed.
     */
    @Transactional
    public void unfollowUser(Username username, Username toUnfollow) {
        Pair<AppUser, AppUser> users = extractUsersFromUsernames(username, toUnfollow);
        users.getFirst().unfollow(users.getSecond());
        userRepository.saveAndFlush(users.getFirst());
        userRepository.saveAndFlush(users.getSecond());
    }
}
