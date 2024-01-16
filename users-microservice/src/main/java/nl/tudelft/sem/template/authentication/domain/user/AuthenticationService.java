package nl.tudelft.sem.template.authentication.domain.user;

import static nl.tudelft.sem.template.authentication.application.Constants.NO_SUCH_USER;

import java.time.Instant;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import nl.tudelft.sem.template.authentication.authentication.JwtTokenGenerator;
import nl.tudelft.sem.template.authentication.authentication.JwtUserDetailsService;
import nl.tudelft.sem.template.authentication.domain.providers.TimeProvider;
import nl.tudelft.sem.template.authentication.models.AuthenticationRequestModel;
import nl.tudelft.sem.template.authentication.models.AuthenticationResponseModel;
import nl.tudelft.sem.template.authentication.models.RegistrationRequestModel;
import nl.tudelft.sem.template.authentication.models.ValidationTokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
    private final transient UserRepository userRepository;
    private final transient PasswordHashingService passwordHashingService;
    private final transient JavaMailSender emailSender;
    private final transient TimeProvider timeProvider;
    private static final int codeValiditySeconds = 60;
    private static final String usernameCodeSeparator = "~";
    private static final ConcurrentMap<String, Long> sessionMap = new ConcurrentHashMap<>();

    /**
     * Creates an AuthenticationService service.
     *
     * @param authenticationManager  manages the authentication
     * @param jwtTokenGenerator      manages the jwt generation
     * @param jwtUserDetailsService  manages the user details
     * @param passwordHashingService the password encoder
     */
    @Autowired
    public AuthenticationService(AuthenticationManager authenticationManager,
                                 JwtTokenGenerator jwtTokenGenerator,
                                 JwtUserDetailsService jwtUserDetailsService,
                                 UserRepository userRepository,
                                 PasswordHashingService passwordHashingService,
                                 JavaMailSender emailSender,
                                 TimeProvider timeProvider) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenGenerator = jwtTokenGenerator;
        this.jwtUserDetailsService = jwtUserDetailsService;
        this.userRepository = userRepository;
        this.passwordHashingService = passwordHashingService;
        this.emailSender = emailSender;
        this.timeProvider = timeProvider;
    }

    static {
        Timer sessionMapManager = new Timer();
        sessionMapManager.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sessionMap.entrySet().removeIf(userCode -> {
                    long codeTimestamp = userCode.getValue();
                    long timespan = Instant.now().toEpochMilli() - codeTimestamp;
                    return timespan > codeValiditySeconds * 1000;
                });
            }
        }, 0, codeValiditySeconds * 1000);
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

        String token = null;
        AppUser user = userRepository.findByUsername(new Username(authenticationRequest.getUsername())).get();
        if (user.is2faEnabled()) {
            long timestamp = timeProvider.getCurrentTime().toEpochMilli();
            sessionMap.put(user.getUsername().getUsernameValue() + usernameCodeSeparator + timestamp, timestamp);
            SimpleMailMessage email = new SimpleMailMessage();
            email.setTo(user.getEmail());
            email.setSubject("BookMania Authentication Code");
            email.setText("Your authentication code is: " + timestamp % 1000000 + ".\nIt is valid for "
                    + codeValiditySeconds + " seconds. Afterwards you will need to request another.");
            emailSender.send(email);
        } else {
            token = jwtTokenGenerator.generateToken(userDetails);
        }
        return new AuthenticationResponseModel(token);
    }

    /**
     * Authenticate a user with two-factor authentication.
     *
     * @param authenticationRequestModel an object containing the username and the 2fa code
     * @return the JWT token
     * @throws ResponseStatusException if the authentication fails
     */
    public AuthenticationResponseModel authenticateWith2fa(AuthenticationRequestModel authenticationRequestModel) {
        String username = authenticationRequestModel.getUsername();
        AtomicReference<AuthenticationResponseModel> token = new AtomicReference<>(null);
        sessionMap.forEach((userCode, value) -> {
            if (userCode.startsWith(username)
                    && userCode.endsWith(authenticationRequestModel.getPassword())
                    && token.get() == null) {
                if (timeProvider.getCurrentTime().toEpochMilli() - value > codeValiditySeconds * 1000) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "EXPIRED_CODE");
                }
                UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(username);
                token.set(new AuthenticationResponseModel(jwtTokenGenerator.generateToken(userDetails)));
            }
        });
        if (token.get() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
        }
        return token.get();
    }

    /**
     * Gets the authority the user has.
     *
     * @return a data object containing the authority
     */
    public ValidationTokenResponse getAuthority(Username username) {
        Optional<AppUser> appUserOptional = userRepository
                .findByUsername(username);
        if (appUserOptional.isEmpty()) {
            throw new UsernameNotFoundException(NO_SUCH_USER);
        }
        return new ValidationTokenResponse(appUserOptional.get().getId(), appUserOptional.get().getAuthority());
    }

    /**
     * Register a new user.
     *
     * @param username The NetID of the user
     * @param email    The email of the user
     * @param password The password of the user
     * @throws Exception if the user already exists
     */
    public AppUser registrationHelper(Username username, String email, Password password) throws Exception {
        if (checkUsernameIsUnique(username)) {
            // Hash password
            HashedPassword hashedPassword = passwordHashingService.hash(password);
            // Create new account
            AppUser user = new AppUser(username, email, hashedPassword);

            user.recordUserWasCreated();

            userRepository.save(user);

            return user;
        }

        throw new UsernameAlreadyInUseException(username);
    }

    private boolean checkUsernameIsUnique(Username username) {
        return !userRepository.existsByUsername(username);
    }
}
