package nl.tudelft.sem.template.authentication.domain.book;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.AuthenticationService;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import nl.tudelft.sem.template.authentication.domain.user.UserService;
import nl.tudelft.sem.template.authentication.domain.user.Username;
import nl.tudelft.sem.template.authentication.models.AuthenticationRequestModel;
import nl.tudelft.sem.template.authentication.models.CreateBookRequestModel;
import nl.tudelft.sem.template.authentication.models.RegistrationRequestModel;
import nl.tudelft.sem.template.authentication.models.UpdateBookRequestModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class BookServiceTests {
    @Autowired
    private transient BookRepository bookRepository;
    @Autowired
    private transient UserRepository userRepository;
    @Autowired
    private transient BookService bookService;
    @Autowired
    private transient AuthenticationService authenticationService;
    @Autowired
    private transient UserService userService;
    private transient UUID bookId;
    private transient Book book;
    private transient String tokenAdmin;
    private transient String tokenNonAdmin;

    /**
     * Sets up the testing environment.
     */
    @BeforeEach
    public void setUp() {
        this.book = new Book("title", List.of("Author1"), List.of(Genre.CRIME, Genre.DRAMA), "description", 257);
        bookRepository.saveAndFlush(book);
        bookId = bookRepository.findByTitle("title").get(0).getId();

        RegistrationRequestModel registrationRequestModel = new RegistrationRequestModel();
        registrationRequestModel.setUsername("admin");
        registrationRequestModel.setEmail("email");
        registrationRequestModel.setPassword("pass");

        AuthenticationRequestModel authenticationRequestModel = new AuthenticationRequestModel();
        authenticationRequestModel.setUsername("admin");
        authenticationRequestModel.setPassword("pass");

        authenticationService.registerUser(registrationRequestModel);
        AppUser admin = userRepository.findByUsername(new Username("admin")).get();
        admin.setAuthority(Authority.ADMIN);
        userRepository.saveAndFlush(admin);
        tokenAdmin = "Bearer " + authenticationService.authenticateUser(authenticationRequestModel).getToken();

        registrationRequestModel.setUsername("user");
        registrationRequestModel.setEmail("user_email");
        authenticationRequestModel.setUsername("user");
        authenticationService.registerUser(registrationRequestModel);
        tokenNonAdmin = "Bearer " + authenticationService.authenticateUser(authenticationRequestModel).getToken();
    }

    @Test
    public void testGet() {
        assertEquals(bookService.getBook(bookId.toString()), book);
    }

    @Test
    public void testGetNotFound() {
        UUID randomUuid = UUID.randomUUID();
        while (randomUuid.equals(bookId)) {
            randomUuid = UUID.randomUUID();
        }
        UUID finalRandomUuid = randomUuid;
        assertThrows(IllegalArgumentException.class, () -> bookService.getBook(finalRandomUuid.toString()));
    }

    @Test
    @Transactional
    public void testAddBook() {
        CreateBookRequestModel bookRequestModel = new CreateBookRequestModel();
        bookRequestModel.setTitle("title new");
        bookRequestModel.setAuthors(List.of("Author2", "Author3"));
        bookRequestModel.setGenres(List.of(Genre.SCIENCE));
        bookRequestModel.setDescription("description");
        bookRequestModel.setNumPages(876);
        Book newBook = new Book("title new", List.of("Author2", "Author3"), List.of(Genre.SCIENCE), "description", 876);
        bookService.addBook(bookRequestModel, tokenAdmin);
        Book addedBook = bookRepository.findByTitle("title new").get(0);

        assertEquals(newBook.getTitle(), addedBook.getTitle());
        assertEquals(newBook.getAuthors(), addedBook.getAuthors());
        assertEquals(newBook.getGenres(), addedBook.getGenres());
        assertEquals(newBook.getDescription(), addedBook.getDescription());
        assertEquals(newBook.getNumPages(), addedBook.getNumPages());
    }

    @Test
    @Transactional
    public void testAddBookAlreadyExisting() {
        CreateBookRequestModel bookRequestModel = new CreateBookRequestModel();
        bookRequestModel.setTitle("title");
        bookRequestModel.setAuthors(List.of("Author1"));
        bookRequestModel.setGenres(List.of(Genre.SCIENCE));
        bookRequestModel.setDescription("desc");
        bookRequestModel.setNumPages(876);

        assertThrows(IllegalArgumentException.class, () -> bookService.addBook(bookRequestModel, tokenAdmin));
    }

    @Test
    @Transactional
    public void testAddBookNotAdmin() {
        CreateBookRequestModel bookRequestModel = new CreateBookRequestModel();
        bookRequestModel.setTitle("title");
        bookRequestModel.setAuthors(List.of("Author1"));
        bookRequestModel.setGenres(List.of(Genre.SCIENCE));
        bookRequestModel.setDescription("desc");
        bookRequestModel.setNumPages(876);

        assertThrows(IllegalCallerException.class, () -> bookService.addBook(bookRequestModel, tokenNonAdmin));
    }

    @Test
    @Transactional
    public void testUpdateBookNotAdmin() {
        UpdateBookRequestModel bookRequestModel = new UpdateBookRequestModel();
        bookRequestModel.setId(bookId.toString());
        bookRequestModel.setTitle("title");
        bookRequestModel.setAuthors(List.of("Author1"));
        bookRequestModel.setGenres(List.of(Genre.SCIENCE));
        bookRequestModel.setDescription("desc");
        bookRequestModel.setNumPages(876);

        assertThrows(IllegalCallerException.class, () -> bookService.updateBook(bookRequestModel, tokenNonAdmin));
    }

    @Test
    @Transactional
    public void testUpdateBookNotExisting() {
        UUID randomUuid = UUID.randomUUID();
        while (randomUuid.equals(bookId)) {
            randomUuid = UUID.randomUUID();
        }
        UUID finalRandomUuid = randomUuid;
        UpdateBookRequestModel bookRequestModel = new UpdateBookRequestModel();
        bookRequestModel.setId(finalRandomUuid.toString());
        bookRequestModel.setTitle("title");
        bookRequestModel.setAuthors(List.of("Author1"));
        bookRequestModel.setGenres(List.of(Genre.SCIENCE));
        bookRequestModel.setDescription("desc");
        bookRequestModel.setNumPages(876);

        assertThrows(IllegalArgumentException.class, () -> bookService.updateBook(bookRequestModel, tokenAdmin));
    }

    @Test
    @Transactional
    public void testUpdateBook() {
        UpdateBookRequestModel bookRequestModel = new UpdateBookRequestModel();
        bookRequestModel.setId(bookId.toString());
        bookRequestModel.setTitle("title new");
        bookRequestModel.setAuthors(List.of("Author1"));
        bookRequestModel.setGenres(List.of(Genre.SCIENCE));
        bookRequestModel.setDescription("desc");
        bookRequestModel.setNumPages(876);
        bookService.updateBook(bookRequestModel, tokenAdmin);
        Book updatedBook = bookRepository.findByTitle("title new").get(0);

        assertEquals(updatedBook.getId().toString(), bookRequestModel.getId());
        assertEquals(updatedBook.getTitle(), bookRequestModel.getTitle());
        assertTrue(updatedBook.getAuthors().containsAll(bookRequestModel.getAuthors()));
        assertTrue(updatedBook.getGenres().containsAll(bookRequestModel.getGenres()));
        assertEquals(updatedBook.getDescription(), bookRequestModel.getDescription());
        assertEquals(updatedBook.getNumPages(), bookRequestModel.getNumPages());
    }

    @Test
    @Transactional
    public void testDeleteBook() {
        bookService.deleteBook(bookId.toString(), tokenAdmin);
        assertTrue(bookRepository.findById(bookId).isEmpty());
    }

    @Test
    @Transactional
    public void testDeleteBookWhileUserHasItAsFavorite() {
        userService.updateFavouriteBook(new Username("user"), bookId.toString());
        bookService.deleteBook(bookId.toString(), tokenAdmin);
        assertTrue(bookRepository.findById(bookId).isEmpty());
        assertTrue(userRepository.findByUsername(new Username("user")).isPresent());
    }

    @Test
    @Transactional
    public void testDeleteBookNonAdmin() {
        assertThrows(IllegalCallerException.class, () -> bookService.deleteBook(bookId.toString(), tokenNonAdmin));
    }

    @Test
    @Transactional
    public void testDeleteBookNonExistent() {
        UUID randomUuid = UUID.randomUUID();
        while (randomUuid.equals(bookId)) {
            randomUuid = UUID.randomUUID();
        }
        UUID finalRandomUuid = randomUuid;
        assertThrows(IllegalArgumentException.class, () -> bookService.deleteBook(finalRandomUuid.toString(), tokenAdmin));
    }
}
