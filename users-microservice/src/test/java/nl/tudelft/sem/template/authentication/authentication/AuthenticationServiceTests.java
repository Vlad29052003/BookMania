package nl.tudelft.sem.template.authentication.authentication;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import nl.tudelft.sem.template.authentication.application.user.UserEventsListener;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.AuthenticationService;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.domain.user.HashedPassword;
import nl.tudelft.sem.template.authentication.domain.user.Password;
import nl.tudelft.sem.template.authentication.domain.user.PasswordHashingService;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import nl.tudelft.sem.template.authentication.domain.user.Username;
import nl.tudelft.sem.template.authentication.models.AuthenticationRequestModel;
import nl.tudelft.sem.template.authentication.models.AuthenticationResponseModel;
import nl.tudelft.sem.template.authentication.models.RegistrationRequestModel;
import nl.tudelft.sem.template.authentication.models.TokenValidationResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AuthenticationServiceTests {
    private transient AuthenticationManager authenticationManager;
    private transient JwtTokenGenerator jwtTokenGenerator;
    private transient JwtUserDetailsService jwtUserDetailsService;
    private transient JwtService jwtService;
    private transient PasswordHashingService passwordHashingService;
    @Autowired
    private transient AuthenticationService authenticationService;
    private transient UserDetails userDetails;
    private transient AppUser appUser;
    private transient RegistrationRequestModel registrationRequest;
    private transient AuthenticationRequestModel authenticationRequest;
    private transient AuthenticationResponseModel authenticationResponse;
    private transient TokenValidationResponse tokenValidationResponse;
    private transient UserRepository userRepository;
    private final transient String token = "Bearer token";

    private static final String BOOKSHELF_URI = "/a/user";
//    private static final String REVIEW_URI = "/b/";
    private static WireMockServer mockServer;

    private static ByteArrayOutputStream outputStreamCaptor;

//    private transient UserWasCreatedListener userWasCreatedListener;

    @BeforeAll
    public static void init() {
        mockServer = new WireMockServer(new WireMockConfiguration().port(8080));
        mockServer.start();

        configureFor("localhost", 8080);
        stubFor(post(urlEqualTo(BOOKSHELF_URI))
                .willReturn(aResponse()
                        .withStatus(200)));

        outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));

        UserEventsListener.BOOKSHELF_URL = "http://localhost:8080/a/user";
    }
    /**
     * Sets up the testing environment.
     */
    @BeforeEach
    public void setUp() {
        authenticationManager = mock(AuthenticationManager.class);
        jwtTokenGenerator = mock(JwtTokenGenerator.class);
        jwtUserDetailsService = mock(JwtUserDetailsService.class);
        jwtService = mock(JwtService.class);
        userRepository = mock(UserRepository.class);
        passwordHashingService = mock(PasswordHashingService.class);
//        userWasCreatedListener = mock(UserWasCreatedListener.class);

        authenticationService = new AuthenticationService(authenticationManager,
                jwtTokenGenerator, jwtUserDetailsService,
                jwtService, userRepository, passwordHashingService);

        String email = "email";
        String netId = "user";
        String password = "someHash";
        Authority authority = Authority.REGULAR_USER;
        UUID id = UUID.randomUUID();

        userDetails = new User(netId, password, List.of(authority));
        appUser = new AppUser(new Username(netId), email, new HashedPassword(password));
        appUser.setId(id);

        registrationRequest = new RegistrationRequestModel();
        registrationRequest.setUsername(netId);
        registrationRequest.setEmail(email);
        registrationRequest.setPassword(password);

        authenticationRequest = new AuthenticationRequestModel();
        authenticationRequest.setPassword(password);
        authenticationRequest.setUsername(email);

        authenticationResponse = new AuthenticationResponseModel();
        authenticationResponse.setToken(token);

        tokenValidationResponse = new TokenValidationResponse();
        tokenValidationResponse.setId(id);
    }

    @Test
    public void registerUser() {
//        UUID id = UUID.randomUUID();
//        when(userRepository.findByUsername(new Username(registrationRequest.getUsername())).get().getId()).thenReturn(id);
        outputStreamCaptor.reset();

        authenticationService.registerUser(registrationRequest);

        verify(userRepository, times(1)).save(any());

        assertThat(outputStreamCaptor.toString().trim()).contains("User created");
    }

    @Test
    public void registerUserException() throws Exception {
        authenticationService.registerUser(registrationRequest);
        Username username = new Username(registrationRequest.getUsername());
        String email = registrationRequest.getEmail();
        Password password = new Password(registrationRequest.getPassword());

        when(authenticationService.registrationHelper(username, email, password))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, ""));

        assertThrows(ResponseStatusException.class, () -> {
            authenticationService.registerUser(registrationRequest);
        });
    }

    @Test
    public void registerUserInvalidUsername() {
        authenticationService.registerUser(registrationRequest);
        registrationRequest.setUsername("@#asfa");
        assertThrows(ResponseStatusException.class, () -> {
            authenticationService.registerUser(registrationRequest);
        });
    }

    @Test
    public void authenticateUser() {
        when(jwtUserDetailsService.loadUserByUsername(authenticationRequest.getUsername())).thenReturn(userDetails);
        when(jwtTokenGenerator.generateToken(userDetails)).thenReturn(token);

        assertEquals(authenticationService.authenticateUser(authenticationRequest), authenticationResponse);
    }

    @Test
    public void authenticateUserDeactivated() {
        when(jwtUserDetailsService.loadUserByUsername(authenticationRequest.getUsername())).thenReturn(userDetails);
        when(authenticationManager.authenticate(any())).thenThrow(new DisabledException(""));

        assertThrows(ResponseStatusException.class, () -> {
            authenticationService.authenticateUser(authenticationRequest);
        });
    }

    @Test
    public void authenticateUserBadCredentials() {
        when(jwtUserDetailsService.loadUserByUsername(authenticationRequest.getUsername())).thenReturn(userDetails);
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException(""));

        assertThrows(ResponseStatusException.class, () -> {
            authenticationService.authenticateUser(authenticationRequest);
        });
    }

    @Test
    public void validateToken() throws Exception {
        when(jwtUserDetailsService.loadUserByUsername("user")).thenReturn(userDetails);
        when(jwtService.extractUsername("token")).thenReturn(userDetails.getUsername());
        when(userRepository.findByUsername(appUser.getUsername())).thenReturn(Optional.of(appUser));

        assertEquals(authenticationService.getId(token), tokenValidationResponse);
    }

    @Test
    public void validateTokenFails() {
        assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.getId(null);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.getId("token");
        });
    }

    @Test
    public void validateTokenFailsEmptyOptional() {
        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
        when(jwtService.extractUsername("token")).thenReturn("user1");

        assertThrows(IllegalArgumentException.class, () -> authenticationService.getId("Bearer token"));
    }

    @AfterAll
    public static void afterAll() {
        mockServer.stop();
    }

}
