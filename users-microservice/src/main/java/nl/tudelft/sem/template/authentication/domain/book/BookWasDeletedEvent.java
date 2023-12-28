package nl.tudelft.sem.template.authentication.domain.book;

import lombok.Getter;

import java.util.UUID;


/**
 * A DDD domain event that indicated a book was deleted.
 */
@Getter
public class BookWasDeletedEvent {

    private final transient Book book;
    private final transient UUID userId;

    public BookWasDeletedEvent(Book book, UUID userId) {
        this.book = book;
        this.userId = userId;
    }
}
