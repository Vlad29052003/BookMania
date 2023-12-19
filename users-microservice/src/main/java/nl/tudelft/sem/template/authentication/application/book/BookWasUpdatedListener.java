package nl.tudelft.sem.template.authentication.application.book;

import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.book.BookWasCreatedEvent;
import nl.tudelft.sem.template.authentication.domain.book.BookWasDeletedEvent;
import nl.tudelft.sem.template.authentication.domain.book.BookWasEditedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * This event listener is automatically called when a domain entity is saved
 * which concerns books.
 */
@Component
public class BookWasUpdatedListener {

    private final static String bookshelfUri = "http://localhost:8081/a";
    private final static WebClient wc = WebClient.create(bookshelfUri);

    /**
     * Event handler for creating books.
     *
     * @param event The event to react to
     */
    @EventListener
    public void onBookWasCreated(BookWasCreatedEvent event) {
        // Handler code here
        Book book = event.getBook();

        System.out.println("Book (" + book.getTitle() + ") was created.");

        wc.post().uri("/catalog").body(Mono.just(book), Book.class)
                .accept(MediaType.APPLICATION_JSON);

    }

    /**
     * Event handler for deleting books.
     *
     * @param event The event to react to
     */
    @EventListener
    public void onBookWasDeleted(BookWasDeletedEvent event) {
        Book book = event.getBook();

        System.out.println("Book (" + book.getTitle() + ") was deleted.");

        wc.delete().uri("/catalog?bookId=" + book.getId());
    }

    /**
     * Event handler for editing books.
     *
     * @param event The event to react to
     */
    @EventListener
    public void onBookWasEdited(BookWasEditedEvent event) {
        Book book = event.getBook();

        System.out.println("Book (" + book.getTitle() + ") was edited.");

        wc.put().uri("/catalog").body(Mono.just(book), Book.class)
                .accept(MediaType.APPLICATION_JSON);
    }
}
