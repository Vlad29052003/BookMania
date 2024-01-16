package nl.tudelft.sem.template.authentication.authentication;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import nl.tudelft.sem.template.authentication.application.user.UserEventsListener;
import nl.tudelft.sem.template.authentication.domain.providers.TimeProvider;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.AuthenticationService;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.domain.user.HashedPassword;
import nl.tudelft.sem.template.authentication.domain.user.Password;
import nl.tudelft.sem.template.authentication.domain.user.PasswordHashingService;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import nl.tudelft.sem.template.authentication.domain.user.Username;
import nl.tudelft.sem.template.authentication.domain.user.UsernameAlreadyInUseException;
import nl.tudelft.sem.template.authentication.models.AuthenticationRequestModel;
import nl.tudelft.sem.template.authentication.models.AuthenticationResponseModel;
import nl.tudelft.sem.template.authentication.models.RegistrationRequestModel;
import nl.tudelft.sem.template.authentication.models.ValidationTokenResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;

@ActiveProfiles("test")
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
    private transient AuthenticationService authenticationService;
    @Autowired
    private transient AuthenticationService authenticationService2;
    private transient UserDetails userDetails;
    private transient AppUser appUser;
    private transient RegistrationRequestModel registrationRequest;
    private transient AuthenticationRequestModel authenticationRequest;
    private transient AuthenticationResponseModel authenticationResponse;
    private transient ValidationTokenResponse validationTokenResponse;
    private transient UserRepository userRepository;
    private final transient JavaMailSender emailSender = mock(JavaMailSender.class);
    private final transient TimeProvider timeProvider = mock(TimeProvider.class);
    private final transient String token = "Bearer token";
    private static final String BOOKSHELF_PATH = "/a/user";
    private static ByteArrayOutputStream outputStreamCaptor;
    private static WireMockServer wireMockServer;
    private static final ConcurrentMap<String, Long> sessionMap = mock(ConcurrentMap.class);

    /**
     * Sets up for testing.
     */
    @BeforeAll
    public static void init() {
        wireMockServer = new WireMockServer(new WireMockConfiguration().port(8080));
        wireMockServer.start();

        configureFor("localhost", 8080);

        stubFor(post(urlEqualTo(BOOKSHELF_PATH))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse().withStatus(200)));

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

        authenticationService = new AuthenticationService(authenticationManager,
                jwtTokenGenerator, jwtUserDetailsService, userRepository, passwordHashingService,
                emailSender, timeProvider);

        String email = "email@gmail.com";
        String username = "user";
        String password = "someHash123!";
        Authority authority = Authority.REGULAR_USER;
        UUID id = UUID.randomUUID();

        userDetails = new User(username, password, List.of(authority));
        appUser = new AppUser(new Username(username), email, new HashedPassword(password));
        appUser.setId(id);

        registrationRequest = new RegistrationRequestModel();
        registrationRequest.setUsername(username);
        registrationRequest.setEmail(email);
        registrationRequest.setPassword(password);

        authenticationRequest = new AuthenticationRequestModel();
        authenticationRequest.setPassword(password);
        authenticationRequest.setUsername(username);

        authenticationResponse = new AuthenticationResponseModel();
        authenticationResponse.setToken(token);

        validationTokenResponse = new ValidationTokenResponse();
        validationTokenResponse.setId(id);
    }

    @Test
    public void registerUser() {
        authenticationService.registerUser(registrationRequest);
        verify(userRepository, times(1)).save(any());

        outputStreamCaptor.reset();

        authenticationService2.registerUser(registrationRequest);

        assertThat(outputStreamCaptor.toString().trim()).contains("Account of user with id ");

        AppUser appUser = new AppUser(new Username(registrationRequest.getUsername()), registrationRequest.getEmail(),
                new HashedPassword(registrationRequest.getPassword()));
        when(userRepository.findByUsername(new Username(registrationRequest.getUsername())))
                .thenReturn(Optional.of(appUser));
        Optional<AppUser> appUserOptional = userRepository.findByUsername(new Username(registrationRequest
                .getUsername()));

        assertThat(appUserOptional.get().getUsername()).isEqualTo(appUser.getUsername());
        assertThat(appUserOptional.get().getEmail()).isEqualTo(appUser.getEmail());
        assertThat(appUserOptional.get().getPassword()).isEqualTo(appUser.getPassword());
    }

    @Test
    public void registerUserException() throws Exception {
        authenticationService.registerUser(registrationRequest);
        Username username = new Username(registrationRequest.getUsername());
        String email = registrationRequest.getEmail();
        Password password = new Password(registrationRequest.getPassword());

        when(authenticationService.registrationHelper(username, email, password))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, ""));

        assertThrows(ResponseStatusException.class, () -> authenticationService.registerUser(registrationRequest));
    }

    @Test
    public void registerUserInvalidUsername() {
        authenticationService.registerUser(registrationRequest);
        registrationRequest.setUsername("@#asfa");
        assertThrows(ResponseStatusException.class, () -> authenticationService.registerUser(registrationRequest));
    }

    @Test
    public void authenticateUser() {
        when(jwtUserDetailsService.loadUserByUsername(authenticationRequest.getUsername())).thenReturn(userDetails);
        when(userRepository.findByUsername(any(Username.class))).thenReturn(Optional.ofNullable(appUser));
        when(jwtTokenGenerator.generateToken(userDetails)).thenReturn(token);

        assertEquals(authenticationService.authenticateUser(authenticationRequest), authenticationResponse);
    }

    @Test
    public void authenticateUserWith2fa() {
        Instant mockedTime = Instant.parse("2021-12-31T13:25:34.00Z");
        when(timeProvider.getCurrentTime()).thenReturn(mockedTime);
        appUser.set2faEnabled(true);

        when(jwtUserDetailsService.loadUserByUsername(authenticationRequest.getUsername())).thenReturn(userDetails);
        when(userRepository.findByUsername(any(Username.class))).thenReturn(Optional.ofNullable(appUser));
        when(jwtTokenGenerator.generateToken(userDetails)).thenReturn(token);

        assertNull(authenticationService.authenticateUser(authenticationRequest).getToken());
    }

    @Test
    public void authenticateUserDeactivated() {
        when(jwtUserDetailsService.loadUserByUsername(authenticationRequest.getUsername())).thenReturn(userDetails);
        when(authenticationManager.authenticate(any())).thenThrow(new DisabledException(""));

        assertThrows(ResponseStatusException.class, () -> authenticationService.authenticateUser(authenticationRequest));
    }

    @Test
    public void authenticateUserBadCredentials() {
        when(jwtUserDetailsService.loadUserByUsername(authenticationRequest.getUsername())).thenReturn(userDetails);
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException(""));

        assertThrows(ResponseStatusException.class, () -> authenticationService.authenticateUser(authenticationRequest));
    }

    @Test
    public void authenticateWith2faExpiredCode() {
        doThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "EXPIRED_CODE")).when(sessionMap).forEach(any());

        assertThrows(ResponseStatusException.class,
                () -> authenticationService.authenticateWith2fa(authenticationRequest));
    }

    @Test
    public void authenticateWith2faInvalidCredentials() {
        doNothing().when(sessionMap).forEach(any());

        assertThrows(ResponseStatusException.class,
                () -> authenticationService.authenticateWith2fa(authenticationRequest));
    }

    @Test
    public void testNotNullToken() {
        Instant mockedTime = Instant.parse("2021-12-31T13:25:34.00Z");
        when(timeProvider.getCurrentTime()).thenReturn(mockedTime);

        when(jwtUserDetailsService.loadUserByUsername("user")).thenReturn(userDetails);
        appUser.set2faEnabled(true);
        when(userRepository.findByUsername(new Username("user"))).thenReturn(Optional.of(appUser));
        authenticationService.authenticateUser(authenticationRequest);

        AppUser appUser1 = new AppUser(new Username("andrei"), "email@mail.com", new HashedPassword("hash"));
        UserDetails userDetails1 = new User("andrei", "hash", List.of(Authority.REGULAR_USER));
        when(jwtUserDetailsService.loadUserByUsername("andrei")).thenReturn(userDetails1);
        appUser1.set2faEnabled(true);
        when(userRepository.findByUsername(new Username("andrei"))).thenReturn(Optional.of(appUser1));
        AuthenticationRequestModel authenticationRequest2 = new AuthenticationRequestModel();
        authenticationRequest2.setUsername("andrei");
        authenticationRequest2.setPassword("hash");
        authenticationService.authenticateUser(authenticationRequest2);

        long timestamp = timeProvider.getCurrentTime().toEpochMilli();
        String code = String.valueOf(timestamp % 1000000);

        authenticationRequest.setPassword(code);
        String token = "generated token";
        when(jwtTokenGenerator.generateToken(any())).thenReturn(token);
        AuthenticationResponseModel response = authenticationService.authenticateWith2fa(authenticationRequest);
        assertThat(response.getToken()).isEqualTo(token);

        authenticationRequest.setUsername("other");
        assertThatThrownBy(() -> authenticationService.authenticateWith2fa(authenticationRequest))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("401 UNAUTHORIZED \"INVALID_CREDENTIALS\"");

        authenticationRequest.setUsername("user");
        mockedTime = Instant.parse("2021-12-31T13:26:34.01Z");
        when(timeProvider.getCurrentTime()).thenReturn(mockedTime);
        assertThatThrownBy(() -> authenticationService.authenticateWith2fa(authenticationRequest))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("401 UNAUTHORIZED \"EXPIRED_CODE\"");
    }

    @Test
    public void validateToken() {
        when(jwtUserDetailsService.loadUserByUsername("user")).thenReturn(userDetails);
        when(jwtService.extractUsername("token")).thenReturn(userDetails.getUsername());
        when(userRepository.findByUsername(appUser.getUsername())).thenReturn(Optional.of(appUser));

        assertEquals(authenticationService.getAuthority(new Username("user")), validationTokenResponse);
    }

    @Test
    public void validateTokenFails() {
        assertThrows(UsernameNotFoundException.class, () -> authenticationService.getAuthority(new Username("user")));
    }

    @Test
    public void validateTokenFailsEmptyOptional() {
        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
        when(jwtService.extractUsername("token")).thenReturn("user1");

        assertThrows(UsernameNotFoundException.class, () -> authenticationService.getAuthority(new Username("user")));
    }

    @Test
    public void testRegistrationHelper() throws Exception {
        Username username = new Username("user");
        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(passwordHashingService.hash(any())).thenReturn(new HashedPassword("hash"));

        AppUser user = authenticationService.registrationHelper(username, "email@mail.com", new Password("Pass123@"));

        assertThat(user.getAuthority()).isEqualTo(Authority.REGULAR_USER);
        assertThat(user.getUsername()).isEqualTo(username);
        assertThat(user.getPassword()).isEqualTo(new HashedPassword("hash"));
        assertThat(user.getEmail()).isEqualTo("email@mail.com");

        when(userRepository.existsByUsername(username)).thenReturn(true);
        assertThatThrownBy(() -> authenticationService.registrationHelper(
                username, "email@mail.com", new Password("Pass123@")))
                .isInstanceOf(UsernameAlreadyInUseException.class)
                .hasMessage("user username is already in use!");
    }

    @AfterAll
    public static void stop() {
        wireMockServer.stop();
    }
}
