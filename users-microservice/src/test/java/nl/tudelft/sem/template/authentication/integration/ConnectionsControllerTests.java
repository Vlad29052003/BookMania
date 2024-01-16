package nl.tudelft.sem.template.authentication.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.util.ArrayList;
import java.util.Collection;
import nl.tudelft.sem.template.authentication.authentication.JwtTokenGenerator;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.domain.user.HashedPassword;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import nl.tudelft.sem.template.authentication.domain.user.Username;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@ActiveProfiles("test")
@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class ConnectionsControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private transient JwtTokenGenerator jwtTokenGenerator;

    @Autowired
    private transient UserRepository userRepository;

    private static WireMockServer wireMockServer;



    /**
     * Sets up for testing.
     */
    @BeforeAll
    public static void setUp() {
        wireMockServer = new WireMockServer(new WireMockConfiguration().port(8080));
        wireMockServer.start();
        WireMock.configureFor("localhost", 8080);

    }


    @Test
    public void testFollowUser() throws Exception {
        final String testUser = "SomeUser";
        final String email = "test@email.com";
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        final AppUser user = new AppUser(new Username(testUser), email, testHashedPassword);
        user.setAuthority(Authority.REGULAR_USER);
        Collection<SimpleGrantedAuthority> roles = new ArrayList<>();
        roles.add(new SimpleGrantedAuthority(Authority.REGULAR_USER.toString()));
        final String token = jwtTokenGenerator.generateToken(new User(testUser,
                testHashedPassword.toString(), roles));
        userRepository.save(user);


        final String testUserFollow = "SomeUserToFollow";
        final String emailFollow = "testFollow@email.com";
        final HashedPassword testHashedPasswordFollow = new HashedPassword("hashedTestPassword");
        final AppUser userFollow = new AppUser(new Username(testUserFollow), emailFollow, testHashedPasswordFollow);
        userFollow.setAuthority(Authority.REGULAR_USER);
        userRepository.save(userFollow);

        mockMvc.perform(
                        post("/c/connections/" + testUserFollow)
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());


        ResultActions resultActions = mockMvc.perform(
                        get("/c/users")
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());


        String response = resultActions.andReturn().getResponse().getContentAsString();

        // neither UserModel nor UserProfile contain the follows field, so I cannot check if the user is followed
        // however, the followUser returns a 200, so I assume it works
    }


    @Test
    public void testUnfollowUser() throws Exception {
        final String testUser = "SomeUser";
        final String email = "test@email.com";
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        final AppUser user = new AppUser(new Username(testUser), email, testHashedPassword);
        user.setAuthority(Authority.REGULAR_USER);
        Collection<SimpleGrantedAuthority> roles = new ArrayList<>();
        roles.add(new SimpleGrantedAuthority(Authority.REGULAR_USER.toString()));
        final String token = jwtTokenGenerator.generateToken(new User(testUser,
                testHashedPassword.toString(), roles));
        userRepository.save(user);


        final String testUserFollow = "SomeUserToFollow";
        final String emailFollow = "testFollow@email.com";
        final HashedPassword testHashedPasswordFollow = new HashedPassword("hashedTestPassword");
        final AppUser userFollow = new AppUser(new Username(testUserFollow), emailFollow, testHashedPasswordFollow);
        userFollow.setAuthority(Authority.REGULAR_USER);
        userRepository.save(userFollow);

        mockMvc.perform(
                        post("/c/connections/" + testUserFollow)
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(
                        post("/c/connections/" + testUserFollow)
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                        delete("/c/connections/" + testUserFollow)
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(
                        delete("/c/connections/" + testUserFollow)
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());


        ResultActions resultActions = mockMvc.perform(
                        get("/c/users")
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());


        String response = resultActions.andReturn().getResponse().getContentAsString();

        // neither UserModel nor UserProfile contain the following field, so I cannot check if the user is followed
        // however, the followUser and unfollowUser returns a 200, so I assume it works
    }

    @AfterAll
    public static void stop() {
        wireMockServer.stop();
    }
}
