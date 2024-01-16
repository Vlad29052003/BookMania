package nl.tudelft.sem.template.authentication.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import javax.transaction.Transactional;
import nl.tudelft.sem.template.authentication.application.user.UserEventsListener;
import nl.tudelft.sem.template.authentication.authentication.JwtTokenGenerator;
import nl.tudelft.sem.template.authentication.domain.providers.TimeProvider;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.domain.user.HashedPassword;
import nl.tudelft.sem.template.authentication.domain.user.Password;
import nl.tudelft.sem.template.authentication.domain.user.PasswordHashingService;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import nl.tudelft.sem.template.authentication.domain.user.Username;
import nl.tudelft.sem.template.authentication.integration.utils.JsonUtil;
import nl.tudelft.sem.template.authentication.models.AuthenticationRequestModel;
import nl.tudelft.sem.template.authentication.models.AuthenticationResponseModel;
import nl.tudelft.sem.template.authentication.models.RegistrationRequestModel;
import nl.tudelft.sem.template.authentication.models.UserModel;
import nl.tudelft.sem.template.authentication.models.ValidationTokenResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@ExtendWith(SpringExtension.class)
// activate profiles to have spring use mocks during auto-injection of certain beans.
@ActiveProfiles({"test"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class JwtIntegrationTests {
    @Autowired
    private transient MockMvc mockMvc;
    @Autowired
    private transient UserRepository userRepository;
    @Autowired
    private transient PasswordHashingService passwordHashingService;
    @Value("${jwt.secret}")
    private transient String secret;
    private TimeProvider timeProvider;
    private transient JwtTokenGenerator jwtTokenGenerator;
    private final transient Instant mockedTime = Instant.parse("2150-12-31T13:25:34.00Z");
    private final transient Instant expiredTime = Instant.parse("1999-12-31T13:25:34.00Z");
    private transient UserDetails user;
    private transient AppUser appUser;
    private transient String jwtToken;
    private transient String jwtExpiredToken;
    private transient String nullUsernameJwtToken;

    private static final String BOOKSHELF_PATH = "/a/user";

    private static WireMockServer wireMockServer;
    private final transient ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    /**
     * Sets up the testing environment.
     */
    @BeforeAll
    public static void init() {
        wireMockServer = new WireMockServer(8080);
        wireMockServer.start();

        configureFor("localhost", 8080);

        stubFor(WireMock.post(urlEqualTo(BOOKSHELF_PATH))
                .willReturn(aResponse().withStatus(200)));

        UserEventsListener.BOOKSHELF_URL = "http://localhost:8080/a/user";
    }

    /**
     * Sets up the testing environment.
     *
     * @throws NoSuchFieldException   if there is no such field
     * @throws IllegalAccessException if there is no access
     */
    @BeforeEach
    public void setup() throws NoSuchFieldException, IllegalAccessException {
        timeProvider = mock(TimeProvider.class);
        when(timeProvider.getCurrentTime()).thenReturn(mockedTime);

        jwtTokenGenerator = new JwtTokenGenerator(timeProvider);
        this.injectSecret(secret);

        user = new User("username", "someHash", List.of(Authority.REGULAR_USER));
        Username username = new Username("username");
        HashedPassword hashedPassword = passwordHashingService.hash(new Password("Pass123!"));
        appUser = new AppUser(username, "email@email.com", hashedPassword);

        jwtToken = jwtTokenGenerator.generateToken(user);

        user = mock(UserDetails.class);
        when(user.getUsername()).thenReturn(null);
        when(user.getAuthorities()).thenReturn(null);
        nullUsernameJwtToken = jwtTokenGenerator.generateToken(user);

        user = new User("username", "someHash", List.of(Authority.REGULAR_USER));
        when(timeProvider.getCurrentTime()).thenReturn(expiredTime);
        jwtExpiredToken = jwtTokenGenerator.generateToken(user);

        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @Test
    public void notBearerJwtToken() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/c/validate-token")
                .header("Authorization", "token"));
        resultActions.andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Bearer"))
                .andExpect(content().string(""));
    }

    @Test
    public void missingJwtToken() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/c/validate-token"));
        resultActions.andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Bearer"))
                .andExpect(content().string(""));
    }

    @Test
    public void forgedJwtToken() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/c/validate-token")
                .header("Authorization", "Bearer " + "thisIsTheForgedToken"));
        resultActions.andExpect(status().isUnauthorized())
                .andExpect(content().string("Unable to parse JWT token"));
    }

    @Test
    @Transactional
    public void validateValidJwtToken() throws Exception {
        userRepository.save(appUser);
        UUID id = userRepository.findAll().get(0).getId();

        ValidationTokenResponse expected = new ValidationTokenResponse();
        expected.setId(id);
        expected.setAuthority(Authority.REGULAR_USER);

        ResultActions resultActions = mockMvc.perform(get("/c/validate-token")
                .header("Authorization", "Bearer " + jwtToken));

        ValidationTokenResponse response = JsonUtil
                .deserialize(resultActions.andReturn().getResponse().getContentAsString(),
                        ValidationTokenResponse.class);

        resultActions.andExpect(status().isOk());

        assertThat(response.getId()).isEqualTo(expected.getId());
        assertThat(response.getAuthority()).isEqualTo(expected.getAuthority());
    }

    @Test
    public void userNotInDatabase_ValidJwtToken() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/c/validate-token")
                .header("Authorization", "Bearer " + jwtToken));

        resultActions.andExpect(status().isUnauthorized())
                .andExpect(content().string("User does not exist!"));
    }

    @Test
    public void expiredToken() throws Exception {
        userRepository.saveAndFlush(appUser);
        ResultActions resultActions = mockMvc.perform(get("/c/validate-token")
                .header("Authorization", "Bearer " + jwtExpiredToken));

        resultActions.andExpect(status().isUnauthorized())
                .andExpect(content().string(("JWT token has expired")));
    }

    @Test
    public void deactivatedUser() throws Exception {
        appUser.setDeactivated(true);
        userRepository.saveAndFlush(appUser);
        ResultActions resultActions = mockMvc.perform(get("/c/validate-token")
                .header("Authorization", "Bearer " + jwtToken));

        resultActions.andExpect(status().isUnauthorized())
                .andExpect(content().string("User is deactivated"));
    }

    @Test
    public void someException() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/c/validate-token")
                .header("Authorization", "Bearer "));

        resultActions.andExpect(status().isUnauthorized())
                .andExpect(content().string("Unauthorized"));
    }

    @Test
    public void testNullUsernameInToken() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/c/validate-token")
                .header("Authorization", "Bearer " + nullUsernameJwtToken));

        resultActions.andExpect(status().isUnauthorized())
                .andExpect(content().string(("Unauthorized")));
    }

    @Test
    @Transactional
    public void unauthenticatedRequest() throws Exception {
        userRepository.save(appUser);
        AppUser found = userRepository.findAll().get(0);
        UUID id = found.getId();

        UserModel expected = new UserModel(found);

        ResultActions resultActions = mockMvc.perform(get("/c/unauthenticated/" + id));

        UserModel response = JsonUtil
                .deserialize(resultActions.andReturn().getResponse().getContentAsString(),
                        UserModel.class);

        resultActions.andExpect(status().isOk());
        assertThat(response).isEqualTo(expected);
    }

    @Test
    public void unauthenticatedRequestNoUsers() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/c/unauthenticated/" + UUID.randomUUID()));

        resultActions.andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void performFullCycle() throws Exception {
        RegistrationRequestModel registrationRequest = new RegistrationRequestModel();
        registrationRequest.setUsername("user1");
        registrationRequest.setEmail("user@email.com");
        registrationRequest.setPassword("Pass123@");

        ResultActions register = mockMvc.perform(post("/c/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(registrationRequest)));

        register.andExpect(status().isOk());
        assertThat(outputStreamCaptor.toString().trim())
                .contains("Account of user with ");
        assertThat(userRepository.findAll().get(0).getDomainEventsSize()).isEqualTo(0);

        AuthenticationRequestModel authenticationRequest = new AuthenticationRequestModel();
        authenticationRequest.setUsername("user1");
        authenticationRequest.setPassword("Pass123@");

        ResultActions authenticate = mockMvc.perform(post("/c/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(authenticationRequest)));

        authenticate.andExpect(status().isOk());
        AuthenticationResponseModel authenticationResponse =
                JsonUtil.deserialize(authenticate.andReturn().getResponse().getContentAsString(),
                        AuthenticationResponseModel.class);

        UUID id = userRepository.findAll().get(0).getId();

        ValidationTokenResponse expected = new ValidationTokenResponse();
        expected.setId(id);
        expected.setAuthority(Authority.REGULAR_USER);

        ResultActions resultActions = mockMvc.perform(get("/c/validate-token")
                .header("Authorization", "Bearer " + authenticationResponse.getToken()));

        ValidationTokenResponse response = JsonUtil
                .deserialize(resultActions.andReturn().getResponse().getContentAsString(),
                        ValidationTokenResponse.class);

        resultActions.andExpect(status().isOk());

        assertThat(response.getId()).isEqualTo(expected.getId());
        assertThat(response.getAuthority()).isEqualTo(expected.getAuthority());

        registrationRequest.setEmail("modEmail@maiul.org");
        ResultActions register2 = mockMvc.perform(post("/c/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(registrationRequest)));

        register2.andExpect(status().isBadRequest())
                .andExpect(content().string("Username or email already in use!"));
    }

    @Test
    public void test2faEnabledAuthentication() throws Exception {
        appUser.set2faEnabled(true);
        userRepository.saveAndFlush(appUser);
        AuthenticationRequestModel authenticationRequest = new AuthenticationRequestModel();
        authenticationRequest.setUsername(appUser.getUsername().toString());
        authenticationRequest.setPassword("Pass123!");

        ResultActions authenticate = mockMvc.perform(post("/c/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(authenticationRequest)));

        authenticate.andExpect(status().isFound())
                .andExpect(MockMvcResultMatchers.header().string("Location", "http://localhost/c/authenticate/2fa"));
    }

    private void injectSecret(String secret) throws NoSuchFieldException, IllegalAccessException {
        Field declaredField = jwtTokenGenerator.getClass().getDeclaredField("jwtSecret");
        declaredField.setAccessible(true);
        declaredField.set(jwtTokenGenerator, secret);
    }

    @AfterEach
    public void revertChanges() {
        System.setOut(System.out);
    }

    @AfterAll
    public static void stop() {
        wireMockServer.stop();
    }
}
