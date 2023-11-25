package nl.tudelft.sem.template.authentication.controllers;

//import nl.tudelft.sem.template.authentication.authentication.JwtUserDetailsService;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.UserLookupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class UserSearchController {


    //private final transient JwtUserDetailsService jwtUserDetailsService;

    private final transient UserLookupService userLookupService;

    /**
     * Instantiates a new UsersController.
     * //@param jwtUserDetailsService the user service
     * @param lookupService         the user lookup service
     */
    @Autowired
    public UserSearchController(UserLookupService lookupService) {

        //this.jwtUserDetailsService = jwtUserDetailsService;
        this.userLookupService = lookupService;
    }

    /**
     * Endpoint for user searching.
     *
     * @param userName The username query parameter
     * @return the users containing the given username
     * @throws ResponseStatusException if the user does not exist or the password is incorrect
     */
    @GetMapping("/getUser")
    public ResponseEntity<Iterable<AppUser>> getUser(@RequestParam(name = "user") String userName)
            throws ResponseStatusException {
        try {
            return ResponseEntity.ok(userLookupService.getUsersByName(userName));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/getAllUsers")
    public ResponseEntity<Iterable<AppUser>> getAllUsers() {
        try {
            var temp = userLookupService.getAllUsers();
            //.stream().map(AppUser::getName)
            //.collect(Collectors.toUnmodifiableList());
            return ResponseEntity.ok(temp);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
