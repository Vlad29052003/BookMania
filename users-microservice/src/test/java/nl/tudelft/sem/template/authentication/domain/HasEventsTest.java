package nl.tudelft.sem.template.authentication.domain;

import java.beans.Transient;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.book.BookWasCreatedEvent;
import nl.tudelft.sem.template.authentication.domain.book.BookWasDeletedEvent;
import nl.tudelft.sem.template.authentication.domain.book.BookWasEditedEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:application-test.properties")
class HasEventsTest {

    @Test
    @Transient
    public void recordThat() {
        Book book = new Book("title", null, null, "", 1);

        book.recordThat(book);

        List<Object> domainEvents = new ArrayList<>();
        domainEvents.add(book);

        Assertions.assertEquals(1, book.releaseEvents().size());
        Assertions.assertEquals(domainEvents, book.releaseEvents());
    }

    @Test
    @Transient
    public void recordThatViaBook() {
        Book book = new Book("title", null, null, "", 1);
        UUID id = UUID.randomUUID();

        book.recordBookWasCreated();
        book.recordBookWasEdited();
        book.recordBookWasDeleted(id);

        List<Object> domainEvents = new ArrayList<>();
        domainEvents.add(new BookWasCreatedEvent(book));
        domainEvents.add(new BookWasEditedEvent(book));
        domainEvents.add(new BookWasDeletedEvent(book, id));

        Assertions.assertEquals(3, book.releaseEvents().size());
        Assertions.assertEquals(domainEvents, book.releaseEvents());
    }

    @Test
    @Transient
    void clearEvents() {
        Book book = new Book("title", null, null, "", 1);

        book.recordThat(book);

        book.clearEvents();

        Assertions.assertEquals(0, book.releaseEvents().size());
    }

}