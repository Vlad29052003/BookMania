package nl.tudelft.sem.template.authentication.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import nl.tudelft.sem.template.authentication.authentication.JwtService;
import nl.tudelft.sem.template.authentication.authentication.JwtTokenGenerator;
import nl.tudelft.sem.template.authentication.authentication.JwtUserDetailsService;
import nl.tudelft.sem.template.authentication.models.AuthenticationRequestModel;
import nl.tudelft.sem.template.authentication.models.AuthenticationResponseModel;
import nl.tudelft.sem.template.authentication.models.RegistrationRequestModel;
import nl.tudelft.sem.template.authentication.models.TokenValidationResponse;
import nl.tudelft.sem.template.authentication.models.UserModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith(SpringExtension.class)
@SpringBootTest
// activate profiles to have spring use mocks during auto-injection of certain beans.
@ActiveProfiles({"test", "mockPasswordEncoder"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UserLookupServiceTests {

    @Autowired
    private transient UserLookupService userLookupService;

    @Autowired
    private transient AuthenticationService authenticationService;

    @Autowired
    private transient UserRepository userRepository;

    @Autowired
    private transient AuthenticationManager authenticationManager;

    @Autowired
    private transient JwtTokenGenerator jwtTokenGenerator;

    @Autowired
    private transient JwtUserDetailsService jwtUserDetailsService;

    @Autowired
    private transient JwtService jwtService;
    private transient RegistrationRequestModel registrationRequest;
    private transient RegistrationRequestModel registrationRequest2;


    /**
     * Sets up the testing environment.
     */
    @BeforeEach
    public void setUp() {
        PasswordHashingService passwordHashingService = mock(PasswordHashingService.class);
        when(passwordHashingService.hash(new Password("someOtherHash"))).thenReturn(new HashedPassword("someHash"));
        authenticationService = new AuthenticationService(authenticationManager,
                jwtTokenGenerator, jwtUserDetailsService,
                jwtService, userRepository, passwordHashingService);

        String email = "email";
        String username = "user";
        String password = "someHash";
        UUID id = UUID.randomUUID();

        AppUser appUser = new AppUser(new Username(username), email, new HashedPassword(password));
        appUser.setId(id);

        registrationRequest = new RegistrationRequestModel();
        registrationRequest.setUsername(username);
        registrationRequest.setEmail(email);
        registrationRequest.setPassword(password);

        AuthenticationRequestModel authenticationRequest = new AuthenticationRequestModel();
        authenticationRequest.setPassword(password);
        authenticationRequest.setUsername(email);

        AuthenticationResponseModel authenticationResponse = new AuthenticationResponseModel();
        String token = "Bearer token";
        authenticationResponse.setToken(token);

        TokenValidationResponse tokenValidationResponse = new TokenValidationResponse();
        tokenValidationResponse.setId(id);


        String email2 = "email2";
        String username2 = "andrei";
        String password2 = "someHash";
        UUID id2 = UUID.randomUUID();

        AppUser appUser2 = new AppUser(new Username(username2), email2, new HashedPassword(password2));
        appUser2.setId(id2);

        registrationRequest2 = new RegistrationRequestModel();
        registrationRequest2.setUsername(username2);
        registrationRequest2.setEmail(email2);
        registrationRequest2.setPassword(password2);

    }

    /**
     * Tests whether the user is correctly returned when using the proper query string.
     *
     */
    @Test
    public void userSearchByName_worksCorrectly() {

        authenticationService.registerUser(registrationRequest);
        authenticationService.registerUser(registrationRequest2);

        // Assert
        List<String> foundUsers = userLookupService.getUsersByName("user")
                .stream().map(UserModel::getNetId).collect(Collectors.toList());

        assertThat(foundUsers).containsExactlyInAnyOrder("user");
    }


    /**
     * Tests whether all users are returned when using an empty query string.
     *
     */
    @Test
    public void userSearchByName_worksCorrectly2() {

        authenticationService.registerUser(registrationRequest);
        authenticationService.registerUser(registrationRequest2);

        // Assert
        List<String> foundUsers = userLookupService.getUsersByName("")
                .stream().map(UserModel::getNetId).collect(Collectors.toList());

        assertThat(foundUsers).containsExactlyInAnyOrder("user", "andrei");
    }

    @Test
    public void userSearchByName_worksCorrectly_deactivatedUser() {

        authenticationService.registerUser(registrationRequest);
        authenticationService.registerUser(registrationRequest2);

        AppUser deactivated = userRepository.findByUsername(new Username("user")).get();
        deactivated.setDeactivated(true);
        userRepository.saveAndFlush(deactivated);

        // Assert
        List<String> foundUsers = userLookupService.getUsersByName("user")
                .stream().map(UserModel::getNetId).collect(Collectors.toList());
        List<String> expected = new ArrayList<>();


        assertThat(foundUsers).isEqualTo(expected);
    }
}
