package nl.tudelft.sem.template.authentication.domain.user;

import io.jsonwebtoken.lang.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import nl.tudelft.sem.template.authentication.domain.book.Genre;
import nl.tudelft.sem.template.authentication.models.UserModel;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


/**
 * A DDD service for looking up users.
 */
@Service
public class    UserLookupService {
    private final transient UserRepository userRepository;

    /**
     * Instantiates a new UserLookupService.
     *
     * @param userRepository  the user repository
     */
    public UserLookupService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Get users by name.
     *
     * @param name name of user
     * @return users matching name that are not deactivated/banned
     */
    public List<UserModel> getUsersByName(String name) {
        return userRepository.findAll()
                .stream().filter(
                        user -> (!user.isDeactivated() && !user.isPrivate())
                                && user.getUsername().toString().contains(name))
                .map(UserModel::new)
                .collect(Collectors.toList());
    }

    /**
     * Get users by favourite book.
     *
     * @param bookId id of the favourite book
     * @return users matching favourite book that are not deactivated/banned
     */
    public List<UserModel> getUsersByFavouriteBook(UUID bookId) {
        List<AppUser> users = userRepository.findAll()
                .stream().filter(user -> !user.isDeactivated()
                        && !user.isPrivate() && user.getFavouriteBook() != null)
                .collect(Collectors.toList());

        if (users.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No users found!");
        }

        boolean bookExists = users
                .stream().anyMatch(user -> user.getFavouriteBook().getId().equals(bookId));

        if (!bookExists) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No users with this favourite book found!");
        }

        return users.stream().map(UserModel::new)
                .collect(Collectors.toList());
    }

    /**
     * Get users by favourite genre.
     *
     * @param genres genre of the favourite book
     * @return users matching favourite genre that are not deactivated/banned
     */
    public List<UserModel> getUsersByFavouriteGenres(List<Genre> genres) {
        List<UserModel> users = userRepository.findAll()
                .stream().filter(user -> !user.isDeactivated() && user.getFavouriteGenres() != null
                        && !user.isPrivate() && !user.getFavouriteGenres().isEmpty()
                        && Collections.containsAny(user.getFavouriteGenres(), genres))
                .map(UserModel::new)
                .collect(Collectors.toList());

        if (users.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No users found!");
        }
        return users;
    }


}

