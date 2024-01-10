package nl.tudelft.sem.template.authentication.controllers;

import nl.tudelft.sem.template.authentication.domain.user.UserService;
import nl.tudelft.sem.template.authentication.domain.user.Username;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
