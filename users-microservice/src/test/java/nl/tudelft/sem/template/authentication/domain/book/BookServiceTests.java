package nl.tudelft.sem.template.authentication.domain.book;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import com.github.tomakehurst.wiremock.WireMockServer;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.AuthenticationService;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import nl.tudelft.sem.template.authentication.domain.user.UserService;
import nl.tudelft.sem.template.authentication.domain.user.Username;
import nl.tudelft.sem.template.authentication.models.AuthenticationRequestModel;
import nl.tudelft.sem.template.authentication.models.CreateBookRequestModel;
import nl.tudelft.sem.template.authentication.models.RegistrationRequestModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

    private transient Book book2;

    private transient UUID book2Id;
    private transient String tokenAdmin;
    private transient String tokenNonAdmin;

    private transient String tokenAuthor;

    /**
     * Sets up the testing environment.
     */
    @BeforeEach
    public void setUp() {
        this.book = new Book("title", List.of("Author1", "authorName"),
                List.of(Genre.CRIME, Genre.DRAMA), "description", 257);
        bookRepository.saveAndFlush(book);
        bookId = bookRepository.findByTitle("title").get(0).getId();

        this.book2 = new Book("title2", List.of("Author2"),
                List.of(Genre.CRIME), "testDscription", 550);
        bookRepository.saveAndFlush(book2);
        book2Id = bookRepository.findByTitle("title2").get(0).getId();


        RegistrationRequestModel registrationRequestModel = new RegistrationRequestModel();
        registrationRequestModel.setUsername("admin");
        registrationRequestModel.setEmail("email");
        registrationRequestModel.setPassword("pass");

        AuthenticationRequestModel authenticationRequestModel = new AuthenticationRequestModel();
        authenticationRequestModel.setUsername("admin");
        authenticationRequestModel.setPassword("pass");

        RegistrationRequestModel registrationRequestModelAuthor = new RegistrationRequestModel();
        registrationRequestModelAuthor.setUsername("authorTest");
        registrationRequestModelAuthor.setEmail("authorEmail");
        registrationRequestModelAuthor.setPassword("pass");

        AuthenticationRequestModel authenticationRequestModelAuthor = new AuthenticationRequestModel();
        authenticationRequestModelAuthor.setUsername("authorTest");
        authenticationRequestModelAuthor.setPassword("pass");

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

        authenticationService.registerUser(registrationRequestModelAuthor);
        AppUser author = userRepository.findByUsername(new Username("authorTest")).get();
        author.setAuthority(Authority.AUTHOR);
        userRepository.saveAndFlush(author);
        tokenAuthor = "Bearer " + authenticationService.authenticateUser(authenticationRequestModelAuthor).getToken();

        author.setName("authorName");
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
        assertThrows(ResponseStatusException.class, () -> bookService.getBook(finalRandomUuid.toString()));
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
        Book newBook = new Book("title new", List.of("Author2", "Author3"),
                List.of(Genre.SCIENCE), "description", 876);
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
    public void testAddBookAsAuthor() {
        CreateBookRequestModel bookRequestModel = new CreateBookRequestModel();
        bookRequestModel.setTitle("titleBook");
        bookRequestModel.setAuthors(List.of("authorName"));
        bookRequestModel.setGenres(List.of(Genre.SCIENCE));
        bookRequestModel.setDescription("descriptionBook");
        bookRequestModel.setNumPages(550);

        Book newBook = new Book("titleBook", List.of("authorName"), List.of(Genre.SCIENCE),
                "descriptionBook", 550);
        bookService.addBook(bookRequestModel, tokenAuthor);
        Book addedBook = bookRepository.findByTitle("titleBook").get(0);

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

        assertThrows(ResponseStatusException.class, () -> bookService.addBook(bookRequestModel, tokenAdmin));
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

        assertThrows(ResponseStatusException.class, () -> bookService.addBook(bookRequestModel, tokenNonAdmin));
    }

    @Test
    @Transactional
    public void testUpdateBookNotAdmin() {
        Book updatedBook = new Book();
        updatedBook.setId(bookId);
        updatedBook.setTitle("title");
        updatedBook.setAuthors(List.of("Author1"));
        updatedBook.setGenres(List.of(Genre.SCIENCE));
        updatedBook.setDescription("desc");
        updatedBook.setNumPages(876);

        assertThrows(ResponseStatusException.class, () -> bookService.updateBook(updatedBook, tokenNonAdmin));
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

        assertThrows(ResponseStatusException.class, () -> bookService.updateBook(updatedBook, tokenAdmin));
    }

    @Test
    @Transactional
    public void testUpdateBookNotExistingAsAuthor() {
        UUID randomUuid = UUID.randomUUID();
        while (randomUuid.equals(bookId)) {
            randomUuid = UUID.randomUUID();
        }
        UUID finalRandomUuid = randomUuid;
        Book updatedBook = new Book();
        updatedBook.setId(finalRandomUuid);
        updatedBook.setTitle("title");
        updatedBook.setAuthors(List.of("Author1"));
        updatedBook.setGenres(List.of(Genre.HORROR));
        updatedBook.setDescription("desc");
        updatedBook.setNumPages(550);

        assertThrows(ResponseStatusException.class, () -> bookService.updateBook(updatedBook, tokenAuthor));
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
        bookService.updateBook(updatedBook, tokenAdmin);
        Book updatedBookTest = bookRepository.findByTitle("title new").get(0);

        assertEquals(updatedBookTest.getId(), updatedBook.getId());
        assertEquals(updatedBookTest.getTitle(), updatedBook.getTitle());
        assertTrue(updatedBookTest.getAuthors().containsAll(updatedBook.getAuthors()));
        assertTrue(updatedBookTest.getGenres().containsAll(updatedBook.getGenres()));
        assertEquals(updatedBookTest.getDescription(), updatedBook.getDescription());
        assertEquals(updatedBookTest.getNumPages(), updatedBook.getNumPages());
    }

    @Test
    @Transactional
    public void testUpdateBookAsAuthor() {
        Book updatedBook = new Book();
        updatedBook.setId(bookId);
        updatedBook.setTitle("title");
        updatedBook.setAuthors(List.of("Author1", "authorName"));
        updatedBook.setGenres(List.of(Genre.SCIENCE));
        updatedBook.setDescription("desc");
        updatedBook.setNumPages(550);
        bookService.updateBook(updatedBook, tokenAuthor);
        Book updatedBookTest = bookRepository.findByTitle("title").get(0);

        assertEquals(updatedBookTest.getId(), updatedBook.getId());
        assertEquals(updatedBookTest.getTitle(), updatedBook.getTitle());
        assertTrue(updatedBookTest.getAuthors().containsAll(updatedBook.getAuthors()));
        assertTrue(updatedBookTest.getGenres().containsAll(updatedBook.getGenres()));
        assertEquals(updatedBookTest.getDescription(), updatedBook.getDescription());
        assertEquals(updatedBookTest.getNumPages(), updatedBook.getNumPages());
    }

    @Test
    @Transactional
    public void testUpdateBookAsNotAuthorOfTheBook() {
        Book updatedBook = new Book();
        updatedBook.setId(book2Id);
        updatedBook.setTitle("newTitle");
        updatedBook.setAuthors(List.of("Author2"));
        updatedBook.setGenres(List.of(Genre.SCIENCE));
        updatedBook.setDescription("desc");
        updatedBook.setNumPages(550);
        assertThrows(ResponseStatusException.class, () -> bookService.updateBook(updatedBook, tokenAuthor));
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
        assertThrows(ResponseStatusException.class, () -> bookService.deleteBook(bookId.toString(), tokenNonAdmin));
    }

    @Test
    @Transactional
    public void testDeleteBookNonExistent() {
        UUID randomUuid = UUID.randomUUID();
        while (randomUuid.equals(bookId)) {
            randomUuid = UUID.randomUUID();
        }
        UUID finalRandomUuid = randomUuid;
        assertThrows(ResponseStatusException.class, () -> bookService.deleteBook(finalRandomUuid.toString(), tokenAdmin));
    }
}
