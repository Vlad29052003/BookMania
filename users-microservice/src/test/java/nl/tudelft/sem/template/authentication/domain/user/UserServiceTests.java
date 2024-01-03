package nl.tudelft.sem.template.authentication.domain.user;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.transaction.Transactional;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import nl.tudelft.sem.template.authentication.application.user.UserEventsListener;
import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.book.BookRepository;
import nl.tudelft.sem.template.authentication.domain.book.Genre;
import org.assertj.core.api.WritableAssertionInfo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UserServiceTests {
    @Autowired
    private transient UserService userService;

    @Autowired
    private transient UserRepository userRepository;

    @Autowired
    private transient BookRepository bookRepository;

    private static final String BOOKSHELF_URI = "/a/user";

    private static WireMockServer mockServer;

    private static ByteArrayOutputStream outputStreamCaptor;

    @BeforeAll
    public static void setUp() {
        mockServer = new WireMockServer(new WireMockConfiguration().port(8080));
        mockServer.start();

        configureFor("localhost", 8080);
        stubFor(delete(BOOKSHELF_URI)
                .willReturn(aResponse()
                        .withStatus(200)));

        outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));

        UserEventsListener.BOOKSHELF_URL = "http://localhost:8080/a/user";
    }

    @Test
    public void testGetUserByNetId() {
        Username username = new Username("username");
        String email = "test@email.com";
        HashedPassword password = new HashedPassword("pass123");
        assertThatThrownBy(() -> userService.getUserByUsername(username))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage(UserService.NO_SUCH_USER);

        AppUser user = new AppUser(username, email, password);
        userRepository.save(user);
        AppUser retrievedUser = userService.getUserByUsername(username);

        assertThat(retrievedUser.getUsername().toString()).isEqualTo(username.toString());
        assertThat(retrievedUser.getEmail()).isEqualTo(email);
        assertThat(retrievedUser.getPassword().toString()).isEqualTo(password.toString());
    }

    @Test
    public void testUpdateName() {
        Username username = new Username("username");
        String email = "test@email.com";
        HashedPassword password = new HashedPassword("pass123");
        String newName = "Name";
        assertThatThrownBy(() -> userService.updateName(username, newName))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage(UserService.NO_SUCH_USER);

        AppUser user = new AppUser(username, email, password);
        userRepository.save(user);
        AppUser retrievedUser = userService.getUserByUsername(username);
        assertThat(retrievedUser.getName()).isNull();


        userService.updateName(username, newName);
        retrievedUser = userService.getUserByUsername(username);
        assertThat(retrievedUser.getName()).isEqualTo(newName);
    }

    @Test
    public void testUpdateBio() {
        Username username = new Username("username");
        String email = "test@email.com";
        HashedPassword password = new HashedPassword("pass123");
        String newBio = "Bio";
        assertThatThrownBy(() -> userService.updateBio(username, newBio))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage(UserService.NO_SUCH_USER);

        AppUser user = new AppUser(username, email, password);
        userRepository.save(user);
        AppUser retrievedUser = userService.getUserByUsername(username);
        assertThat(retrievedUser.getBio()).isNull();


        userService.updateBio(username, newBio);
        retrievedUser = userService.getUserByUsername(username);
        assertThat(retrievedUser.getBio()).isEqualTo(newBio);
    }

    @Test
    public void testUpdatePicture() {
        Username username = new Username("username");
        String email = "test@email.com";
        HashedPassword password = new HashedPassword("pass123");
        byte[] newPicture = new byte[]{13, 24, 51, 24, 14};
        assertThatThrownBy(() -> userService.updatePicture(username, newPicture))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage(UserService.NO_SUCH_USER);

        AppUser user = new AppUser(username, email, password);
        userRepository.save(user);
        AppUser retrievedUser = userService.getUserByUsername(username);
        assertThat(retrievedUser.getPicture()).isNull();


        userService.updatePicture(username, newPicture);
        retrievedUser = userService.getUserByUsername(username);
        assertThat(retrievedUser.getPicture()).isEqualTo(newPicture);
    }

    @Test
    public void testUpdateLocation() {
        Username username = new Username("username");
        String email = "test@email.com";
        HashedPassword password = new HashedPassword("pass123");
        String newLocation = "Location";
        assertThatThrownBy(() -> userService.updateLocation(username, newLocation))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage(UserService.NO_SUCH_USER);

        AppUser user = new AppUser(username, email, password);
        userRepository.save(user);
        AppUser retrievedUser = userService.getUserByUsername(username);
        assertThat(retrievedUser.getLocation()).isNull();


        userService.updateLocation(username, newLocation);
        retrievedUser = userService.getUserByUsername(username);
        assertThat(retrievedUser.getLocation()).isEqualTo(newLocation);
    }

    @Test
    @Transactional
    public void testUpdateFavouriteGenres() {
        Username username = new Username("username");
        String email = "test@email.com";
        HashedPassword password = new HashedPassword("pass123");
        List<Genre> newFavouriteGenres = new ArrayList<>(List.of(Genre.CRIME, Genre.SCIENCE, Genre.ROMANCE));
        assertThatThrownBy(() -> userService.updateFavouriteGenres(username, newFavouriteGenres))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage(UserService.NO_SUCH_USER);

        AppUser user = new AppUser(username, email, password);
        userRepository.save(user);
        AppUser retrievedUser = userService.getUserByUsername(username);
        assertThat(retrievedUser.getFavouriteGenres()).isEmpty();

        userService.updateFavouriteGenres(username, newFavouriteGenres);
        retrievedUser = userService.getUserByUsername(username);
        assertThat(retrievedUser.getFavouriteGenres().toArray()).isEqualTo(newFavouriteGenres.toArray());
    }

    @Test
    @Transactional
    public void testUpdateFavouriteBook() {
        Username username = new Username("username");
        String email = "test@email.com";
        HashedPassword password = new HashedPassword("pass123");
        assertThatThrownBy(() -> userService.updateFavouriteBook(username, UUID.randomUUID().toString()))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage(UserService.NO_SUCH_USER);

        AppUser user = new AppUser(username, email, password);
        userRepository.save(user);
        AppUser retrievedUser = userService.getUserByUsername(username);
        assertThat(retrievedUser.getFavouriteBook()).isNull();
        assertThatThrownBy(() -> userService.updateFavouriteBook(username, UUID.randomUUID().toString()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The book with the given ID does not exist!");

        Book newBook = new Book("Title",
                List.of("First Author", "Second Author"),
                List.of(Genre.CRIME, Genre.SCIENCE), "Short description.", 5);
        bookRepository.save(newBook);
        UUID bookId = bookRepository.findAll().get(0).getId();
        userService.updateFavouriteBook(username, bookId.toString());
        retrievedUser = userService.getUserByUsername(username);
        assertThat(retrievedUser.getFavouriteBook().getTitle()).isEqualTo(newBook.getTitle());
        assertThat(retrievedUser.getFavouriteBook().getAuthors().toArray()).isEqualTo(newBook.getAuthors().toArray());
        assertThat(retrievedUser.getFavouriteBook().getGenres().toArray()).isEqualTo(newBook.getGenres().toArray());
        assertThat(retrievedUser.getFavouriteBook().getDescription()).isEqualTo(newBook.getDescription());
    }

    @Test
    @Transactional
    public void testDeleteUser() {
        Username username = new Username("username");
        String email = "test@email.com";
        HashedPassword password = new HashedPassword("pass123");
        assertThatThrownBy(() -> userService.getUserByUsername(username))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage(UserService.NO_SUCH_USER);

        AppUser user = new AppUser(username, email, password);
        userRepository.save(user);
        AppUser retrievedUser = userService.getUserByUsername(username);

        outputStreamCaptor.reset();

        stubFor(WireMock.delete(urlEqualTo(BOOKSHELF_URI + "?userId=" + user.getId().toString()))
                .willReturn(aResponse().withStatus(200)));

        userService.delete(retrievedUser.getUsername());

        assertThat(outputStreamCaptor.toString().trim()).contains("User deleted");

        assertThatThrownBy(() -> userService.getUserByUsername(retrievedUser.getUsername()))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage(UserService.NO_SUCH_USER);
    }
}
