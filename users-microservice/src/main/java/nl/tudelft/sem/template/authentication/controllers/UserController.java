package nl.tudelft.sem.template.authentication.controllers;

import java.util.ArrayList;
import java.util.List;
import nl.tudelft.sem.template.authentication.domain.book.Genre;
import nl.tudelft.sem.template.authentication.domain.rolechange.RoleChange;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.EmailAlreadyInUseException;
import nl.tudelft.sem.template.authentication.domain.user.Password;
import nl.tudelft.sem.template.authentication.domain.user.PasswordHashingService;
import nl.tudelft.sem.template.authentication.domain.user.UserService;
import nl.tudelft.sem.template.authentication.domain.user.Username;
import nl.tudelft.sem.template.authentication.domain.user.UsernameAlreadyInUseException;
import nl.tudelft.sem.template.authentication.models.BanUserRequestModel;
import nl.tudelft.sem.template.authentication.models.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/c/users")
public class UserController {
    private final transient UserService userService;
    private final transient PasswordHashingService passwordHashingService;

    /**
     * Instantiates a new UserController.
     *
     * @param userService the registration service
     */
    @Autowired
    public UserController(UserService userService, PasswordHashingService passwordHashingService) {
        this.userService = userService;
        this.passwordHashingService = passwordHashingService;
    }

    /**
     * Endpoint for getting a user.
     *
     * @return the ResponseEntity containing the most important user information
     */
    @GetMapping
    public ResponseEntity<UserProfile> getUserByUsername() {
        Username username = new Username(SecurityContextHolder.getContext().getAuthentication().getName());
        AppUser user = userService.getUserByUsername(username);

        UserProfile userProfile = new UserProfile(user);

        return ResponseEntity.ok(userProfile);
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
     * @param bio the new bio of the user
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
     * @param picture the new profile photo of the user
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
     * @param location the new location of the user
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
     * Patch request to ban / unban a user.
     *
     * @param banUserRequestModel username and future status of user that needs to be (un)banned.
     * @return request status.
     */
    @PatchMapping("/isDeactivated")
    public ResponseEntity<?> updateBannedStatus(@RequestBody BanUserRequestModel banUserRequestModel) {
        String authority = new ArrayList<>(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .get(0).getAuthority();
        try {
            userService.updateBannedStatus(new Username(banUserRequestModel.getUsername()),
                    banUserRequestModel.getIsBanned(),
                    authority);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getMessage(), e.getStatus());
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Update a user's authority.
     *
     * @param roleChange role change request of the user.
     * @return 200 OK if the authority was changed successfully; errors otherwise.
     */
    @PatchMapping("/authority")
    public ResponseEntity<?> updateAuthority(@RequestBody RoleChange roleChange) {
        String authority = new ArrayList<>(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .get(0).getAuthority();
        try {
            userService.updateAuthority(roleChange.getId(), roleChange.getNewRole(), authority);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getMessage(), e.getStatus());
        }
        return ResponseEntity.ok().build();
    }



    /**
     * Endpoint for updating the username.
     *
     * @param newUsername the new username
     * @return a ResponseEntity containing the OK response
     */
    @PatchMapping("/username")
    public ResponseEntity<Void> updateUsername(@RequestBody String newUsername) {
        try {
            Username username = new Username(SecurityContextHolder.getContext().getAuthentication().getName());
            userService.updateUsername(username, newUsername);
        } catch (UsernameAlreadyInUseException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint for updating the email of a user.
     *
     * @param newEmail the new email
     * @return a ResponseEntity containing the OK response
     */
    @PatchMapping("/email")
    public ResponseEntity<Void> updateEmail(@RequestBody String newEmail) {
        try {
            Username username = new Username(SecurityContextHolder.getContext().getAuthentication().getName());
            userService.updateEmail(username, newEmail);
        } catch (EmailAlreadyInUseException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint for updating the password of a user.
     *
     * @param newPassword the new password
     * @return a ResponseEntity containing the OK response
     */
    @PatchMapping("/password")
    public ResponseEntity<Void> updatePassword(@RequestBody String newPassword) {
        try {
            Username username = new Username(SecurityContextHolder.getContext().getAuthentication().getName());
            Password newPass = new Password(newPassword);
            userService.updatePassword(username, passwordHashingService.hash(newPass));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
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
        userService.delete(username, username);

        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint for deleting a user by an admin.
     *
     * @param username username of the user to be deleted
     * @return a ResponseEntity containing the OK response
     */
    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteByAdmin(@PathVariable String username) {
        Username adminUsername = new Username(SecurityContextHolder.getContext().getAuthentication().getName());
        userService.delete(new Username(username), adminUsername);

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

        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint for updating a user's two-factor authentication settings.
     *
     * @param is2faEnabled new 2fa status
     * @return a ResponseEntity containing the OK response
     */
    @PatchMapping("/is2faEnabled")
    public ResponseEntity<Void> update2fa(@RequestBody String is2faEnabled) {
        Username username = new Username(SecurityContextHolder.getContext().getAuthentication().getName());
        userService.update2fa(username, Boolean.parseBoolean(is2faEnabled));

        return ResponseEntity.ok().build();
    }
}
