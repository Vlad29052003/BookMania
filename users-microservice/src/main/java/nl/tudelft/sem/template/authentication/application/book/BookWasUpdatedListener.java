package nl.tudelft.sem.template.authentication.application.book;

import nl.tudelft.sem.template.authentication.domain.book.BookWasCreatedEvent;
import nl.tudelft.sem.template.authentication.domain.book.BookWasDeletedEvent;
import nl.tudelft.sem.template.authentication.domain.book.BookWasEditedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * This event listener is automatically called when a domain entity is saved
 * which has stored events of type: BookWasCreated.
 */
@Component
public class BookWasUpdatedListener {
    /**
     * The name of the function indicated which event is listened to.
     * The format is onEVENTNAME.
     *
     * @param event The event to react to
     */
    @EventListener
    public void onBookWasCreated(BookWasCreatedEvent event) {
        // Handler code here
        System.out.println("Book (" + event.getBook().getTitle()+ ") was created.");

    }

    @EventListener
    public void onBookWasDeleted(BookWasDeletedEvent event) {
        System.out.println("Book (" + event.getBook().getTitle()+ ") was edited.");
    }

    @EventListener
    public void onBookWasEdited(BookWasEditedEvent event) {
        System.out.println("Book (" + event.getBook().getTitle()+ ") was deleted.");
    }
}
