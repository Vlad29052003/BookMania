package nl.tudelft.sem.template.authentication.controllers;

import java.util.List;
import java.util.Map;
import nl.tudelft.sem.template.authentication.authentication.JwtTokenGenerator;
import nl.tudelft.sem.template.authentication.domain.book.Genre;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.UserService;
import nl.tudelft.sem.template.authentication.domain.user.Username;
import nl.tudelft.sem.template.authentication.models.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/user")
public class UserController {
    private final transient UserService userService;

    private final transient JwtTokenGenerator jwtTokenGenerator;

    private static final String AUTHORIZATION = "Authorization";

    /**
     * Instantiates a new UserController.
     *
     * @param userService the registration service
     * @param jwtTokenGenerator the token service
     */
    @Autowired
    public UserController(UserService userService, JwtTokenGenerator jwtTokenGenerator) {
        this.userService = userService;
        this.jwtTokenGenerator = jwtTokenGenerator;
    }

    /**
     * Helper method to extract the netId from the bearer token.
     *
     * @param bearerToken the serialized token
     * @return the netId of the user
     */
    private Username getNetId(String bearerToken) {
        String token = bearerToken.split(" ")[1];
        return new Username(jwtTokenGenerator.getUsername(token));
    }

    /**
     * Endpoint for getting a user.
     *
     * @param bearerToken The token associated with this user
     * @return the ResponseEntity containing the most important user information
     */
    @GetMapping
    public ResponseEntity<UserModel> getUserByNetId(@RequestHeader(name = AUTHORIZATION) String bearerToken) {
        AppUser user = userService.getUserByNetId(getNetId(bearerToken));

        UserModel userModel = new UserModel(user.getUsername().toString(), user.getEmail(), user.getName(), user.getBio(),
                                user.getLocation(), user.getFavouriteGenres(), user.getFavouriteBook());

        return ResponseEntity.ok(userModel);
    }

    /**
     * Endpoint for getting the profile picture of a user.
     *
     * @param bearerToken The token associated with this user
     * @return the ResponseEntity containing the profile photo
     */
    @GetMapping(value = "/picture", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> getUserPictureByNetId(@RequestHeader(name = AUTHORIZATION) String bearerToken) {
        AppUser user = userService.getUserByNetId(getNetId(bearerToken));

        return ResponseEntity.ok(user.getPicture());
    }

    /**
     * Endpoint for updating a user's name.
     *
     * @param name the new name of the user
     * @param bearerToken The token associated with this user
     * @return a ResponseEntity containing the OK response
     */
    @PatchMapping("/name")
    public ResponseEntity<Void> updateName(@RequestBody Map.Entry<String, String> name,
                                           @RequestHeader(name = AUTHORIZATION) String bearerToken) {
        userService.updateName(getNetId(bearerToken), name.getValue());

        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint for updating a user's bio.
     *
     * @param bio the new bio of the user
     * @param bearerToken The token associated with this user
     * @return a ResponseEntity containing the OK response
     */
    @PatchMapping("/bio")
    public ResponseEntity<Void> updateBio(@RequestBody Map.Entry<String, String> bio,
                                          @RequestHeader(name = AUTHORIZATION) String bearerToken) {
        userService.updateBio(getNetId(bearerToken), bio.getValue());

        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint for updating a user's profile picture.
     *
     * @param picture the new profile photo of the user
     * @param bearerToken The token associated with this user
     * @return a ResponseEntity containing the OK response
     */
    @PatchMapping(value = "/picture", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Void> updatePicture(@RequestBody byte[] picture,
                                              @RequestHeader(name = AUTHORIZATION) String bearerToken) {
        userService.updatePicture(getNetId(bearerToken), picture);

        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint for updating a user's location.
     *
     * @param location the new location of the user
     * @param bearerToken The token associated with this user
     * @return a ResponseEntity containing the OK response
     */
    @PatchMapping("/location")
    public ResponseEntity<Void> updateLocation(@RequestBody Map.Entry<String, String> location,
                                               @RequestHeader(name = AUTHORIZATION) String bearerToken) {
        userService.updateLocation(getNetId(bearerToken), location.getValue());

        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint for updating the list of favourite genres of a user.
     *
     * @param favouriteGenres the new list of favourite genres of the user
     * @param bearerToken The token associated with this user
     * @return a ResponseEntity containing the OK response
     */
    @PatchMapping("/favouriteGenres")
    public ResponseEntity<Void> updateFavouriteGenres(@RequestBody Map.Entry<String, List<Genre>> favouriteGenres,
                                                      @RequestHeader(name = AUTHORIZATION) String bearerToken) {
        userService.updateFavouriteGenres(getNetId(bearerToken), favouriteGenres.getValue());

        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint for updating a user's favourite book.
     *
     * @param favouriteBookId the new favourite book of the user
     * @param bearerToken The token associated with this user
     * @return a ResponseEntity containing the OK response
     */
    @PatchMapping("/favouriteBook")
    public ResponseEntity<Void> updateFavouriteBook(@RequestBody Map.Entry<String, Integer> favouriteBookId,
                                                    @RequestHeader(name = AUTHORIZATION) String bearerToken) {
        try {
            userService.updateFavouriteBook(getNetId(bearerToken), favouriteBookId.getValue());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }

        return ResponseEntity.ok().build();
    }
}
