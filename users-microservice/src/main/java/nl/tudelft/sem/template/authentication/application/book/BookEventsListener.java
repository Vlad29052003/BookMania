package nl.tudelft.sem.template.authentication.application.book;

import static nl.tudelft.sem.template.authentication.application.Constants.BOOKSHELF_SERVER;
import static nl.tudelft.sem.template.authentication.application.Constants.REVIEW_SERVER;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.book.BookWasCreatedEvent;
import nl.tudelft.sem.template.authentication.domain.book.BookWasDeletedEvent;
import nl.tudelft.sem.template.authentication.domain.book.BookWasEditedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * This event listener is automatically called when a domain entity is saved
 * which concerns books.
 */
@Component
public class BookEventsListener {

    // Have everything on one port for now, since nothing on port 8080/8082 exist for now.
    public static String BOOKSHELF_URI = BOOKSHELF_SERVER + "/catalog";
    public static String REVIEW_URI = REVIEW_SERVER + "/book";
    private final transient HttpClient client = HttpClient.newHttpClient();
    private final transient ObjectMapper mapper = new ObjectMapper();

    /**
     * Event handler for creating books.
     *
     * @param event The event to react to
     * @throws RuntimeException        in the case that the request is not sent correctly
     * @throws ResponseStatusException if the request is not received as expected
     */
    @EventListener
    public void onBookWasCreated(BookWasCreatedEvent event) {
        // Handler code here
        Book book = event.getBook();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BOOKSHELF_URI))
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(book)))
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
            System.out.println("unauth caught");
            if (!rse.getStatus().equals(HttpStatus.UNAUTHORIZED)) {
                throw new ResponseStatusException(rse.getStatus());
            }
        }
        System.out.println("Book (id: " + book.getId() + ", title: " + book.getTitle() + ") was created.");

    }

    /**
     * Event handler for deleting books.
     *
     * @param event The event to react to
     * @throws RuntimeException        in the case that the request is not sent correctly
     * @throws ResponseStatusException if the request is not received as expected
     */
    public void onBookWasDeleted(BookWasDeletedEvent event) {
        Book book = event.getBook();
        try {
            HttpRequest requestBookshelf = HttpRequest.newBuilder()
                    .uri(URI.create(BOOKSHELF_URI + "?bookId=" + book.getId()))
                    .DELETE()
                    .build();

            HttpResponse<?> response = client.send(requestBookshelf, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != HttpStatus.OK.value()) {
                throw new ResponseStatusException(HttpStatus.valueOf(response.statusCode()));
            }

            HttpRequest requestReview = HttpRequest.newBuilder()
                    .uri(URI.create(REVIEW_URI + "/" + book.getId().toString() + "/" + event.getUserId().toString()))
                    .DELETE()
                    .build();

            response = client.send(requestReview, HttpResponse.BodyHandlers.ofString());
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

        System.out.println("Book (id: " + book.getId() + ", title: " + book.getTitle() + ") was deleted.");
    }

    /**
     * Event handler for editing books.
     *
     * @param event The event to react to
     * @throws RuntimeException        in the case that the request is not sent correctly
     * @throws ResponseStatusException if the request is not received as expected
     */
    @EventListener
    public void onBookWasEdited(BookWasEditedEvent event) {
        Book book = event.getBook();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BOOKSHELF_URI))
                    .PUT(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(book)))
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

        System.out.println("Book (id: " + book.getId() + ", title: " + book.getTitle() + ") was edited.");
    }
}
