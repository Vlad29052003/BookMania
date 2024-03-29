package nl.tudelft.sem.template.authentication.controllers;

import java.util.List;
import java.util.UUID;
import nl.tudelft.sem.template.authentication.domain.book.Genre;
import nl.tudelft.sem.template.authentication.domain.user.UserLookupService;
import nl.tudelft.sem.template.authentication.models.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/c")
public class UserSearchController {

    private final transient UserLookupService userLookupService;

    /**
     * Instantiates a new UsersController.
     *
     * @param lookupService the user lookup service
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

    /**
     * Endpoint for user searching by favourite book.
     *
     * @param favouriteBookId The bookId query parameter.
     * @return the users containing the given favourite book
     * @throws ResponseStatusException - If there is no user found
     */
    @GetMapping("/users/favBook")
    public ResponseEntity<?> getUsersByFavouriteBook(@RequestParam UUID favouriteBookId)
            throws ResponseStatusException {
        try {
            Iterable<UserModel> x = userLookupService.getUsersByFavouriteBook(favouriteBookId);
            return ResponseEntity.ok(x);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getMessage(), e.getStatus());
        }
    }

    /**
     * Endpoint for user searching by favourite genres.
     *
     * @param genres - The genres query parameter.
     * @return - The users containing the given favourite genres
     * @throws ResponseStatusException - If there is no user found
     */
    @PostMapping("/users/favGenres")
    public ResponseEntity<?> getUsersByFavouriteGenres(@RequestBody List<Genre> genres)
            throws ResponseStatusException {
        try {
            Iterable<UserModel> x = userLookupService.getUsersByFavouriteGenres(genres);
            return ResponseEntity.ok(x);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getMessage(), e.getStatus());
        }
    }
}

