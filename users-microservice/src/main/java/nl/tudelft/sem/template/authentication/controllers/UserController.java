package nl.tudelft.sem.template.authentication.controllers;

import java.util.List;
import nl.tudelft.sem.template.authentication.domain.book.Genre;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.UserService;
import nl.tudelft.sem.template.authentication.domain.user.Username;
import nl.tudelft.sem.template.authentication.models.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/c/users")
public class UserController {
    private final transient UserService userService;

    /**
     * Instantiates a new UserController.
     *
     * @param userService       the registration service
     */
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Endpoint for getting a user.
     *
     * @return the ResponseEntity containing the most important user information
     */
    @GetMapping
    public ResponseEntity<UserModel> getUserByUsername() {
        Username username = new Username(SecurityContextHolder.getContext().getAuthentication().getName());
        AppUser user = userService.getUserByUsername(username);

        UserModel userModel = new UserModel(user.getUsername().toString(), user.getEmail(), user.getName(), user.getBio(),
                user.getLocation(), user.getFavouriteGenres(), user.getFavouriteBook(), user.isPrivate());

        return ResponseEntity.ok(userModel);
    }

    /**
     * Endpoint for getting the profile picture of a user.
     *
     * @return the ResponseEntity containing the profile photo
     */
    @GetMapping(value = "/picture", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> getUserPictureByUsername() {
        Username username = new Username(SecurityContextHolder.getContext().getAuthentication().getName());
        AppUser user = userService.getUserByUsername(username);

        return ResponseEntity.ok(user.getPicture());
    }

    /**
     * Endpoint for updating a user's name.
     *
     * @param name the new name of the user
     * @return a ResponseEntity containing the OK response
     */
    @PatchMapping("/name")
    public ResponseEntity<Void> updateName(@RequestBody String name) {
        Username username = new Username(SecurityContextHolder.getContext().getAuthentication().getName());
        userService.updateName(username, name);

        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint for updating a user's bio.
     *
     * @param bio         the new bio of the user
     * @return a ResponseEntity containing the OK response
     */
    @PatchMapping("/bio")
    public ResponseEntity<Void> updateBio(@RequestBody String bio) {
        Username username = new Username(SecurityContextHolder.getContext().getAuthentication().getName());
        userService.updateBio(username, bio);

        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint for updating a user's profile picture.
     *
     * @param picture     the new profile photo of the user
     * @return a ResponseEntity containing the OK response
     */
    @PatchMapping(value = "/picture", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Void> updatePicture(@RequestBody byte[] picture) {
        Username username = new Username(SecurityContextHolder.getContext().getAuthentication().getName());
        userService.updatePicture(username, picture);

        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint for updating a user's location.
     *
     * @param location    the new location of the user
     * @return a ResponseEntity containing the OK response
     */
    @PatchMapping("/location")
    public ResponseEntity<Void> updateLocation(@RequestBody String location) {
        Username username = new Username(SecurityContextHolder.getContext().getAuthentication().getName());
        userService.updateLocation(username, location);

        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint for updating the list of favourite genres of a user.
     *
     * @param favouriteGenres the new list of favourite genres of the user
     * @return a ResponseEntity containing the OK response
     */
    @PatchMapping("/favouriteGenres")
    public ResponseEntity<Void> updateFavouriteGenres(@RequestBody List<Genre> favouriteGenres) {
        Username username = new Username(SecurityContextHolder.getContext().getAuthentication().getName());
        userService.updateFavouriteGenres(username, favouriteGenres);

        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint for updating a user's favourite book.
     *
     * @param favouriteBookId the new favourite book of the user
     * @return a ResponseEntity containing the OK response
     */
    @PatchMapping("/favouriteBook")
    public ResponseEntity<Void> updateFavouriteBook(@RequestBody String favouriteBookId) {
        try {
            Username username = new Username(SecurityContextHolder.getContext().getAuthentication().getName());
            userService.updateFavouriteBook(username, favouriteBookId);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }

        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint for deleting a user.
     *
     * @return a ResponseEntity containing the OK response
     */
    @DeleteMapping
    public ResponseEntity<Void> delete() {
        Username username = new Username(SecurityContextHolder.getContext().getAuthentication().getName());
        userService.delete(username);

        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint for updating a user's privacy settings.
     *
     * @param isPrivate new privacy setting
     * @return a ResponseEntity containing the OK response
     */
    @PatchMapping("/isPrivate")
    public ResponseEntity<Void> updatePrivacy(@RequestBody String isPrivate) {
        Username username = new Username(SecurityContextHolder.getContext().getAuthentication().getName());
        userService.updatePrivacy(username, Boolean.parseBoolean(isPrivate));
        //System.out.println(Boolean.parseBoolean(isPrivate));

        return ResponseEntity.ok().build();
    }
}
