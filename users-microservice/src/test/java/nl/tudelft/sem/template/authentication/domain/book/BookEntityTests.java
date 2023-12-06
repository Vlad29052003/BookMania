package nl.tudelft.sem.template.authentication.domain.book;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BookEntityTests {
    private transient Book book1;
    private transient Book book2;
    private transient Book book3;

    /**
     * Sets up the testing environment.
     */
    @BeforeEach
    public void setUp() {
        this.book1 = new Book("title1", List.of("Author1"), List.of(Genre.DRAMA), "description", 155);
        this.book2 = new Book("title2", List.of("Author2"), List.of(Genre.CRIME), "", 87);
        this.book3 = new Book("title1", List.of("Author1"), List.of(Genre.DRAMA), "description", 271);

        UUID id1 = UUID.randomUUID();
        UUID id3 = UUID.randomUUID();
        while (id3.equals(id1)) {
            id3 = UUID.randomUUID();
        }

        this.book1.setId(id1);
        this.book2.setId(id1);
        this.book3.setId(id3);
    }

    @Test
    public void testEquals() {
        assertEquals(book1, book1);
        assertEquals(book1, book2);
        assertNotEquals(book1, book3);
        assertNotEquals(book1, null);
    }

    @Test
    public void testHash() {
        assertEquals(book1.hashCode(), book2.hashCode());
    }
}
