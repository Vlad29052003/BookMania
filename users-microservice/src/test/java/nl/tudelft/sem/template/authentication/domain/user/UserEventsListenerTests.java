package nl.tudelft.sem.template.authentication.domain.user;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import nl.tudelft.sem.template.authentication.application.user.UserEventsListener;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ActiveProfiles("test")
@SpringBootTest
@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UserEventsListenerTests {

    private final transient UserEventsListener userEventsListener = new UserEventsListener();

    @Autowired
    private transient UserRepository userRepository;

    private static final String BOOKSHELF_URI = "/bookshelf_service/user";

    private static final String REVIEW_URI = "/b/user";

    private static WireMockServer wireMockServer;

    private static ByteArrayOutputStream outputCaptor;

    /**
     * Sets up for testing.
     */
    @BeforeAll
    public static void setUp() {
        wireMockServer = new WireMockServer(new WireMockConfiguration().port(8080));
        wireMockServer.start();

        WireMock.configureFor("localhost", 8080);

        outputCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputCaptor));

        UserEventsListener.REVIEW_URL = "http://localhost:8080/b/user";
    }

    @Test
    public void testOnUserWasCreated() {
        AppUser user = new AppUser(new Username("username"), "email@email.com", new HashedPassword("password"));
        userRepository.save(user);

        outputCaptor.reset();

        stubFor(WireMock.post(urlEqualTo(BOOKSHELF_URI))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse().withStatus(200)));

        userEventsListener.onUserWasCreated(new UserWasCreatedEvent(user));

        assertThat(outputCaptor.toString().trim()).contains("Account of user with id ");
        verify(postRequestedFor(urlEqualTo(BOOKSHELF_URI))
                .withHeader("Content-Type", equalTo("application/json")));
    }

    @Test
    public void testOnUserWasCreatedWithException() {
        AppUser user = new AppUser(new Username("username"), "email@email.com", new HashedPassword("password"));

        outputCaptor.reset();

        stubFor(WireMock.post(urlEqualTo(BOOKSHELF_URI))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse().withStatus(404)));

        assertThrows(RuntimeException.class, () -> userEventsListener.onUserWasCreated(new UserWasCreatedEvent(user)));

        assertThat(outputCaptor.toString().trim()).doesNotContain("Account of user with id ");
        verify(postRequestedFor(urlEqualTo(BOOKSHELF_URI))
                .withHeader("Content-Type", equalTo("application/json")));
    }

    @Test
    public void testOnUserWasDeleted() {
        AppUser user = new AppUser(new Username("username"), "email@email.com", new HashedPassword("password"));
        userRepository.save(user);

        outputCaptor.reset();

        stubFor(WireMock.delete(urlEqualTo(BOOKSHELF_URI + "?userId=" + user.getId().toString()))
                .willReturn(aResponse().withStatus(200)));
        stubFor(WireMock.delete(urlEqualTo(REVIEW_URI + "/" + user.getId().toString() + "/" + user.getId()))
                .willReturn(aResponse().withStatus(200)));

        userEventsListener.onUserWasDeleted(new UserWasDeletedEvent(user, user.getId()));

        assertThat(outputCaptor.toString().trim()).contains("Account of user with id "
                + user.getId().toString() + " was deleted");
        verify(deleteRequestedFor(urlEqualTo(BOOKSHELF_URI + "?userId=" + user.getId())));
        verify(deleteRequestedFor(urlEqualTo(REVIEW_URI + "/" + user.getId().toString() + "/" + user.getId())));
    }

    @Test
    public void testOnUserWasDeletedWithException() {
        AppUser user = new AppUser(new Username("username"), "email@email.com", new HashedPassword("password"));
        userRepository.save(user);

        stubFor(WireMock.delete(urlEqualTo(BOOKSHELF_URI + "?userId=" + user.getId().toString()))
                .willReturn(aResponse().withStatus(404)));
        stubFor(WireMock.delete(urlEqualTo(REVIEW_URI + "/" + user.getId().toString() + "/" + user.getId()))
                .willReturn(aResponse().withStatus(404)));

        outputCaptor.reset();

        assertThrows(RuntimeException.class, () ->
                userEventsListener.onUserWasDeleted(new UserWasDeletedEvent(user, user.getId())));

        assertThat(outputCaptor.toString().trim()).doesNotContain("Account of user with id "
                + user.getId().toString() + " was deleted");
        verify(deleteRequestedFor(urlEqualTo(BOOKSHELF_URI + "?userId=" + user.getId())));
    }

    @AfterAll
    public static void stop() {
        wireMockServer.stop();
    }
}
