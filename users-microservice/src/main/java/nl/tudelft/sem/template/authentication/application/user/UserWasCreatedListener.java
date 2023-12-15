package nl.tudelft.sem.template.authentication.application.user;

//import com.github.tomakehurst.wiremock.WireMockServer;
import nl.tudelft.sem.template.authentication.domain.user.UserWasCreatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

//import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * This event listener is automatically called when an user entity is saved
 * which has stored events of type: UserWasCreated.
 */
@Component
public class UserWasCreatedListener {

//    private static final HttpClient client = HttpClient.newHttpClient();
//    private static final String bookshelvesUrl = "http://localhost:8081/a/user";
//
//    private WireMockServer wireMockServer = new WireMockServer(options().withPort(8081)));
//
//    /**
//     * On account creation call the endpoint of the bookshelves microservice to create an user.
//     *
//     * @param event The event to react to
//     */
//    @EventListener
//    public void onAccountWasCreated(UserWasCreatedEvent event) {
//        stubFor(post(bookshelvesUrl)
//                .withHeader("Content-Type", equalTo("application/json"))
//                .withRequestBody(equalTo(event.getId().toString()))
//                .willReturn(aResponse()
//                        .withStatus(200)));
//
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create(bookshelvesUrl))
//                .header("Content-Type", "application/json")
//                .POST(HttpRequest.BodyPublishers.ofString(event.getId().toString()))
//                .build();
//
//        try {
//            client.send(request, HttpResponse.BodyHandlers.ofString());
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
}
