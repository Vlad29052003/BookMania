package nl.tudelft.sem.template.authentication.domain.book;

import lombok.Getter;


/**
 * A DDD domain event that indicated a book was edited.
 */
@Getter
public class BookWasEditedEvent {

    private final transient Book book;

    public BookWasEditedEvent(Book book) {
        this.book = book;
    }
}
