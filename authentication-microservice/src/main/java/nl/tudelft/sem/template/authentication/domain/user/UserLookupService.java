package nl.tudelft.sem.template.authentication.domain.user;

import java.util.List;
import java.util.stream.Collectors;
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
     * @return users matching name
     */
    public List<AppUser> getUsersByName(String name) {
        return userRepository.findAll()
                .stream().filter(user -> user.getName().contains(name))
                .collect(Collectors.toList());
    }

}


//    /**
//     * Get user by netId.
//     *
//     * @param id id of user
//     * @return user
//     */
//    public AppUser getUserById(NetId id) {
//
//  return userRepository.findByNetId(id).orElse(null);}


//    /**
//     * Get all users.
//     *
//     * @return all users
//     */
//    public List<AppUser> getAllUsers() {
//        return userRepository.findAll();}
