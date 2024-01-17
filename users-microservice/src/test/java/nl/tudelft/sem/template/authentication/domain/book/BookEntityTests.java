package nl.tudelft.sem.template.authentication.domain.book;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import nl.tudelft.sem.template.authentication.models.BookshelfBookModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
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
        assertThat(test).isNotEqualTo(null);
    }

    @Test
    public void testConstructor() {
        assertThat(book1).isNotEqualTo(null);
        assertThat(book1.getTitle()).isEqualTo("title1");
        assertThat(book1.getAuthors()).isEqualTo(List.of("Author1"));
        assertThat(book1.getGenre()).isEqualTo(List.of(Genre.DRAMA));
        assertThat(book1.getDescription()).isEqualTo("description");
        assertThat(book1.getNumPages()).isEqualTo(155);
        assertThat(book1.getUsersWithBookAsFavorite()).isEqualTo(new ArrayList<>());
    }

    @Test
    public void testReviewBookModel() {
        BookshelfBookModel bookshelfBookModel = new BookshelfBookModel(book1);
        assertThat(bookshelfBookModel.getBookId()).isEqualTo(book1.getId());
        assertThat(bookshelfBookModel.getTitle()).isEqualTo(book1.getTitle());
        assertThat(bookshelfBookModel.getAuthors()).isEqualTo(book1.getAuthors());
        assertThat(bookshelfBookModel.getGenres()).isEqualTo(book1.getGenre());
        assertThat(bookshelfBookModel.getDescription()).isEqualTo(book1.getDescription());
        assertThat(bookshelfBookModel.getNumPages()).isEqualTo(book1.getNumPages());
    }

    @Test
    public void testConstructor2() {
        Book test = new Book("title", null, null, "desc", 255);
        assertThat(test).isNotEqualTo(null);
        assertThat(test.getTitle()).isEqualTo("title");
        assertThat(test.getAuthors()).isEqualTo(new ArrayList<>());
        assertThat(test.getGenre()).isEqualTo(new ArrayList<>());
        assertThat(test.getDescription()).isEqualTo("desc");
        assertThat(test.getNumPages()).isEqualTo(255);
        assertThat(test.getUsersWithBookAsFavorite()).isEqualTo(new ArrayList<>());
    }

    @Test
    public void testEquals() {
        assertThat(book1).isEqualTo(book1);
        assertThat(book1).isEqualTo(book2);
        assertThat(book1).isNotEqualTo(book3);
        assertThat(book1).isNotEqualTo(null);
        assertThat(book1).isNotEqualTo(new ArrayList<>());
    }

    @Test
    public void testHash() {
        assertThat(book1.hashCode()).isEqualTo(book2.hashCode());
        assertThat(book1.hashCode()).isEqualTo(Objects.hash(book1.getId()));
    }

    @Test
    public void testToString() {
        assertThat(book1.toString()).isNotEqualTo(book3.toString());
        assertThat(book1.toString()).isEqualTo(book1.toString());
    }
}
