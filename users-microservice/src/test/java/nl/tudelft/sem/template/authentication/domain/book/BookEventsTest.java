package nl.tudelft.sem.template.authentication.domain.book;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

        while (book.getId().equals(book2.getId())) { book2.setId(UUID.randomUUID()); }

        BookWasCreatedEvent bookWasCreatedEvent2 = new BookWasCreatedEvent(book2);

        assertNotEquals(bookWasCreatedEvent, bookWasCreatedEvent2);
        assertNotEquals(null, bookWasCreatedEvent2);
        assertNotEquals(new Object(), bookWasCreatedEvent2);

        assertEquals(book, bookWasCreatedEvent.getBook());
        assertEquals(book, book);

        assertEquals(Objects.hash(book), bookWasCreatedEvent.hashCode());
    }

    @Test
    public void testEditEvent() {
        Book book = new Book("title", null, null, "", 1);
        book.setId(UUID.randomUUID());

        BookWasEditedEvent bookEvent = new BookWasEditedEvent(book);

        Book book2 = new Book("title2", null, null, "", 21);
        book2.setId(UUID.randomUUID());

        while (book.getId().equals(book2.getId())) { book2.setId(UUID.randomUUID()); }

        BookWasEditedEvent bookEvent2 = new BookWasEditedEvent(book2);

        assertNotEquals(bookEvent, bookEvent2);
        assertFalse(bookEvent.equals(null));
        assertNotEquals(bookEvent, new Object());

        assertEquals(book, bookEvent.getBook());
        assertEquals(book, book);

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

        while (id2.equals(id)) { id2 = UUID.randomUUID(); }
        while (book.getId().equals(book2.getId())) { book2.setId(UUID.randomUUID()); }

        BookWasDeletedEvent bookEvent = new BookWasDeletedEvent(book, id);

        BookWasDeletedEvent bookEvent2 = new BookWasDeletedEvent(book, id2);

        assertNotEquals(bookEvent, bookEvent2);
        assertNotEquals(null, bookEvent2);
        assertNotEquals(new Object(), bookEvent2);

        assertEquals(book, bookEvent.getBook());
        assertEquals(id, bookEvent.getUserId());
        assertEquals(book, book);

        assertEquals(Objects.hash(book, id), bookEvent.hashCode());
    }
}