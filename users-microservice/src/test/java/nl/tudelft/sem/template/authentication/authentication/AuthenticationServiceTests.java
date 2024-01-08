package nl.tudelft.sem.template.authentication.authentication;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
import nl.tudelft.sem.template.authentication.models.TokenValidationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.server.ResponseStatusException;

public class AuthenticationServiceTests {
    private transient AuthenticationManager authenticationManager;
    private transient JwtTokenGenerator jwtTokenGenerator;
    private transient JwtUserDetailsService jwtUserDetailsService;
    private transient JwtService jwtService;
    private transient PasswordHashingService passwordHashingService;
    private transient AuthenticationService authenticationService;
    private transient UserDetails userDetails;
    private transient AppUser appUser;
    private transient RegistrationRequestModel registrationRequest;
    private transient AuthenticationRequestModel authenticationRequest;
    private transient AuthenticationResponseModel authenticationResponse;
    private transient TokenValidationResponse tokenValidationResponse;
    private transient UserRepository userRepository;
    private final transient String token = "Bearer token";

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
                jwtTokenGenerator, jwtUserDetailsService, userRepository, passwordHashingService);

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
        authenticationRequest.setUsername(email);

        authenticationResponse = new AuthenticationResponseModel();
        authenticationResponse.setToken(token);

        tokenValidationResponse = new TokenValidationResponse();
        tokenValidationResponse.setId(id);
    }

    @Test
    public void registerUser() {
        authenticationService.registerUser(registrationRequest);
        verify(userRepository, times(1)).save(any());
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
        when(jwtTokenGenerator.generateToken(userDetails)).thenReturn(token);

        assertEquals(authenticationService.authenticateUser(authenticationRequest), authenticationResponse);
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
    public void validateToken() {
        when(jwtUserDetailsService.loadUserByUsername("user")).thenReturn(userDetails);
        when(jwtService.extractUsername("token")).thenReturn(userDetails.getUsername());
        when(userRepository.findByUsername(appUser.getUsername())).thenReturn(Optional.of(appUser));

        assertEquals(authenticationService.getId(new Username("user")), tokenValidationResponse);
    }

    @Test
    public void validateTokenFails() {
        assertThrows(UsernameNotFoundException.class, () -> authenticationService.getId(new Username("user")));
    }

    @Test
    public void validateTokenFailsEmptyOptional() {
        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
        when(jwtService.extractUsername("token")).thenReturn("user1");

        assertThrows(UsernameNotFoundException.class, () -> authenticationService.getId(new Username("user")));
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

}
