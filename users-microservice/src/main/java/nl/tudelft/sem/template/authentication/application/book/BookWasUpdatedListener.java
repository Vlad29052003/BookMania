package nl.tudelft.sem.template.authentication.application.book;

import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.book.BookWasCreatedEvent;
import nl.tudelft.sem.template.authentication.domain.book.BookWasDeletedEvent;
import nl.tudelft.sem.template.authentication.domain.book.BookWasEditedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * This event listener is automatically called when a domain entity is saved
 * which concerns books.
 */
@Component
public class BookWasUpdatedListener {

    private static final String bookshelfUri = "http://localhost:8081/a/catalog";
    private static final HttpClient client = HttpClient.newHttpClient();

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
                    .PUT(HttpRequest.BodyPublishers.noBody())
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

        System.out.println("Book (" + book.getTitle() + ") was deleted.");

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(bookshelfUri + "?bookId=" + book.getId()))
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();

            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
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
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Book (" + book.getTitle() + ") was edited.");
    }
}
