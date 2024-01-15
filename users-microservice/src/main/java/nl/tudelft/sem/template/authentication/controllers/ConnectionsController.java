package nl.tudelft.sem.template.authentication.controllers;

import nl.tudelft.sem.template.authentication.domain.user.UserService;
import nl.tudelft.sem.template.authentication.domain.user.Username;
import nl.tudelft.sem.template.authentication.models.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/c/connections")
public class ConnectionsController {
    private final transient UserService userService;

    /**
     * Instantiates a new ConnectionsController.
     *
     * @param userService the user service
     */
    @Autowired
    public ConnectionsController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Endpoint for following a user.
     *
     * @param username the username of the user to follow
     * @return a ResponseEntity containing the OK response if the follow is successful
     */
    @PostMapping("/{username}")
    public ResponseEntity<Void> followUser(@PathVariable String username) {
        Username currentUsername = new Username(SecurityContextHolder.getContext().getAuthentication().getName());
        try {
            userService.followUser(currentUsername, new Username(username));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }


        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint for unfollowing a user.
     *
     * @param username the username of the user to unfollow
     * @return a ResponseEntity containing the OK response if the unfollow is successful
     */
    @DeleteMapping("/{username}")
    public ResponseEntity<Void> unfollowUser(@PathVariable String username) {
        Username currentUsername = new Username(SecurityContextHolder.getContext().getAuthentication().getName());
        try {
            userService.unfollowUser(currentUsername, new Username(username));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok().build();
    }

    /**
     * Get a user's followers.
     *
     * @param username user whose followers we wish to see.
     * @return a list of the user's followers.
     */
    @GetMapping("/followers/{username}")
    public ResponseEntity<List<UserModel>> getFollowers(@PathVariable String username) {
        if (!SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        List<UserModel> followers = new ArrayList<>();
        try {
            followers = userService.getFollowers(username);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(followers);
    }

    /**
     * Get a list of users that are followed by a specific user.
     *
     * @param username the specific user.
     * @return a list of users that are followed by this user.
     */
    @GetMapping("/follows/{username}")
    public ResponseEntity<List<UserModel>> getFollowing(@PathVariable String username) {
        if (!SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        List<UserModel> follows = new ArrayList<>();
        try {
            follows = userService.getFollowing(username);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(follows);
    }
}
