package nl.tudelft.sem.template.authentication.application.user;

import static nl.tudelft.sem.template.authentication.application.Constants.BOOKSHELF_SERVER;
import static nl.tudelft.sem.template.authentication.application.Constants.REVIEW_SERVER;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    public static String BOOKSHELF_URL = BOOKSHELF_SERVER + "/user";
    public static String REVIEW_URL = REVIEW_SERVER + "/user";


    /**
     * Event handler for account creation.
     *
     * @param event The event to react to.
     */
    @EventListener
    public void onUserWasCreated(UserWasCreatedEvent event) {
        UUID id = event.getUser().getId();

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
        } catch (ResponseStatusException rse) {
            // Since we do not have the other microservices locally yet, the http client will always throw an
            // unauthorized exception. Thus, we will filter it out for now.

            if (!rse.getStatus().equals(HttpStatus.UNAUTHORIZED)) {
                throw new ResponseStatusException(rse.getStatus());
            }
        }

        System.out.println("Account of user with id " + id + " was created.");
    }

    /**
     * Event handler for account deletion.
     *
     * @param event The event to react to.
     */
    @EventListener
    public void onUserWasDeleted(UserWasDeletedEvent event) {
        UUID id = event.getUser().getId();

        try {
            HttpRequest bookShelfRequest = HttpRequest.newBuilder()
                    .uri(URI.create(BOOKSHELF_URL + "?userId=" + id))
                    .DELETE()
                    .build();

            HttpResponse<?> response = client.send(bookShelfRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != HttpStatus.OK.value()) {
                throw new ResponseStatusException(HttpStatus.valueOf(response.statusCode()));
            }

            UUID adminId = event.getAdminId();

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
        } catch (ResponseStatusException rse) {
            // Since we do not have the other microservices locally yet, the http client will always throw an
            // unauthorized exception. Thus, we will filter it out for now.

            if (!rse.getStatus().equals(HttpStatus.UNAUTHORIZED)) {
                throw new ResponseStatusException(rse.getStatus());
            }
        }

        System.out.println("Account of user with id " + id + " was deleted.");
    }
}
