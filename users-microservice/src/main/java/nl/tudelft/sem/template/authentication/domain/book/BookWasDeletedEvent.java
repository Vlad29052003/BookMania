package nl.tudelft.sem.template.authentication.domain.book;

import lombok.Getter;


/**
 * A DDD domain event that indicated a book was deleted.
 */
@Getter
public class BookWasDeletedEvent {

    private final transient Book book;

    public BookWasDeletedEvent(Book book) {
        this.book = book;
    }
}
