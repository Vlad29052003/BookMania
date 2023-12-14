package nl.tudelft.sem.template.authentication.domain.book;

import lombok.Getter;


/**
 * A DDD domain event that indicated a book was created.
 */
@Getter
public class BookWasCreatedEvent {

    private final transient Book book;

    public BookWasCreatedEvent(Book book) {
        this.book = book;
    }
}
