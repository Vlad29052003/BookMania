package nl.tudelft.sem.template.authentication.application.user;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import nl.tudelft.sem.template.authentication.domain.user.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.beans.Transient;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.post;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UserEventsListenerTests {

    private final transient UserEventsListener userEventsListener = new UserEventsListener();
    private transient AuthenticationService authenticationService;

    @Autowired
    private transient UserRepository userRepository;

    private static final String BOOKSHELF_URI = "/a/user";

    private static WireMockServer wireMockServer;

    private static ByteArrayOutputStream outputCaptor;

    @BeforeAll
    public static void setUp() {
        wireMockServer = new WireMockServer(new WireMockConfiguration().port(8080));
        wireMockServer.start();
        WireMock.configureFor("localhost", 8080);

        outputCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputCaptor));

        UserEventsListener.BOOKSHELF_URL = "http://localhost:8080/a/user";
    }

    @Test
    public void testOnUserWasCreated() {
        AppUser user = new AppUser(new Username("username"), "email", new HashedPassword("password"));
        userRepository.save(user);

        stubFor(WireMock.post(urlEqualTo(BOOKSHELF_URI))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse().withStatus(200)));

        outputCaptor.reset();

        userEventsListener.onUserWasCreated(new UserWasCreatedEvent(user));

        assertThat(outputCaptor.toString().trim()).contains("Account of user with id ");

        WireMock.verify(postRequestedFor(urlEqualTo(BOOKSHELF_URI))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(containing(user.getId().toString())));
    }

    @Test
    public void testOnUserWasCreatedWithException() {
        AppUser user = new AppUser(new Username("username"), "email", new HashedPassword("password"));
        userRepository.save(user);

        stubFor(WireMock.post(urlEqualTo(BOOKSHELF_URI))
                .willReturn(aResponse().withStatus(404)));

        outputCaptor.reset();

        assertThrows(RuntimeException.class, () -> userEventsListener.onUserWasCreated(new UserWasCreatedEvent(user)));

        assertThat(outputCaptor.toString().trim()).doesNotContain("Account of user with id ");

        WireMock.verify(postRequestedFor(urlEqualTo(BOOKSHELF_URI))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(containing(user.getId().toString())));
    }

    @Test
    public void testOnUserWasDeleted() {
        AppUser user = new AppUser(new Username("username"), "email", new HashedPassword("password"));
        userRepository.save(user);

        stubFor(WireMock.delete(urlEqualTo(BOOKSHELF_URI + "?userId=" + user.getId().toString()))
                .willReturn(aResponse().withStatus(200)));

        outputCaptor.reset();

        userEventsListener.onUserWasDeleted(new UserWasDeletedEvent(user));

        assertThat(outputCaptor.toString().trim()).contains("Account of user with id " + user.getId().toString() + " was deleted");

        WireMock.verify(deleteRequestedFor(urlEqualTo(BOOKSHELF_URI + "?userId=" + user.getId()))
                .withQueryParam("userId", equalTo(user.getId().toString())));
    }

    @Test
    public void testOnUserWasDeletedWithException() {
        AppUser user = new AppUser(new Username("username"), "email", new HashedPassword("password"));
        userRepository.save(user);

        stubFor(WireMock.delete(urlEqualTo(BOOKSHELF_URI + "?userId=" + user.getId().toString()))
                .willReturn(aResponse().withStatus(404)));

        outputCaptor.reset();

        assertThrows(RuntimeException.class, () -> userEventsListener.onUserWasDeleted(new UserWasDeletedEvent(user)));

        assertThat(outputCaptor.toString().trim()).doesNotContain("Account of user with id " + user.getId().toString() + " was deleted");

        WireMock.verify(deleteRequestedFor(urlEqualTo(BOOKSHELF_URI + "?userId=" + user.getId()))
                .withQueryParam("userId", equalTo(user.getId().toString())));
    }

    @AfterAll
    public static void stop() {
        wireMockServer.stop();
    }
}
