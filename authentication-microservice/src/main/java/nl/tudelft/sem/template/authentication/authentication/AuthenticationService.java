package nl.tudelft.sem.template.authentication.authentication;

import nl.tudelft.sem.template.authentication.authentication.JwtService;
import nl.tudelft.sem.template.authentication.authentication.JwtTokenGenerator;
import nl.tudelft.sem.template.authentication.authentication.JwtUserDetailsService;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.domain.user.EmailNotFoundException;
import nl.tudelft.sem.template.authentication.domain.user.NetId;
import nl.tudelft.sem.template.authentication.domain.user.Password;
import nl.tudelft.sem.template.authentication.domain.user.RegistrationService;
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
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthenticationService {
    private final transient AuthenticationManager authenticationManager;

    private final transient JwtTokenGenerator jwtTokenGenerator;

    private final transient JwtUserDetailsService jwtUserDetailsService;

    private final transient RegistrationService registrationService;
    private final transient JwtService jwtService;

    /**
     * Creates an AuthenticationService service.
     *
     * @param authenticationManager manages the authentication
     * @param jwtTokenGenerator     manages the jwt generation
     * @param jwtUserDetailsService manages the user details
     * @param registrationService   manages the registration
     * @param jwtService            extract the claims from the jwt
     */
    @Autowired
    public AuthenticationService(AuthenticationManager authenticationManager,
                                 JwtTokenGenerator jwtTokenGenerator,
                                 JwtUserDetailsService jwtUserDetailsService,
                                 RegistrationService registrationService,
                                 JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenGenerator = jwtTokenGenerator;
        this.jwtUserDetailsService = jwtUserDetailsService;
        this.registrationService = registrationService;
        this.jwtService = jwtService;
    }

    /**
     * Registers a new user.
     *
     * @param registrationRequest contains the registration data
     */
    public void registerUser(RegistrationRequestModel registrationRequest) {
        try {
            NetId netId = new NetId(registrationRequest.getNetId());
            String email = registrationRequest.getEmail();
            Password password = new Password(registrationRequest.getPassword());
            Authority authority = Authority.valueOf(registrationRequest.getAuthority());
            registrationService.registerUser(netId, email, password, authority);
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
            userDetails = jwtUserDetailsService.loadUserByEmail(authenticationRequest.getEmail());
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            userDetails.getUsername(),
                            authenticationRequest.getPassword()));
        } catch (DisabledException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "USER_DISABLED", e);
        } catch (BadCredentialsException | EmailNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", e);
        }

        final String jwtToken = jwtTokenGenerator.generateToken(userDetails);
        return new AuthenticationResponseModel(jwtToken);
    }

    /**
     * Gets the authority the user has.
     *
     * @param token is the jwt bearer token.
     * @return a data object containing the authority
     * @throws Exception if the validation of the token fails
     */
    public TokenValidationResponse getAuthority(String token) throws Exception {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new IllegalArgumentException();
        }
        return new TokenValidationResponse(
                Authority.valueOf(jwtUserDetailsService
                        .loadUserByUsername(jwtService.extractUsername(token.substring(7)))
                        .getAuthorities().iterator().next().toString()));
    }
}
