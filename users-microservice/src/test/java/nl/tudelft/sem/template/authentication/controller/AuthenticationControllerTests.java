package nl.tudelft.sem.template.authentication.controller;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import nl.tudelft.sem.template.authentication.controllers.AuthenticationController;
import nl.tudelft.sem.template.authentication.domain.user.AuthenticationService;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.domain.user.Username;
import nl.tudelft.sem.template.authentication.models.AuthenticationRequestModel;
import nl.tudelft.sem.template.authentication.models.AuthenticationResponseModel;
import nl.tudelft.sem.template.authentication.models.RegistrationRequestModel;
import nl.tudelft.sem.template.authentication.models.ValidationTokenResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.server.ResponseStatusException;

public class AuthenticationControllerTests {
    private final transient AuthenticationService authenticationService = mock(AuthenticationService.class);
    private final transient AuthenticationController authenticationController =
            new AuthenticationController(authenticationService);

    @Test
    public void registrationValid() {
        RegistrationRequestModel request = new RegistrationRequestModel();
        request.setUsername("user");
        request.setEmail("email@gmail.com");
        request.setPassword("Password123!");

        assertEquals(authenticationController.register(request), ResponseEntity.ok().build());
    }

    @Test
    public void registrationRequestThrowsError() {
        RegistrationRequestModel request = new RegistrationRequestModel();
        request.setUsername("user");
        request.setEmail("email@gmail.com");
        request.setPassword("Password123!");

        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, ""))
                .when(authenticationService).registerUser(request);

        assertEquals(authenticationController.register(request),
                new ResponseEntity<>("Username or email already in use!",
                        HttpStatus.BAD_REQUEST));
    }

    @Test
    public void authenticationRequestValid() {
        AuthenticationRequestModel request = new AuthenticationRequestModel();
        request.setUsername("username");
        request.setPassword("Password123!");
        AuthenticationResponseModel response = new AuthenticationResponseModel();
        response.setToken("token");

        when(authenticationService.authenticateUser(request)).thenReturn(response);

        assertEquals(authenticationController.authenticate(request), ResponseEntity.ok(response));
    }

    @Test
    public void authenticationRequestThrowsError() {
        AuthenticationRequestModel request = new AuthenticationRequestModel();
        request.setUsername("usernameS");
        request.setPassword("Password123!");

        when(authenticationService.authenticateUser(request))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, ""));

        assertEquals(authenticationController.authenticate(request).getStatusCodeValue(), 401);
    }

    @Test
    public void authenticationRequestWith2fa() {
        AuthenticationRequestModel request = new AuthenticationRequestModel();
        request.setUsername("username");
        request.setPassword("Password123!");
        AuthenticationResponseModel response = new AuthenticationResponseModel();
        response.setToken("token");

        when(authenticationService.authenticateWith2fa(request)).thenReturn(response);

        assertEquals(authenticationController.authenticateWith2fa(request), ResponseEntity.ok(response));
    }

    @Test
    public void authenticationRequestWith2faThrowsError() {
        AuthenticationRequestModel request = new AuthenticationRequestModel();
        request.setUsername("usernameS");
        request.setPassword("Password123!");

        when(authenticationService.authenticateWith2fa(request))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "EXPIRED_CODE"));

        assertEquals(authenticationController.authenticateWith2fa(request).getStatusCodeValue(), 401);
    }

    @Test
    public void validateToken() {
        Authentication authenticationMock = mock(Authentication.class);
        when(authenticationMock.getName()).thenReturn("user");
        doReturn(List.of(Authority.REGULAR_USER)).when(authenticationMock).getAuthorities();
        SecurityContext securityContextMock = mock(SecurityContext.class);
        when(securityContextMock.getAuthentication()).thenReturn(authenticationMock);
        SecurityContextHolder.setContext(securityContextMock);

        ValidationTokenResponse validationTokenResponse = new ValidationTokenResponse();
        validationTokenResponse.setAuthority(Authority.ADMIN);
        validationTokenResponse.setId(UUID.randomUUID());
        when(authenticationService.getAuthority(new Username("user"))).thenReturn(validationTokenResponse);

        assertEquals(authenticationController.verifyJwt(), ResponseEntity.ok(validationTokenResponse));
        assertEquals(((ValidationTokenResponse)
                Objects.requireNonNull(authenticationController.verifyJwt().getBody())).getId(),
                validationTokenResponse.getId());
        assertEquals(((ValidationTokenResponse)
                Objects.requireNonNull(authenticationController.verifyJwt().getBody())).getAuthority(),
                validationTokenResponse.getAuthority());

        SecurityContextHolder.clearContext();
    }

    @Test
    public void validateTokenThrowsError() {
        when(authenticationService.getAuthority(any()))
                .thenThrow(new UsernameNotFoundException("User does not exist!"));

        assertEquals(authenticationController.verifyJwt(),
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized!"));
    }

    @Test
    public void testErrormessage() {
        String errorMessage = "password unauthorized";
        HttpStatus errorStatus = HttpStatus.UNAUTHORIZED;
        doThrow(new ResponseStatusException(errorStatus, errorMessage))
                .when(authenticationService).registerUser(any());
        assertThat(authenticationController.register(new RegistrationRequestModel()))
                .isEqualTo(new ResponseEntity<>(errorStatus + " \"" + errorMessage + "\"", errorStatus));
    }
}
