package nl.tudelft.sem.template.authentication.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import nl.tudelft.sem.template.authentication.controllers.AuthenticationController;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.models.AuthenticationRequestModel;
import nl.tudelft.sem.template.authentication.models.AuthenticationResponseModel;
import nl.tudelft.sem.template.authentication.models.RegistrationRequestModel;
import nl.tudelft.sem.template.authentication.models.TokenValidationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

public class AuthenticationControllerTests {
    private final transient AuthenticationService authenticationService = mock(AuthenticationService.class);
    private final transient AuthenticationController authenticationController =
            new AuthenticationController(authenticationService);

    @Test
    public void registrationValid() {
        RegistrationRequestModel request = new RegistrationRequestModel();
        request.setUsername("user");
        request.setEmail("email");
        request.setPassword("password");

        assertEquals(authenticationController.register(request), ResponseEntity.ok().build());
    }

    @Test
    public void registrationRequestThrowsError() {
        RegistrationRequestModel request = new RegistrationRequestModel();
        request.setUsername("user");
        request.setEmail("email");
        request.setPassword("password");

        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, ""))
                .when(authenticationService).registerUser(request);

        assertEquals(authenticationController.register(request),
                new ResponseEntity<>("Username already in use! or Email already in use!",
                        HttpStatus.BAD_REQUEST));
    }

    @Test
    public void authenticationRequestValid() {
        AuthenticationRequestModel request = new AuthenticationRequestModel();
        request.setUsername("username");
        request.setPassword("pass");
        AuthenticationResponseModel response = new AuthenticationResponseModel();
        response.setToken("token");

        when(authenticationService.authenticateUser(request)).thenReturn(response);

        assertEquals(authenticationController.authenticate(request), ResponseEntity.ok(response));
    }

    @Test
    public void authenticationRequestThrowsError() {
        AuthenticationRequestModel request = new AuthenticationRequestModel();
        request.setUsername("usernameS");
        request.setPassword("pass");

        when(authenticationService.authenticateUser(request))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, ""));

        assertEquals(authenticationController.authenticate(request).getStatusCodeValue(), 401);
    }

    @Test
    public void validateToken() throws Exception {
        TokenValidationResponse response = new TokenValidationResponse();
        response.setAuthority(Authority.REGULAR_USER);
        when(authenticationService.getAuthority("token")).thenReturn(response);
        assertEquals(authenticationController.verifyJwt("token"), ResponseEntity.ok(response));
    }

    @Test
    public void validateTokenThrowsError() throws Exception {
        when(authenticationService.getAuthority("token")).thenThrow(new Exception());

        assertEquals(authenticationController.verifyJwt("token"),
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized!"));
    }
}
