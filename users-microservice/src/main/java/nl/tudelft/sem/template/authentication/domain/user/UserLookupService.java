package nl.tudelft.sem.template.authentication.domain.user;

import java.util.ArrayList;
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
public class UserLookupService {
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
                .stream().filter(user -> !user.isDeactivated() && user.getUsername().toString().contains(name))
                .map(u -> new UserModel(u.getUsername().toString(), u.getEmail(),
                        u.getName(), u.getBio(), u.getLocation(),
                        u.getFavouriteGenres(), u.getFavouriteBook()))
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
                .stream().filter(user -> !user.isDeactivated() && user.getFavouriteBook() != null)
                .collect(Collectors.toList());

        if (users.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No users found!");
        }

        boolean bookExists = users
                .stream().anyMatch(user -> user.getFavouriteBook().getId().equals(bookId));

        if (!bookExists) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No users with these favourite book found!");
        }

        List<UserModel> res = users
                .stream().filter(user -> bookExists)
                .map(u -> new UserModel(u.getUsername().toString(), u.getEmail(),
                        u.getName(), u.getBio(), u.getLocation(),
                        u.getFavouriteGenres(), u.getFavouriteBook()))
                .collect(Collectors.toList());

        return res;
    }

    /**
     * Get users by favourite genre.
     *
     * @param genre genre of the favourite book
     * @return users matching favourite genre that are not deactivated/banned
     */
    public List<UserModel> getUsersByFavouriteGenres(List<Genre> genre) {
        List<UserModel> res = new ArrayList<>();

        List<AppUser> users = userRepository.findAll()
                .stream().filter(user -> !user.isDeactivated() && user.getFavouriteGenres() != null
                        && !user.getFavouriteGenres().isEmpty())
                .collect(Collectors.toList());

        if (users.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No users found!");
        }

        for(AppUser user : users) {
            for(Genre g : user.getFavouriteGenres()) {
                if(genre.contains(g)) {
                    res.add(new UserModel(user.getUsername().toString(), user.getEmail(),
                            user.getName(), user.getBio(), user.getLocation(),
                            user.getFavouriteGenres(), user.getFavouriteBook()));
                    break;
                }
            }
        }

        return res;
    }


}

