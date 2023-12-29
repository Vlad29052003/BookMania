package nl.tudelft.sem.template.authentication.domain.book;

import java.util.Objects;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BookWasEditedEvent that = (BookWasEditedEvent) o;
        return Objects.equals(book, that.book);
    }

    @Override
    public int hashCode() {
        return Objects.hash(book);
    }
}
