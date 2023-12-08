package nl.tudelft.sem.template.authentication.controllers;

import nl.tudelft.sem.template.authentication.domain.user.UserLookupService;
import nl.tudelft.sem.template.authentication.models.UserModel;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/c")
public class UserSearchController {

    private final transient UserLookupService userLookupService;

    /**
     * Instantiates a new UsersController.
     *
     * @param lookupService         the user lookup service
     */
    @Autowired
    public UserSearchController(UserLookupService lookupService) {

        this.userLookupService = lookupService;
    }

    /**
     * Endpoint for user searching.
     *
     * @param userName The username query parameter
     * @return the users containing the given username
     * @throws ResponseStatusException if there is no user found
     */
    @GetMapping("/users/{user}")
    public ResponseEntity<Iterable<UserModel>> getUser(@PathVariable(name = "user") String userName)
            throws ResponseStatusException {
        try {
            Iterable<UserModel> x = userLookupService.getUsersByName(userName);
            return ResponseEntity.ok(x);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /** Endpoint for user searching by favourite book.
     * @param bookId The bookId query parameter
     * @return the users containing the given favourite book
     * @throws ResponseStatusException if there is no user found
     */
    @GetMapping("/users/{bookId}")
    public ResponseEntity<Iterable<UserModel>> getUsersByFavouriteBook(@PathVariable(name = "bookId") String bookId)
            throws ResponseStatusException {
        try {
            Iterable<UserModel> x = userLookupService.getUsersByFavouriteBook(bookId);
            return ResponseEntity.ok(x);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }


}

