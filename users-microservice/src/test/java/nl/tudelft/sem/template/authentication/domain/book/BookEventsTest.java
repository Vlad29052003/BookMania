package nl.tudelft.sem.template.authentication.domain.book;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class BookEventsTest {
    @Test
    public void testCreateEvent() {
        Book book = new Book("title", null, null, "", 1);
        book.setId(UUID.randomUUID());

        BookWasCreatedEvent bookWasCreatedEvent = new BookWasCreatedEvent(book);

        Book book2 = new Book("title2", null, null, "", 3);
        book2.setId(UUID.randomUUID());

        while (book.getId().equals(book2.getId())) {
            book2.setId(UUID.randomUUID());
        }

        BookWasCreatedEvent bookWasCreatedEvent2 = new BookWasCreatedEvent(book2);

        assertNotEquals(bookWasCreatedEvent, bookWasCreatedEvent2);
        assertThat(bookWasCreatedEvent).isNotEqualTo(new Object());
        assertThat(bookWasCreatedEvent).isNotEqualTo(null);

        assertEquals(Objects.hash(book), bookWasCreatedEvent.hashCode());

        assertThat(bookWasCreatedEvent).isEqualTo(bookWasCreatedEvent);
    }

    @Test
    public void testEditEvent() {
        Book book = new Book("title", null, null, "", 1);
        book.setId(UUID.randomUUID());

        BookWasEditedEvent bookEvent = new BookWasEditedEvent(book);

        Book book2 = new Book("title2", null, null, "", 21);
        book2.setId(UUID.randomUUID());

        while (book.getId().equals(book2.getId())) {
            book2.setId(UUID.randomUUID());
        }

        BookWasEditedEvent bookEvent2 = new BookWasEditedEvent(book2);

        assertNotEquals(bookEvent, bookEvent2);
        assertThat(bookEvent).isNotEqualTo(new Object());
        assertThat(bookEvent).isNotEqualTo(null);

        assertThat(bookEvent).isEqualTo(bookEvent);

        assertEquals(Objects.hash(book), bookEvent.hashCode());
    }

    @Test
    public void testDeleteEvent() {
        Book book = new Book("title", null, null, "", 1);
        book.setId(UUID.randomUUID());
        UUID id = UUID.randomUUID();

        Book book2 = new Book("title2", null, null, "", 1);
        book2.setId(UUID.randomUUID());
        UUID id2 = UUID.randomUUID();

        while (id2.equals(id)) {
            id2 = UUID.randomUUID();
        }

        while (book.getId().equals(book2.getId())) {
            book2.setId(UUID.randomUUID());
        }

        BookWasDeletedEvent bookEvent = new BookWasDeletedEvent(book, id);

        BookWasDeletedEvent bookEvent2 = new BookWasDeletedEvent(book, id2);

        assertNotEquals(bookEvent, bookEvent2);
        assertThat(bookEvent).isNotEqualTo(new Object());
        assertThat(bookEvent).isNotEqualTo(null);

        assertThat(bookEvent).isEqualTo(bookEvent);

        BookWasDeletedEvent bookEvent3 = new BookWasDeletedEvent(book, id);
        BookWasDeletedEvent bookEvent4 = new BookWasDeletedEvent(new Book(), id);

        assertThat(bookEvent).isEqualTo(bookEvent3);
        assertThat(bookEvent).isNotEqualTo(bookEvent4);

        assertEquals(Objects.hash(book, id), bookEvent.hashCode());
    }
}