package nl.tudelft.sem.template.authentication.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.domain.user.HashedPassword;
import nl.tudelft.sem.template.authentication.domain.user.Password;
import nl.tudelft.sem.template.authentication.domain.user.RegistrationService;
import nl.tudelft.sem.template.authentication.domain.user.Username;
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
import org.springframework.web.server.ResponseStatusException;

public class AuthenticationServiceTests {
    private transient AuthenticationManager authenticationManager;
    private transient JwtTokenGenerator jwtTokenGenerator;
    private transient JwtUserDetailsService jwtUserDetailsService;
    private transient RegistrationService registrationService;
    private transient JwtService jwtService;
    private transient AuthenticationService authenticationService;
    private transient UserDetails userDetails;
    private transient AppUser appUser;
    private transient RegistrationRequestModel registrationRequest;
    private transient AuthenticationRequestModel authenticationRequest;
    private transient AuthenticationResponseModel authenticationResponse;
    private transient TokenValidationResponse tokenValidationResponse;
    private final transient String token = "Bearer token";

    /**
     * Sets up the testing environment.
     */
    @BeforeEach
    public void setUp() {
        authenticationManager = mock(AuthenticationManager.class);
        jwtTokenGenerator = mock(JwtTokenGenerator.class);
        jwtUserDetailsService = mock(JwtUserDetailsService.class);
        registrationService = mock(RegistrationService.class);
        jwtService = mock(JwtService.class);
        System.out.println(registrationService);
        authenticationService = new AuthenticationService(
                authenticationManager, jwtTokenGenerator, jwtUserDetailsService, registrationService, jwtService);

        String email = "email";
        String netId = "user";
        String password = "someHash";
        Authority authority = Authority.REGULAR_USER;

        userDetails = new User(netId, password, List.of(authority));
        appUser = new AppUser(new Username(netId), email, new HashedPassword(password));

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
        tokenValidationResponse.setAuthority(authority);
    }

    @Test
    public void registerUser() throws Exception {
        authenticationService.registerUser(registrationRequest);
        Username username = new Username(registrationRequest.getUsername());
        String email = registrationRequest.getEmail();
        Password password = new Password(registrationRequest.getPassword());

        verify(registrationService, times(1)).registerUser(username, email, password);
    }

    @Test
    public void registerUserException() throws Exception {
        authenticationService.registerUser(registrationRequest);
        Username username = new Username(registrationRequest.getUsername());
        String email = registrationRequest.getEmail();
        Password password = new Password(registrationRequest.getPassword());
        Authority authority = Authority.REGULAR_USER;

        when(registrationService.registerUser(username, email, password))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, ""));

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

        assertEquals(authenticationService.getAuthority(token), tokenValidationResponse);
    }

    @Test
    public void validateTokenFails() {
        assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.getAuthority(null);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.getAuthority("token");
        });
    }

}
