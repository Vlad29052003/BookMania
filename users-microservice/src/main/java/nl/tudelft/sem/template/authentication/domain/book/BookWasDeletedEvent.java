package nl.tudelft.sem.template.authentication.domain.book;

import java.util.Objects;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BookWasDeletedEvent that = (BookWasDeletedEvent) o;
        return Objects.equals(book, that.book) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(book, userId);
    }
}
