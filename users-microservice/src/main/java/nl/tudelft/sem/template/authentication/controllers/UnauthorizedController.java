package nl.tudelft.sem.template.authentication.controllers;

import nl.tudelft.sem.template.authentication.domain.user.UserService;
import nl.tudelft.sem.template.authentication.models.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;

@RestController
@RequestMapping("/c/unauthorized/")
public class UnauthorizedController {
    private final transient UserService userService;

    @Autowired
    public UnauthorizedController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserDetails(@PathVariable("userId") UUID userId) {
        try {
            UserModel userDetails = userService.getUserDetails(userId);
            return ResponseEntity.ok(userDetails);
        } catch (UsernameNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
