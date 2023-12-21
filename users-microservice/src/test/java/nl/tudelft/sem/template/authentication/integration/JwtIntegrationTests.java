package nl.tudelft.sem.template.authentication.integration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import javax.transaction.Transactional;
import nl.tudelft.sem.template.authentication.authentication.JwtTokenGenerator;
import nl.tudelft.sem.template.authentication.domain.providers.TimeProvider;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.domain.user.HashedPassword;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import nl.tudelft.sem.template.authentication.domain.user.Username;
import nl.tudelft.sem.template.authentication.integration.utils.JsonUtil;
import nl.tudelft.sem.template.authentication.models.TokenValidationResponse;
import nl.tudelft.sem.template.authentication.models.UserModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@ExtendWith(SpringExtension.class)
// activate profiles to have spring use mocks during auto-injection of certain beans.
@ActiveProfiles({"test", "mockPasswordEncoder"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class JwtIntegrationTests {
    @Autowired
    private transient MockMvc mockMvc;
    @Autowired
    private transient UserRepository userRepository;
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

    /**
     * Sets up the testing environment.
     *
     * @throws NoSuchFieldException if there is no such field
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
        HashedPassword hashedPassword = new HashedPassword("someHash");
        appUser = new AppUser(username, "email@email.com", hashedPassword);

        jwtToken = jwtTokenGenerator.generateToken(user);

        user = mock(UserDetails.class);
        when(user.getUsername()).thenReturn(null);
        when(user.getAuthorities()).thenReturn(null);
        nullUsernameJwtToken = jwtTokenGenerator.generateToken(user);

        user = new User("username", "someHash", List.of(Authority.REGULAR_USER));
        when(timeProvider.getCurrentTime()).thenReturn(expiredTime);
        jwtExpiredToken = jwtTokenGenerator.generateToken(user);
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

        TokenValidationResponse expected = new TokenValidationResponse();
        expected.setId(id);
        expected.setAuthority(Authority.REGULAR_USER);

        ResultActions resultActions = mockMvc.perform(get("/c/validate-token")
                .header("Authorization", "Bearer " + jwtToken));

        TokenValidationResponse response = JsonUtil
                .deserialize(resultActions.andReturn().getResponse().getContentAsString(),
                        TokenValidationResponse.class);

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

    private void injectSecret(String secret) throws NoSuchFieldException, IllegalAccessException {
        Field declaredField = jwtTokenGenerator.getClass().getDeclaredField("jwtSecret");
        declaredField.setAccessible(true);
        declaredField.set(jwtTokenGenerator, secret);
    }
}
