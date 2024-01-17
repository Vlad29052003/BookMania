package nl.tudelft.sem.template.authentication.domain.rolechange;

import java.util.List;
import java.util.Optional;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import nl.tudelft.sem.template.authentication.domain.user.Username;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RoleChangeService {

    private final transient RoleChangeRepository roleChangeRepository;

    private final transient UserRepository userRepository;

    /**
     * Create a RoleChangeService.
     *
     * @param roleChangeRepository role change repository.
     * @param userRepository user repository.
     */
    @Autowired
    public RoleChangeService(RoleChangeRepository roleChangeRepository,
                                    UserRepository userRepository) {
        this.roleChangeRepository = roleChangeRepository;
        this.userRepository = userRepository;
    }

    /**
     * Retrieves all requests from the DB if called by an admin.
     *
     * @param authority user authority.
     * @return a list of all requests.
     * @throws ResponseStatusException if the user does not have admin authority.
     */
    public List<RoleChange> getAll(String authority) throws ResponseStatusException {
        if (!authority.equals(Authority.ADMIN.toString())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Only admins can access role change requests!");
        }
        return roleChangeRepository.findAll();
    }

    /**
     * Adds a request to the DB.
     *
     * @param request role change request.
     * @throws ResponseStatusException if the user does not exist or parameters are not present.
     */
    public void addRequest(Username username, RoleChange request) throws ResponseStatusException {
        Optional<AppUser> optionalUser = userRepository.findByUsername(username);

        if (optionalUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User does not exist!");
        }
        if (request == null || request.getNewRole() == null || request.getSsn() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Illegal parameters!");
        }
        if (!optionalUser.get().getId().equals(request.getId())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You cannot submit requests for other users!");
        }

        roleChangeRepository.saveAndFlush(request);
    }
}
