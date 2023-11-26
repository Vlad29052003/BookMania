package nl.tudelft.sem.template.authentication.controllers;

import nl.tudelft.sem.template.authentication.authentication.JwtTokenGenerator;
import nl.tudelft.sem.template.authentication.authentication.JwtUserDetailsService;
import nl.tudelft.sem.template.authentication.domain.user.NetId;
import nl.tudelft.sem.template.authentication.domain.user.Password;
import nl.tudelft.sem.template.authentication.domain.user.RegistrationService;
import nl.tudelft.sem.template.authentication.models.AuthenticationRequestModel;
import nl.tudelft.sem.template.authentication.models.AuthenticationResponseModel;
import nl.tudelft.sem.template.authentication.models.RegistrationRequestModel;
import nl.tudelft.sem.template.authentication.services.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class AuthenticationController {

    private final transient AuthenticationService authenticationService;

    /**
     * Instantiates a new UsersController.
     *
     * @param authenticationService the authentication service
     */
    @Autowired
    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Endpoint for registration.
     *
     * @param request The registration model
     * @return 200 OK if the registration is successful
     * @throws ResponseStatusException if a user with this netId already exists
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegistrationRequestModel request) throws ResponseStatusException {
        try {
            authenticationService.registerUser(request);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>("Username already in use! or Email already in use!", e.getStatus());
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint for authentication.
     *
     * @param request The login model
     * @return JWT token if the login is successful
     * @throws ResponseStatusException if the user does not exist or the password is incorrect
     */
    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody AuthenticationRequestModel request) throws ResponseStatusException {
        try {
            AuthenticationResponseModel authenticationResponseModel = authenticationService.authenticateUser(request);
            return ResponseEntity.ok(authenticationResponseModel);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getMessage(), e.getStatus());
        }
    }

    /**
     * Endpoint for validating the jwt bearer token.
     *
     * @param token The bearer jwt token
     * @return Authority if verification is successful
     */
    @GetMapping("/validate-token")
    public ResponseEntity<?> verifyJwt(@RequestHeader("Authorization") String token) {
        try {
            return ResponseEntity.ok(authenticationService.getAuthority(token));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized!");
        }
    }
}



