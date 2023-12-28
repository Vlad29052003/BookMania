package nl.tudelft.sem.template.authentication.domain.book;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

        BookWasCreatedEvent bookWasCreatedEvent = new BookWasCreatedEvent(book);

        assertEquals(book, bookWasCreatedEvent.getBook());
    }

    @Test
    public void testEditEvent() {
        Book book = new Book("title", null, null, "", 1);

        BookWasEditedEvent bookEvent = new BookWasEditedEvent(book);

        assertEquals(book, bookEvent.getBook());
    }

    @Test
    public void testDeleteEvent() {
        Book book = new Book("title", null, null, "", 1);
        UUID id = UUID.randomUUID();

        BookWasDeletedEvent bookEvent = new BookWasDeletedEvent(book, id);

        assertEquals(book, bookEvent.getBook());
        assertEquals(id, bookEvent.getUserId());
    }
}