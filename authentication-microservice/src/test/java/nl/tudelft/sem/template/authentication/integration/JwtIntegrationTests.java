package nl.tudelft.sem.template.authentication.integration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import nl.tudelft.sem.template.authentication.authentication.JwtTokenGenerator;
import nl.tudelft.sem.template.authentication.domain.providers.TimeProvider;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.domain.user.HashedPassword;
import nl.tudelft.sem.template.authentication.domain.user.NetId;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
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
    private transient Instant mockedTime = Instant.parse("2050-12-31T13:25:34.00Z");
    private transient UserDetails user;
    private transient AppUser appUser;
    private transient String jwtToken;

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
        NetId netId = new NetId("username");
        HashedPassword hashedPassword = new HashedPassword("someHash");
        appUser = new AppUser(netId, "email@email.com", hashedPassword, Authority.REGULAR_USER);

        jwtToken = jwtTokenGenerator.generateToken(user);
    }

    @Test
    public void missingJwtToken() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/validate-token"));
        resultActions.andExpect(status().isBadRequest());
    }

    @Test
    public void forgedJwtToken() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/validate-token")
                .header("Authorization", "Bearer " + "thisIsTheForgedToken"));
        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    public void validateValidJwtToken() throws Exception {
        userRepository.save(appUser);

        ResultActions resultActions = mockMvc.perform(get("/validate-token")
                .header("Authorization", "Bearer " + jwtToken));

        resultActions.andExpect(status().isOk());
    }

    @Test
    public void userNotInDatabase_ValidJwtToken() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/validate-token")
                .header("Authorization", "Bearer " + jwtToken));

        resultActions.andExpect(status().isUnauthorized());
    }

    private void injectSecret(String secret) throws NoSuchFieldException, IllegalAccessException {
        Field declaredField = jwtTokenGenerator.getClass().getDeclaredField("jwtSecret");
        declaredField.setAccessible(true);
        declaredField.set(jwtTokenGenerator, secret);
    }
}
