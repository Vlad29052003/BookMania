package nl.tudelft.sem.template.authentication.domain.book;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.ArrayList;
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
    public void testEmptyConstructor() {
        Book test  = new Book();
        assertNotEquals(test, null);
    }

    @Test
    public void testConstructor() {
        assertNotEquals(book1, null);
        assertEquals("title1", book1.getTitle());
        assertEquals(List.of("Author1"), book1.getAuthors());
        assertEquals(List.of(Genre.DRAMA), book1.getGenres());
        assertEquals("description", book1.getDescription());
        assertEquals(155, book1.getNumPages());
        assertEquals(new ArrayList<>(), book1.getUsersWithBookAsFavorite());
    }

    @Test
    public void testConstructor2() {
        Book test = new Book("title", null, null, "desc", 255);
        assertNotEquals(test, null);
        assertEquals("title", test.getTitle());
        assertEquals(new ArrayList<>(), test.getAuthors());
        assertEquals(new ArrayList<>(), test.getGenres());
        assertEquals("desc", test.getDescription());
        assertEquals(255, test.getNumPages());
        assertEquals(new ArrayList<>(), test.getUsersWithBookAsFavorite());
    }

    @Test
    public void testEquals() {
        assertEquals(book1, book1);
        assertEquals(book1, book2);
        assertNotEquals(book1, book3);
        assertNotEquals(book1, null);
        assertFalse(book1.equals(new ArrayList<>()));
    }

    @Test
    public void testHash() {
        assertEquals(book1.hashCode(), book2.hashCode());
    }
}
