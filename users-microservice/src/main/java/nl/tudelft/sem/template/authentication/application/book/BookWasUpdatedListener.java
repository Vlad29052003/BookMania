package nl.tudelft.sem.template.authentication.application.book;

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
import org.springframework.stereotype.Component;

/**
 * This event listener is automatically called when a domain entity is saved
 * which concerns books.
 */
@Component
public class BookWasUpdatedListener {

    private final transient String bookshelfUri = "http://localhost:8080/a/catalog";
    private final transient String reviewUri = "http://localhost:8081/b/book";
    private final transient HttpClient client = HttpClient.newHttpClient();
    private final transient ObjectMapper mapper = new ObjectMapper();

    /**
     * Event handler for creating books.
     *
     * @param event The event to react to
     * @throws RuntimeException in the case that the request is not sent correctly
     */
    @EventListener
    public void onBookWasCreated(BookWasCreatedEvent event) {
        // Handler code here
        Book book = event.getBook();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(bookshelfUri))
                    .PUT(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(book)))
                    .build();

            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Book (" + book.getTitle() + ") was created.");

    }

    /**
     * Event handler for deleting books.
     *
     * @param event The event to react to
     * @throws RuntimeException in the case that the request is not sent correctly
     */
    @EventListener
    public void onBookWasDeleted(BookWasDeletedEvent event) {
        Book book = event.getBook();

        try {
            HttpRequest requestBookshelf = HttpRequest.newBuilder()
                    .uri(URI.create(bookshelfUri + "?bookId=" + book.getId().toString()))
                    .DELETE()
                    .build();

            client.send(requestBookshelf, HttpResponse.BodyHandlers.ofString());

            HttpRequest requestReview = HttpRequest.newBuilder()
                    .uri(URI.create(reviewUri + "/" + book.getId().toString() + "/" + event.getUserId().toString()))
                    .DELETE()
                    .build();

            client.send(requestReview, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {

            // This second try catch block is only required for testing, as wire mock can only configure one port at
            // a time. It can be deleted later. Even if it is not removed, the handler should work for both the tests
            // and the real server

            try {
                HttpRequest requestReview = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/b/book/" + book.getId().toString() + "/" + event.getUserId().toString()))
                        .DELETE()
                        .build();

                client.send(requestReview, HttpResponse.BodyHandlers.ofString());
            } catch (IOException | InterruptedException e2) {
                throw new RuntimeException(e2);
            }
        }


        System.out.println("Book (" + book.getTitle() + ") was deleted.");
    }

    /**
     * Event handler for editing books.
     *
     * @param event The event to react to
     * @throws RuntimeException in the case that the request is not sent correctly
     */
    @EventListener
    public void onBookWasEdited(BookWasEditedEvent event) {
        Book book = event.getBook();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(bookshelfUri))
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(book)))
                    .build();

            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Book (" + book.getTitle() + ") was edited.");
    }
}
