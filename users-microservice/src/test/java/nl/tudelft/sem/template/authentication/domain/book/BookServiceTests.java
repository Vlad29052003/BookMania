package nl.tudelft.sem.template.authentication.domain.book;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.util.List;
import java.util.UUID;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.AuthenticationService;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import nl.tudelft.sem.template.authentication.domain.user.UserService;
import nl.tudelft.sem.template.authentication.domain.user.Username;
import nl.tudelft.sem.template.authentication.models.AuthenticationRequestModel;
import nl.tudelft.sem.template.authentication.models.RegistrationRequestModel;
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
                List.of(Genre.CRIME), "testDescription", 550);
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
        bookService.deleteBook(bookId.toString(), tokenAdmin);
        assertThat(bookRepository.findById(bookId)).isEmpty();
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
}
