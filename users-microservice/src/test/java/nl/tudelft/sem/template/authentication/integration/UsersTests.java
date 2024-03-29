package nl.tudelft.sem.template.authentication.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.util.UUID;
import nl.tudelft.sem.template.authentication.application.user.UserEventsListener;
import nl.tudelft.sem.template.authentication.authentication.JwtTokenGenerator;
import nl.tudelft.sem.template.authentication.domain.stats.Stats;
import nl.tudelft.sem.template.authentication.domain.stats.StatsRepository;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.HashedPassword;
import nl.tudelft.sem.template.authentication.domain.user.Password;
import nl.tudelft.sem.template.authentication.domain.user.PasswordHashingService;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import nl.tudelft.sem.template.authentication.domain.user.Username;
import nl.tudelft.sem.template.authentication.integration.utils.JsonUtil;
import nl.tudelft.sem.template.authentication.models.AuthenticationRequestModel;
import nl.tudelft.sem.template.authentication.models.AuthenticationResponseModel;
import nl.tudelft.sem.template.authentication.models.RegistrationRequestModel;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@ExtendWith(SpringExtension.class)
// activate profiles to have spring use mocks during auto-injection of certain beans.
@ActiveProfiles({"test", "mockPasswordEncoder", "mockTokenGenerator", "mockAuthenticationManager"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class UsersTests {
    @Autowired
    private transient MockMvc mockMvc;

    @Autowired
    private transient PasswordHashingService mockPasswordEncoder;

    @Autowired
    private transient JwtTokenGenerator mockJwtTokenGenerator;

    @Autowired
    private transient AuthenticationManager mockAuthenticationManager;

    @Autowired
    private transient UserRepository userRepository;

    @Autowired
    private transient StatsRepository statsRepository;

    private static WireMockServer wireMockServer;

    /**
     * Sets up for testing.
     */
    @BeforeAll
    public static void setUp() {
        wireMockServer = new WireMockServer(new WireMockConfiguration().port(8080));
        wireMockServer.start();

        configureFor("localhost", 8080);

        stubFor(WireMock.any(urlPathMatching(".*"))
                .willReturn(aResponse()
                        .withStatus(200)));
    }

    @Test
    public void register_withValidData_worksCorrectly() throws Exception {
        // Arrange
        final Username testUser = new Username("SomeUser");
        final String email = "test@email.com";
        final Password testPassword = new Password("Password123!");
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        when(mockPasswordEncoder.hash(testPassword)).thenReturn(testHashedPassword);

        RegistrationRequestModel model = new RegistrationRequestModel();
        model.setUsername(testUser.toString());
        model.setEmail(email);
        model.setPassword(testPassword.toString());

        // Act
        ResultActions resultActions = mockMvc.perform(post("/c/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(model)));

        // Assert
        resultActions.andExpect(status().isOk());

        AppUser savedUser = userRepository.findByUsername(testUser).orElseThrow();

        assertThat(savedUser.getUsername()).isEqualTo(testUser);
        assertThat(savedUser.getEmail()).isEqualTo(email);
        assertThat(savedUser.getPassword()).isEqualTo(testHashedPassword);
    }

    @Test
    public void register_withExistingUser_throwsException() throws Exception {
        // Arrange
        final Username testUser = new Username("SomeUser1");
        final String email = "testEmail@gmail.com";
        final Password newTestPassword = new Password("Password456!");
        final HashedPassword existingTestPassword = new HashedPassword("Password23!");

        AppUser existingAppUser = new AppUser(testUser, email, existingTestPassword);
        userRepository.save(existingAppUser);

        RegistrationRequestModel model = new RegistrationRequestModel();
        model.setUsername(testUser.toString());
        model.setPassword(newTestPassword.toString());

        // Act
        ResultActions resultActions = mockMvc.perform(post("/c/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(model)));

        // Assert
        resultActions.andExpect(status().isBadRequest());

        AppUser savedUser = userRepository.findByUsername(testUser).orElseThrow();

        assertThat(savedUser.getUsername()).isEqualTo(testUser);
        assertThat(savedUser.getPassword()).isEqualTo(existingTestPassword);
    }

    @Test
    public void login_withValidUser_returnsToken() throws Exception {
        // Arrange
        final Username testUser = new Username("SomeUser2");
        final String email = "testEmail@gmail.com";
        final Password testPassword = new Password("Password13!");
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        when(mockPasswordEncoder.hash(testPassword)).thenReturn(testHashedPassword);

        when(mockAuthenticationManager.authenticate(argThat(authentication ->
                !testUser.toString().equals(authentication.getPrincipal())
                    || !testPassword.toString().equals(authentication.getCredentials())
        ))).thenThrow(new UsernameNotFoundException("User not found"));

        final String testToken = "testJWTToken";
        when(mockJwtTokenGenerator.generateToken(
            argThat(userDetails -> userDetails.getUsername().equals(testUser.toString())))
        ).thenReturn(testToken);

        AppUser appUser = new AppUser(testUser, email, testHashedPassword);
        userRepository.save(appUser);

        UUID userId = userRepository.findAll().get(0).getId();

        statsRepository.save(new Stats(userId, 0));

        AuthenticationRequestModel model = new AuthenticationRequestModel();
        model.setUsername(testUser.toString());
        model.setPassword(testPassword.toString());

        // Act
        ResultActions resultActions = mockMvc.perform(post("/c/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(model)));


        // Assert
        MvcResult result = resultActions
                .andExpect(status().isOk())
                .andReturn();

        AuthenticationResponseModel responseModel = JsonUtil.deserialize(result.getResponse().getContentAsString(),
                AuthenticationResponseModel.class);

        assertThat(responseModel.getToken()).isEqualTo(testToken);

        assertThat(statsRepository.findAll().get(0).getNumberOfLogins()).isEqualTo(1);

        verify(mockAuthenticationManager).authenticate(argThat(authentication ->
                testUser.toString().equals(authentication.getPrincipal())
                    && testPassword.toString().equals(authentication.getCredentials())));
    }

    @Test
    public void login_withNonexistentUsername_returns401() throws Exception {
        // Arrange
        final String testUser = "SomeUser3";
        final String testPassword = "Password1235!";

        when(mockAuthenticationManager.authenticate(argThat(authentication ->
                testUser.equals(authentication.getPrincipal())
                    && testPassword.equals(authentication.getCredentials())
        ))).thenThrow(new UsernameNotFoundException("User not found"));

        AuthenticationRequestModel model = new AuthenticationRequestModel();
        model.setUsername(testUser);
        model.setPassword(testPassword);

        // Act
        ResultActions resultActions = mockMvc.perform(post("/c/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(model)));

        // Assert
        resultActions.andExpect(status().isUnauthorized());

        verify(mockJwtTokenGenerator, times(0)).generateToken(any());
    }

    @Test
    public void login_withInvalidPassword_returns403() throws Exception {
        // Arrange
        final String testUser = "SomeUser4";
        final String email = "testEmail@gmail.com";
        final String wrongPassword = "Password1234!";
        final String testPassword = "Password12!";
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        when(mockPasswordEncoder.hash(new Password(testPassword))).thenReturn(testHashedPassword);

        when(mockAuthenticationManager.authenticate(argThat(authentication ->
                testUser.equals(authentication.getPrincipal())
                    && wrongPassword.equals(authentication.getCredentials())
        ))).thenThrow(new BadCredentialsException("Invalid password"));

        AppUser appUser = new AppUser(new Username(testUser), email, testHashedPassword);
        userRepository.save(appUser);

        AuthenticationRequestModel model = new AuthenticationRequestModel();
        model.setUsername(testUser);
        model.setPassword(wrongPassword);

        // Act
        ResultActions resultActions = mockMvc.perform(post("/c/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(model)));

        // Assert
        resultActions.andExpect(status().isUnauthorized());

        verify(mockJwtTokenGenerator, times(0)).generateToken(any());
    }

    @AfterAll
    public static void stop() {
        wireMockServer.stop();
    }
}
