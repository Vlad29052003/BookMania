package nl.tudelft.sem.template.authentication.application.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import nl.tudelft.sem.template.authentication.domain.user.UserWasCreatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * This event listener is automatically called when a user entity is saved
 * which has stored events of type: UserWasCreated.
 */
@Component
public class UserWasCreatedListener {

    private final transient HttpClient client = HttpClient.newHttpClient();
//    private final transient ObjectMapper objectMapper = new ObjectMapper();

//    private static WireMockServer wireMockServer;
    private static final String BOOKSHELF_URL = "http://localhost:8080/a/user";

//    private static final String REVIEW_URL = "http://localhost:8080/b/user";

//    /**
//     * Constructor for the UserWasCreatedListener.
//     */
//    public void setUp() {
//        wireMockServer = new WireMockServer(new WireMockConfiguration().port(8080));
//        wireMockServer.start();
//        configureFor("localhost", 8080);
//    }

    /**
     * Event handler for account creation.
     *
     * @param event The event to react to.
     */
    @EventListener
    public void onAccountWasCreated(UserWasCreatedEvent event) {
        UUID id = event.getUser().getId();

//        stubFor(post(BOOKSHELF_URL)
//                .withHeader("Content-Type", equalTo("application/json"))
//                .withRequestBody(equalTo(id.toString()))
//                .willReturn(aResponse()
//                        .withStatus(200)));

        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BOOKSHELF_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(id.toString()))
                .build();

            HttpResponse<?> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != HttpStatus.OK.value()) {
                throw new ResponseStatusException(HttpStatus.valueOf(response.statusCode()));
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ResponseStatusException rse) {
            // Since we do not have the other microservices locally yet, the http client will always throw an
            // unauthorized exception. Thus, we will filter it out for now.

            if (!rse.getStatus().equals(HttpStatus.UNAUTHORIZED)) {
                throw new ResponseStatusException(rse.getStatus());
            }
        }


        System.out.println("Account of user with id " + id + " was created.");

        //        wireMockServer.stop();
    }
}
