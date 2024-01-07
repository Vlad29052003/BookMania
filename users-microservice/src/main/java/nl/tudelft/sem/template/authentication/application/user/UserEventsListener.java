package nl.tudelft.sem.template.authentication.application.user;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import nl.tudelft.sem.template.authentication.domain.user.UserWasCreatedEvent;
import nl.tudelft.sem.template.authentication.domain.user.UserWasDeletedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * This event listener is automatically called when a user entity is saved
 * which has stored events of type: UserWasCreated.
 */
@Component
public class UserEventsListener {

    private final transient HttpClient client = HttpClient.newHttpClient();
    private final transient ObjectMapper objectMapper = new ObjectMapper();

    private static WireMockServer wireMockServer;
    public static String BOOKSHELF_URL = "http://localhost:8080/a/user";

    public static String REVIEW_URL = "http://localhost:8080/b/user";


    /**
     * Constructor for the UserEventsListener.
     */
    public void init() {
        wireMockServer = new WireMockServer(new WireMockConfiguration().port(8080));
        wireMockServer.start();

        configureFor("localhost", 8080);
    }

    /**
     * Event handler for account creation.
     *
     * @param event The event to react to.
     */
    @EventListener
    public void onUserWasCreated(UserWasCreatedEvent event) {
        init();
        UUID id = event.getUser().getId();

        stubFor(WireMock.post(urlEqualTo("/a/user"))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse().withStatus(200)));

        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BOOKSHELF_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(id)))
                .build();

            HttpResponse<?> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != HttpStatus.OK.value()) {
                throw new ResponseStatusException(HttpStatus.valueOf(response.statusCode()));
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Account of user with id " + id + " was created.");

        wireMockServer.stop();
    }

    /**
     * Event handler for account deletion.
     *
     * @param event The event to react to.
     */
    @EventListener
    public void onUserWasDeleted(UserWasDeletedEvent event) {
        init();
        UUID id = event.getUser().getId();
        UUID adminId = event.getAdminId();

        stubFor(WireMock.delete(urlEqualTo("/a/user?userId=" + id.toString()))
                .willReturn(aResponse().withStatus(200)));
        stubFor(WireMock.delete(urlEqualTo("/b/user/" + id + "/" + adminId.toString()))
                .willReturn(aResponse().withStatus(200)));
        stubFor(WireMock.delete(urlEqualTo("/b/user/" + id + "/" + id))
                .willReturn(aResponse().withStatus(200)));

        try {
            HttpRequest bookShelfRequest = HttpRequest.newBuilder()
                .uri(URI.create(BOOKSHELF_URL + "?userId=" + id))
                .DELETE()
                .build();

            HttpResponse<?> response = client.send(bookShelfRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != HttpStatus.OK.value()) {
                throw new ResponseStatusException(HttpStatus.valueOf(response.statusCode()));
            }

            HttpRequest reviewRequest = HttpRequest.newBuilder()
                .uri(URI.create(REVIEW_URL + "/" + id + "/" + adminId))
                .DELETE()
                .build();

            response = client.send(reviewRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != HttpStatus.OK.value()) {
                throw new ResponseStatusException(HttpStatus.valueOf(response.statusCode()));
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Account of user with id " + id + " was deleted.");

        wireMockServer.stop();
    }
}
