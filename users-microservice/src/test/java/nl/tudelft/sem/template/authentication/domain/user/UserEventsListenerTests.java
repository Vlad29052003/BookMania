package nl.tudelft.sem.template.authentication.domain.user;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import nl.tudelft.sem.template.authentication.application.user.UserEventsListener;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UserEventsListenerTests {

    private final transient UserEventsListener userEventsListener = new UserEventsListener();

    @Autowired
    private transient UserRepository userRepository;

    private static final String BOOKSHELF_URI = "/a/user";

    private static final String REVIEW_URI = "/b/user";

    private static WireMockServer wireMockServer;

    private static ByteArrayOutputStream outputCaptor;

    @BeforeAll
    public static void setUp() {
        outputCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputCaptor));
    }

    /**
     * Configures the wire mock server.
     */
    public static void configureWireMockServer() {
        wireMockServer = new WireMockServer(new WireMockConfiguration().port(8080));
        wireMockServer.start();

        WireMock.configureFor("localhost", 8080);
    }

    @Test
    public void testOnUserWasCreated() {
        AppUser user = new AppUser(new Username("username"), "email@email.com", new HashedPassword("password"));
        userRepository.save(user);

        outputCaptor.reset();

        userEventsListener.onUserWasCreated(new UserWasCreatedEvent(user));

        assertThat(outputCaptor.toString().trim()).contains("Account of user with id ");
    }

    @Test
    public void testOnUserWasCreatedWithException() {
        configureWireMockServer();

        AppUser user = new AppUser(new Username("username"), "email@email.com", new HashedPassword("password"));

        outputCaptor.reset();

        stubFor(WireMock.post(urlEqualTo(BOOKSHELF_URI))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse().withStatus(404)));

        assertThrows(RuntimeException.class, () -> userEventsListener.onUserWasCreated(new UserWasCreatedEvent(user)));

        assertThat(outputCaptor.toString().trim()).doesNotContain("Account of user with id ");

        stop();
    }

    @Test
    public void testOnUserWasDeleted() {
        AppUser user = new AppUser(new Username("username"), "email@email.com", new HashedPassword("password"));
        userRepository.save(user);

        outputCaptor.reset();

        userEventsListener.onUserWasDeleted(new UserWasDeletedEvent(user, user.getId()));

        assertThat(outputCaptor.toString().trim()).contains("Account of user with id "
                + user.getId().toString() + " was deleted");
    }

    @Test
    public void testOnUserWasDeletedWithException() {
        configureWireMockServer();

        AppUser user = new AppUser(new Username("username"), "email@email.com", new HashedPassword("password"));
        userRepository.save(user);

        stubFor(WireMock.delete(urlEqualTo(BOOKSHELF_URI + "?userId=" + user.getId().toString()))
                .willReturn(aResponse().withStatus(404)));
        stubFor(WireMock.delete(urlEqualTo(REVIEW_URI + "/" + user.getId().toString() + "/" + user.getId()))
                .willReturn(aResponse().withStatus(404)));

        outputCaptor.reset();

        assertThrows(RuntimeException.class, () ->
                userEventsListener.onUserWasDeleted(new UserWasDeletedEvent(user, any())));

        assertThat(outputCaptor.toString().trim()).doesNotContain("Account of user with id "
                + user.getId().toString() + " was deleted");

        stop();
    }

    /**
     * Stops the wire mock server.
     */
    public static void stop() {
        wireMockServer.stop();
    }
}
