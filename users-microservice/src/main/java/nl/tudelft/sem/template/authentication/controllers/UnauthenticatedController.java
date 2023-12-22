package nl.tudelft.sem.template.authentication.controllers;

import java.util.UUID;
import nl.tudelft.sem.template.authentication.domain.user.UserService;
import nl.tudelft.sem.template.authentication.models.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/c/unauthenticated/")
public class UnauthenticatedController {
    private final transient UserService userService;

    @Autowired
    public UnauthenticatedController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserDetails(@PathVariable("userId") UUID userId) {
        UserModel userDetails = userService.getUserDetails(userId);
        return ResponseEntity.ok(userDetails);
    }
}
