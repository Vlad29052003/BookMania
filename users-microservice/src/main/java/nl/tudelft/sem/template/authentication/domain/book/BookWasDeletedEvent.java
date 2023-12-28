package nl.tudelft.sem.template.authentication.domain.book;

import java.util.UUID;
import lombok.Getter;


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
