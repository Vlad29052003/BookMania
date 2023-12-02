package nl.tudelft.sem.template.authentication.domain.user;

import java.util.Optional;
import nl.tudelft.sem.template.authentication.authentication.JwtService;
import nl.tudelft.sem.template.authentication.authentication.JwtTokenGenerator;
import nl.tudelft.sem.template.authentication.authentication.JwtUserDetailsService;
import nl.tudelft.sem.template.authentication.models.AuthenticationRequestModel;
import nl.tudelft.sem.template.authentication.models.AuthenticationResponseModel;
import nl.tudelft.sem.template.authentication.models.RegistrationRequestModel;
import nl.tudelft.sem.template.authentication.models.TokenValidationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthenticationService {
    private final transient AuthenticationManager authenticationManager;
    private final transient JwtTokenGenerator jwtTokenGenerator;
    private final transient JwtUserDetailsService jwtUserDetailsService;
    private final transient JwtService jwtService;
    private final transient UserRepository userRepository;
    private final transient PasswordHashingService passwordHashingService;

    /**
     * Creates an AuthenticationService service.
     *
     * @param authenticationManager  manages the authentication
     * @param jwtTokenGenerator      manages the jwt generation
     * @param jwtUserDetailsService  manages the user details
     * @param jwtService             extract the claims from the jwt
     * @param passwordHashingService the password encoder
     */
    @Autowired
    public AuthenticationService(AuthenticationManager authenticationManager,
                                 JwtTokenGenerator jwtTokenGenerator,
                                 JwtUserDetailsService jwtUserDetailsService,
                                 JwtService jwtService,
                                 UserRepository userRepository,
                                 PasswordHashingService passwordHashingService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenGenerator = jwtTokenGenerator;
        this.jwtUserDetailsService = jwtUserDetailsService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordHashingService = passwordHashingService;
    }

    /**
     * Registers a new user.
     *
     * @param registrationRequest contains the registration data
     */
    public void registerUser(RegistrationRequestModel registrationRequest) {
        try {
            Username username = new Username(registrationRequest.getUsername());
            String email = registrationRequest.getEmail();
            Password password = new Password(registrationRequest.getPassword());
            registrationHelper(username, email, password);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Authenticates a user.
     *
     * @param authenticationRequest a data object containing the authentication data
     * @return a jwt token
     * @throws ResponseStatusException if the authentication fails
     */
    public AuthenticationResponseModel authenticateUser(
            AuthenticationRequestModel authenticationRequest) throws ResponseStatusException {

        UserDetails userDetails;
        try {
            userDetails = jwtUserDetailsService.loadUserByUsername(authenticationRequest.getUsername());
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            userDetails.getUsername(),
                            authenticationRequest.getPassword()));
        } catch (DisabledException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "USER_DISABLED");
        } catch (BadCredentialsException | UsernameNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
        }

        final String jwtToken = jwtTokenGenerator.generateToken(userDetails);
        return new AuthenticationResponseModel(jwtToken);
    }

    /**
     * Gets the authority the user has.
     *
     * @param token is the jwt bearer token.
     * @return a data object containing the authority
     */
    public TokenValidationResponse getId(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new IllegalArgumentException();
        }
        Optional<AppUser> appUserOptional = userRepository
                .findByUsername(new Username(jwtService.extractUsername(token.substring(7))));
        if (appUserOptional.isEmpty()) {
            throw new IllegalArgumentException();
        }
        return new TokenValidationResponse(appUserOptional.get().getId());
    }

    /**
     * Register a new user.
     *
     * @param username The NetID of the user
     * @param email The email of the user
     * @param password The password of the user
     * @throws Exception if the user already exists
     */
    public AppUser registrationHelper(Username username, String email, Password password) throws Exception {

        if (checkUsernameIsUnique(username)) {
            // Hash password
            HashedPassword hashedPassword = passwordHashingService.hash(password);

            // Create new account
            AppUser user = new AppUser(username, email, hashedPassword);
            userRepository.save(user);

            return user;
        }

        throw new UsernameAlreadyInUseException(username);
    }

    private boolean checkUsernameIsUnique(Username username) {
        return !userRepository.existsByUsername(username);
    }
}