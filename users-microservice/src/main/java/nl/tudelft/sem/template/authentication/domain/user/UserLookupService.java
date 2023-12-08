package nl.tudelft.sem.template.authentication.domain.user;

import java.util.List;
import java.util.stream.Collectors;
import nl.tudelft.sem.template.authentication.models.UserModel;
import org.springframework.stereotype.Service;


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


}

