package nl.tudelft.sem.template.authentication.domain.book;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.util.List;
import java.util.UUID;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.HashedPassword;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import nl.tudelft.sem.template.authentication.domain.user.UserService;
import nl.tudelft.sem.template.authentication.domain.user.Username;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class BookServiceTests {
    @Autowired
    private transient BookRepository bookRepository;
    @Autowired
    private transient UserRepository userRepository;
    @Autowired
    private transient BookService bookService;
    @Autowired
    private transient UserService userService;
    private transient UUID bookId;
    private transient Book book;

    /**
     * Sets up the testing environment.
     */
    @BeforeEach
    public void setUp() {
        this.book = new Book("title", List.of("Author1", "authorName"),
                List.of(Genre.CRIME, Genre.DRAMA), "description", 257);
        bookRepository.saveAndFlush(book);
        bookId = bookRepository.findByTitle("title").get(0).getId();

        Book book2 = new Book("title2", List.of("Author2"),
                List.of(Genre.CRIME), "testDescription", 550);
        bookRepository.saveAndFlush(book2);
    }

    @Test
    public void testGet() {
        assertThat(bookService.getBook(bookId.toString())).isEqualTo(book);
    }

    @Test
    public void testGetNotFound() {
        UUID randomUuid = UUID.randomUUID();
        while (randomUuid.equals(bookId)) {
            randomUuid = UUID.randomUUID();
        }
        UUID finalRandomUuid = randomUuid;
        assertThatThrownBy(() -> bookService.getBook(finalRandomUuid.toString()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("404 NOT_FOUND \"The book does not exist!\"");
    }

    @Test
    @Transactional
    public void testAddBook() {
        Book addBook = new Book("title new", List.of("Author2", "Author3"),
                List.of(Genre.SCIENCE), "description", 876);
        Book newBook = new Book("title new", List.of("Author2", "Author3"),
                List.of(Genre.SCIENCE), "description", 876);
        bookService.addBook(addBook);
        Book addedBook = bookRepository.findByTitle("title new").get(0);

        assertThat(newBook.getTitle()).isEqualTo(addedBook.getTitle());
        assertThat(newBook.getAuthors()).isEqualTo(addedBook.getAuthors());
        assertThat(newBook.getGenres()).isEqualTo(addedBook.getGenres());
        assertThat(newBook.getDescription()).isEqualTo(addedBook.getDescription());
        assertThat(newBook.getNumPages()).isEqualTo(addedBook.getNumPages());

        assertThatThrownBy(() -> bookService.addBook(addBook))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("409 CONFLICT \"The book is already in the system!\"");
    }

    @Test
    @Transactional
    public void testUpdateBookNotExisting() {
        UUID randomUuid = UUID.randomUUID();
        while (randomUuid.equals(bookId)) {
            randomUuid = UUID.randomUUID();
        }
        UUID finalRandomUuid = randomUuid;
        Book updatedBook = new Book();
        updatedBook.setId(finalRandomUuid);
        updatedBook.setTitle("title");
        updatedBook.setAuthors(List.of("Author1"));
        updatedBook.setGenres(List.of(Genre.SCIENCE));
        updatedBook.setDescription("desc");
        updatedBook.setNumPages(876);

        assertThatThrownBy(() -> bookService.updateBook(updatedBook))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("404 NOT_FOUND \"The book does not exist!\"");
    }

    @Test
    @Transactional
    public void testUpdateBook() {
        Book updatedBook = new Book();
        updatedBook.setId(bookId);
        updatedBook.setTitle("title new");
        updatedBook.setAuthors(List.of("Author1"));
        updatedBook.setGenres(List.of(Genre.SCIENCE));
        updatedBook.setDescription("desc");
        updatedBook.setNumPages(876);
        bookService.updateBook(updatedBook);
        Book updatedBookTest = bookRepository.findByTitle("title new").get(0);

        assertThat(updatedBookTest.getId()).isEqualTo(updatedBook.getId());
        assertThat(updatedBookTest.getTitle()).isEqualTo(updatedBook.getTitle());
        assertThat(updatedBookTest.getAuthors().containsAll(updatedBook.getAuthors())).isTrue();
        assertThat(updatedBookTest.getGenres().containsAll(updatedBook.getGenres())).isTrue();
        assertThat(updatedBookTest.getDescription()).isEqualTo(updatedBook.getDescription());
        assertThat(updatedBookTest.getNumPages()).isEqualTo(updatedBook.getNumPages());
    }

    @Test
    @Transactional
    public void testUpdateBookNullListAuthors() {
        Book updatedBook = new Book();
        updatedBook.setId(bookId);
        updatedBook.setTitle("title new");
        updatedBook.setAuthors(null);
        updatedBook.setGenres(List.of(Genre.CRIME));
        updatedBook.setDescription("desc");
        updatedBook.setNumPages(876);

        assertThatThrownBy(() -> bookService.updateBook(updatedBook))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("400 BAD_REQUEST \"The authors and genres cannot be null!\"");
    }

    @Test
    @Transactional
    public void testUpdateBookNullListGenres() {
        Book updatedBook = new Book();
        updatedBook.setId(bookId);
        updatedBook.setTitle("title new");
        updatedBook.setAuthors(List.of("Author1"));
        updatedBook.setGenres(null);
        updatedBook.setDescription("desc");
        updatedBook.setNumPages(876);

        assertThatThrownBy(() -> bookService.updateBook(updatedBook))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("400 BAD_REQUEST \"The authors and genres cannot be null!\"");
    }

    @Test
    @Transactional
    public void testUpdateBookAsAuthor() {
        Book updatedBook = new Book();
        updatedBook.setId(bookId);
        updatedBook.setTitle("new title");
        updatedBook.setAuthors(List.of("Author1", "authorName"));
        updatedBook.setGenres(List.of(Genre.SCIENCE, Genre.ROMANCE, Genre.HORROR));
        updatedBook.setDescription("desc desc");
        updatedBook.setNumPages(5501);
        bookService.updateBook(updatedBook);
        Book updatedBookTest = bookRepository.findByTitle("new title").get(0);

        assertThat(updatedBookTest.getId()).isEqualTo(updatedBook.getId());
        assertThat(updatedBookTest.getTitle()).isEqualTo(updatedBook.getTitle());
        assertThat(updatedBookTest.getAuthors().containsAll(updatedBook.getAuthors())).isTrue();
        assertThat(updatedBookTest.getGenres().containsAll(updatedBook.getGenres())).isTrue();
        assertThat(updatedBookTest.getDescription()).isEqualTo(updatedBook.getDescription());
        assertThat(updatedBookTest.getNumPages()).isEqualTo(updatedBook.getNumPages());
    }

    @Test
    @Transactional
    public void testDeleteBook() {
        bookService.deleteBook(bookId);
        assertThat(bookRepository.findById(bookId)).isEmpty();
    }

    @Test
    @Transactional
    public void testDeleteBookWhileUserHasItAsFavorite() {
        Username username = new Username("user");
        AppUser user = new AppUser(username, "test_emal@mail.com", new HashedPassword("hash"));
        userRepository.saveAndFlush(user);

        userService.updateFavouriteBook(username, bookId.toString());
        assertThat(userRepository.findByUsername(username).get().getFavouriteBook().getId()).isEqualTo(bookId);
        bookService.deleteBook(bookId);
        assertThat(bookRepository.findById(bookId)).isEmpty();
    }

    @Test
    @Transactional
    public void testDeleteBookNonExistent() {
        UUID randomUuid = UUID.randomUUID();
        while (randomUuid.equals(bookId)) {
            randomUuid = UUID.randomUUID();
        }
        UUID finalRandomUuid = randomUuid;
        assertThatThrownBy(() -> bookService.deleteBook(finalRandomUuid))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("404 NOT_FOUND \"This book does not exist!\"");
    }
}
