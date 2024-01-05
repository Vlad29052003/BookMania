package nl.tudelft.sem.template.authentication.domain.book;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import nl.tudelft.sem.template.authentication.application.book.BookEventsListener;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.AuthenticationService;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import nl.tudelft.sem.template.authentication.domain.user.UserService;
import nl.tudelft.sem.template.authentication.domain.user.Username;
import nl.tudelft.sem.template.authentication.models.AuthenticationRequestModel;
import nl.tudelft.sem.template.authentication.models.CreateBookRequestModel;
import nl.tudelft.sem.template.authentication.models.RegistrationRequestModel;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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
    private static final String bookshelfPath = "/a/catalog";
    private static final String reviewPath = "/b/book";
    private static WireMockServer mockServer;

    private static ByteArrayOutputStream outputStreamCaptor;
    private UUID adminId;

    /**
     * Initializes wire mock server.
     */
    @BeforeAll
    public static void init() {
        mockServer = new WireMockServer(
                new WireMockConfiguration().port(8080)
        );
        mockServer.start();

        configureFor("localhost", 8080);
        stubFor(WireMock.put(urlEqualTo(bookshelfPath))
                .willReturn(aResponse().withStatus(200)));
        stubFor(WireMock.post(urlEqualTo(bookshelfPath))
                .willReturn(aResponse().withStatus(200)));

        outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));

        // Since wiremock is configured on 8080, we assume everything is on the same port.
        BookEventsListener.BOOKSHELF_URI = "http://localhost:8080/a/catalog";
        BookEventsListener.REVIEW_URI = "http://localhost:8080/b/book";
    }

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
        registrationRequestModel.setEmail("email@gmail.com");
        registrationRequestModel.setPassword("Password123!");

        AuthenticationRequestModel authenticationRequestModel = new AuthenticationRequestModel();
        authenticationRequestModel.setUsername("admin");
        authenticationRequestModel.setPassword("Password123!");

        RegistrationRequestModel registrationRequestModelAuthor = new RegistrationRequestModel();
        registrationRequestModelAuthor.setUsername("authorTest");
        registrationRequestModelAuthor.setEmail("authorEmail@gmail.com");
        registrationRequestModelAuthor.setPassword("Password123!");

        AuthenticationRequestModel authenticationRequestModelAuthor = new AuthenticationRequestModel();
        authenticationRequestModelAuthor.setUsername("authorTest");
        authenticationRequestModelAuthor.setPassword("Password123!");

        authenticationService.registerUser(registrationRequestModel);
        AppUser admin = userRepository.findByUsername(new Username("admin")).orElseThrow();
        admin.setAuthority(Authority.ADMIN);
        adminId = admin.getId();
        userRepository.saveAndFlush(admin);
        tokenAdmin = "Bearer " + authenticationService.authenticateUser(authenticationRequestModel).getToken();

        registrationRequestModel.setUsername("user");
        registrationRequestModel.setEmail("user_email@gmail.com");
        authenticationRequestModel.setUsername("user");
        authenticationService.registerUser(registrationRequestModel);
        tokenNonAdmin = "Bearer " + authenticationService.authenticateUser(authenticationRequestModel).getToken();

        authenticationService.registerUser(registrationRequestModelAuthor);
        AppUser author = userRepository.findByUsername(new Username("authorTest")).orElseThrow();
        author.setAuthority(Authority.AUTHOR);
        userRepository.saveAndFlush(author);
        tokenAuthor = "Bearer " + authenticationService.authenticateUser(authenticationRequestModelAuthor).getToken();

        author.setName("authorName");


        stubFor(delete(urlEqualTo(bookshelfPath + "?bookId=" + book.getId().toString()))
                .willReturn(aResponse().withStatus(200)));
        stubFor(delete(urlEqualTo(reviewPath + "/" + book.getId() + "/" + adminId))
                .willReturn(aResponse().withStatus(200)));
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
        CreateBookRequestModel bookRequestModel = new CreateBookRequestModel();
        bookRequestModel.setTitle("title new");
        bookRequestModel.setAuthors(List.of("Author2", "Author3"));
        bookRequestModel.setGenres(List.of(Genre.SCIENCE));
        bookRequestModel.setDescription("description");
        bookRequestModel.setNumPages(876);
        outputStreamCaptor.reset();
        bookService.addBook(bookRequestModel, tokenAdmin);

        Book addedBook = bookRepository.findByTitle("title new").get(0);
        Assertions.assertThat(outputStreamCaptor.toString().trim())
                .contains("Book (id: " + addedBook.getId() + ", title: " + addedBook.getTitle() + ") was created.");

        Book newBook = new Book("title new", List.of("Author2", "Author3"),
                List.of(Genre.SCIENCE), "description", 876);
        assertThat(newBook.getTitle()).isEqualTo(addedBook.getTitle());
        assertThat(newBook.getAuthors()).isEqualTo(addedBook.getAuthors());
        assertThat(newBook.getGenres()).isEqualTo(addedBook.getGenres());
        assertThat(newBook.getDescription()).isEqualTo(addedBook.getDescription());
        assertThat(newBook.getNumPages()).isEqualTo(addedBook.getNumPages());
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

        assertThat(newBook.getTitle()).isEqualTo(addedBook.getTitle());
        assertThat(newBook.getAuthors()).isEqualTo(addedBook.getAuthors());
        assertThat(newBook.getGenres()).isEqualTo(addedBook.getGenres());
        assertThat(newBook.getDescription()).isEqualTo(addedBook.getDescription());
        assertThat(newBook.getNumPages()).isEqualTo(addedBook.getNumPages());
    }

    @Test
    @Transactional
    public void testAddBookAlreadyExisting() {
        CreateBookRequestModel bookRequestModel = new CreateBookRequestModel();
        bookRequestModel.setTitle("title");
        bookRequestModel.setAuthors(List.of("Author1", "authorName"));
        bookRequestModel.setGenres(List.of(Genre.SCIENCE));
        bookRequestModel.setDescription("desc");
        bookRequestModel.setNumPages(876);

        assertThatThrownBy(() -> bookService.addBook(bookRequestModel, tokenAdmin))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("409 CONFLICT \"The book is already in the system!\"");
    }

    @Test
    @Transactional
    public void testAddBookAlmostAlreadyExisting() {
        CreateBookRequestModel bookRequestModel = new CreateBookRequestModel();
        bookRequestModel.setTitle("title");
        bookRequestModel.setAuthors(List.of("Author1"));
        bookRequestModel.setGenres(List.of(Genre.SCIENCE));
        bookRequestModel.setDescription("desc");
        bookRequestModel.setNumPages(876);

        bookService.addBook(bookRequestModel, tokenAdmin);

        Book added = (Book) bookRepository.findByTitle("title")
                .stream().filter(b -> b.getAuthors().contains("Author1") && b.getAuthors().size() == 1).toArray()[0];
        assertThat(added.getTitle()).isEqualTo(bookRequestModel.getTitle());
        assertThat(new ArrayList<>(added.getAuthors())).isEqualTo(bookRequestModel.getAuthors());
        assertThat(new ArrayList<>(added.getGenres())).isEqualTo(bookRequestModel.getGenres());
        assertThat(added.getDescription()).isEqualTo(bookRequestModel.getDescription());
        assertThat(added.getNumPages()).isEqualTo(bookRequestModel.getNumPages());
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

        assertThatThrownBy(() -> bookService.addBook(bookRequestModel, tokenNonAdmin))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("401 UNAUTHORIZED \"Only admins or authors may add books to the system!\"");
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

        assertThatThrownBy(() -> bookService.updateBook(updatedBook, tokenNonAdmin))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("401 UNAUTHORIZED \"Only admins or authors may update books in the system!\"");
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

        assertThatThrownBy(() -> bookService.updateBook(updatedBook, tokenAdmin))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("404 NOT_FOUND \"This book does not exist!\"");
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

        assertThatThrownBy(() -> bookService.updateBook(updatedBook, tokenAuthor))
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

        outputStreamCaptor.reset();
        bookService.updateBook(updatedBook, tokenAdmin);
        Book updatedBookTest = bookRepository.findByTitle("title new").get(0);
        Assertions.assertThat(outputStreamCaptor.toString().trim())
                .contains("Book (id: " + updatedBook.getId() + ", title: " + updatedBook.getTitle() + ") was edited.");

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

        assertThatThrownBy(() -> bookService.updateBook(updatedBook, tokenAdmin))
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

        assertThatThrownBy(() -> bookService.updateBook(updatedBook, tokenAdmin))
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
        bookService.updateBook(updatedBook, tokenAuthor);
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
    public void testUpdateBookAsNotAuthorOfTheBook() {
        Book updatedBook = new Book();
        updatedBook.setId(book2Id);
        updatedBook.setTitle("newTitle");
        updatedBook.setAuthors(List.of("Author2"));
        updatedBook.setGenres(List.of(Genre.SCIENCE));
        updatedBook.setDescription("desc");
        updatedBook.setNumPages(550);
        assertThatThrownBy(() -> bookService.updateBook(updatedBook, tokenAuthor))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("401 UNAUTHORIZED \"Only the authors of the book may edit it!\"");
    }

    @Test
    @Transactional
    public void testDeleteBook() {
        bookService.deleteBook(bookId.toString(), tokenAdmin);
        assertThat(bookRepository.findById(bookId)).isEmpty();
        Assertions.assertThat(outputStreamCaptor.toString().trim())
                .contains("Book (id: " + book.getId() + ", title: " + book.getTitle() + ") was deleted.");

    }

    @Test
    @Transactional
    public void testDeleteBookWhileUserHasItAsFavorite() {
        userService.updateFavouriteBook(new Username("user"), bookId.toString());
        bookService.deleteBook(bookId.toString(), tokenAdmin);
        assertThat(bookRepository.findById(bookId)).isEmpty();
        assertThat(userRepository.findByUsername(new Username("user"))).isPresent();
    }

    @Test
    @Transactional
    public void testDeleteBookNonAdmin() {
        assertThatThrownBy(() -> bookService.deleteBook(bookId.toString(), tokenNonAdmin))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("401 UNAUTHORIZED \"Only admins may delete books from the system!\"");
    }

    @Test
    @Transactional
    public void testDeleteBookNonExistent() {
        UUID randomUuid = UUID.randomUUID();
        while (randomUuid.equals(bookId)) {
            randomUuid = UUID.randomUUID();
        }
        UUID finalRandomUuid = randomUuid;
        assertThatThrownBy(() -> bookService.deleteBook(finalRandomUuid.toString(), tokenAdmin))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("404 NOT_FOUND \"This book does not exist!\"");
    }

    @Test
    @Transactional
    public void authorNotPresentInTheDbAnymore() {
        CreateBookRequestModel bookRequestModel = new CreateBookRequestModel();
        bookRequestModel.setTitle("title new");
        bookRequestModel.setAuthors(List.of("Author2", "Author3"));
        bookRequestModel.setGenres(List.of(Genre.SCIENCE));
        bookRequestModel.setDescription("description");
        bookRequestModel.setNumPages(876);
        AppUser author = userRepository.findByUsername(new Username("authorTest")).get();
        userRepository.deleteById(author.getId());

        assertThatThrownBy(() -> bookService.addBook(bookRequestModel, tokenAuthor))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("401 UNAUTHORIZED \"Only the authors of the book may add it to the system!\"");
    }

    @Test
    @Transactional
    public void authorNotPresentInAuthors() {
        CreateBookRequestModel bookRequestModel = new CreateBookRequestModel();
        bookRequestModel.setTitle("title new");
        bookRequestModel.setAuthors(List.of("Author2", "Author3"));
        bookRequestModel.setGenres(List.of(Genre.SCIENCE));
        bookRequestModel.setDescription("description");
        bookRequestModel.setNumPages(876);

        assertThatThrownBy(() -> bookService.addBook(bookRequestModel, tokenAuthor))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("401 UNAUTHORIZED \"Only the authors of the book may add it to the system!\"");
    }

    /**
     * Clean up for the testing environment after all tests.
     */
    @AfterAll
    public static void afterEach() {
        mockServer.stop();
    }
}
