package nl.tudelft.sem.template.authentication.domain.user;

import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import nl.tudelft.sem.template.authentication.application.user.UserEventsListener;
import nl.tudelft.sem.template.authentication.authentication.JwtService;
import nl.tudelft.sem.template.authentication.authentication.JwtTokenGenerator;
import nl.tudelft.sem.template.authentication.authentication.JwtUserDetailsService;
import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.book.BookRepository;
import nl.tudelft.sem.template.authentication.domain.book.Genre;
import nl.tudelft.sem.template.authentication.models.AuthenticationRequestModel;
import nl.tudelft.sem.template.authentication.models.AuthenticationResponseModel;
import nl.tudelft.sem.template.authentication.models.RegistrationRequestModel;
import nl.tudelft.sem.template.authentication.models.TokenValidationResponse;
import nl.tudelft.sem.template.authentication.models.UserModel;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UserLookupServiceTests {

    @Autowired
    private transient UserLookupService userLookupService;

    @Autowired
    private transient UserService userService;

    @Autowired
    private transient AuthenticationService authenticationService;

    @Autowired
    private transient UserRepository userRepository;

    @Autowired
    private transient BookRepository bookRepository;

    @Autowired
    private transient AuthenticationManager authenticationManager;

    @Autowired
    private transient JwtTokenGenerator jwtTokenGenerator;

    @Autowired
    private transient JwtUserDetailsService jwtUserDetailsService;

    @Autowired
    private transient JwtService jwtService;

    private static WireMockServer wireMockServer;

    private transient RegistrationRequestModel registrationRequest;
    private transient RegistrationRequestModel registrationRequest2;

    private transient Book book;

    private transient UUID bookId;

    private transient AppUser testUser;

    private transient AppUser testUser2;

    private static final String BOOKSHELF_PATH = "/a/user";


    /**
     * Sets up for testing.
     */
    @BeforeAll
    public static void init() {
        wireMockServer = new WireMockServer(new WireMockConfiguration().port(8080));
        wireMockServer.start();

        WireMock.configureFor("localhost", 8080);

        stubFor(WireMock.post(urlEqualTo(BOOKSHELF_PATH))
                .willReturn(WireMock.aResponse().withStatus(200)));

        UserEventsListener.BOOKSHELF_URL = "http://localhost:8080/a/user";
    }

    /**
     * Sets up the testing environment.
     */
    @BeforeEach
    public void setUp() {
        PasswordHashingService passwordHashingService = mock(PasswordHashingService.class);
        when(passwordHashingService.hash(new Password("someOtherHash1!"))).thenReturn(new HashedPassword("someHash"));
        authenticationService = new AuthenticationService(authenticationManager,
                jwtTokenGenerator, jwtUserDetailsService,
                jwtService, userRepository, passwordHashingService);

        String email = "email@gmail.com";
        String username = "user";
        String password = "someHash123!";
        UUID id = UUID.randomUUID();

        testUser = new AppUser(new Username(username), email, new HashedPassword(password));
        testUser.setId(id);
        testUser.setFavouriteBook(book);

        registrationRequest = new RegistrationRequestModel();
        registrationRequest.setUsername(username);
        registrationRequest.setEmail(email);
        registrationRequest.setPassword(password);

        AuthenticationRequestModel authenticationRequest = new AuthenticationRequestModel();
        authenticationRequest.setPassword(password);
        authenticationRequest.setUsername(email);

        AuthenticationResponseModel authenticationResponse = new AuthenticationResponseModel();
        String token = "Bearer token";
        authenticationResponse.setToken(token);

        TokenValidationResponse tokenValidationResponse = new TokenValidationResponse();
        tokenValidationResponse.setId(id);


        String email2 = "email2@gmail.com";
        String username2 = "andrei";
        String password2 = "someHash1!";
        UUID id2 = UUID.randomUUID();

        AppUser appUser2 = new AppUser(new Username(username2), email2, new HashedPassword(password2));
        appUser2.setId(id2);
        testUser2 = new AppUser(new Username(username2), email2, new HashedPassword(password2));
        testUser2.setId(id2);

        registrationRequest2 = new RegistrationRequestModel();
        registrationRequest2.setUsername(username2);
        registrationRequest2.setEmail(email2);
        registrationRequest2.setPassword(password2);

        book = new Book("title", List.of("authorName"), List.of(Genre.CRIME),
                "description", 20);
        bookId = UUID.randomUUID();
        book.setId(bookId);
        bookRepository.save(book);
    }

    /**
     * Tests whether the user is correctly returned when using the proper query string.
     *
     */
    @Test
    public void userSearchByName_worksCorrectly() {
        authenticationService.registerUser(registrationRequest);
        authenticationService.registerUser(registrationRequest2);

        // Assert
        List<String> foundUsers = userLookupService.getUsersByName("user")
                .stream().map(UserModel::getUsername).collect(Collectors.toList());

        assertThat(foundUsers).containsExactlyInAnyOrder("user");
    }


    /**
     * Tests whether all users are returned when using an empty query string.
     *
     */
    @Test
    public void userSearchByName_worksCorrectly2() {

        authenticationService.registerUser(registrationRequest);
        authenticationService.registerUser(registrationRequest2);

        // Assert
        List<String> foundUsers = userLookupService.getUsersByName("")
                .stream().map(UserModel::getUsername).collect(Collectors.toList());

        assertThat(foundUsers).containsExactlyInAnyOrder("user", "andrei");
    }

    @Test
    public void userSearchByName_worksCorrectly_deactivatedUser() {

        authenticationService.registerUser(registrationRequest);
        authenticationService.registerUser(registrationRequest2);

        AppUser deactivated = userRepository.findByUsername(new Username("user")).get();
        deactivated.setDeactivated(true);
        userRepository.saveAndFlush(deactivated);

        // Assert
        List<String> foundUsers = userLookupService.getUsersByName("user")
                .stream().map(UserModel::getUsername).collect(Collectors.toList());
        List<String> expected = new ArrayList<>();


        assertThat(foundUsers).isEqualTo(expected);
    }

    @Test
    public void userSearchByName_withPrivateUser() {

        authenticationService.registerUser(registrationRequest);
        authenticationService.registerUser(registrationRequest2);

        String email3 = "private@user.com";
        String username3 = "privateUser";
        String password3 = "Pass123!";
        UUID id3 = UUID.randomUUID();

        AppUser appUser3 = new AppUser(new Username(username3), email3, new HashedPassword(password3));
        appUser3.setId(id3);

        RegistrationRequestModel registrationRequest3 = new RegistrationRequestModel();
        registrationRequest3.setUsername(username3);
        registrationRequest3.setEmail(email3);
        registrationRequest3.setPassword(password3);

        authenticationService.registerUser(registrationRequest3);

        userService.updatePrivacy(new Username(username3), true);

        List<String> foundUsers = userLookupService.getUsersByName("")
                .stream().map(UserModel::getUsername).collect(Collectors.toList());
        List<String> expected = List.of("user", "andrei");

        assertThat(foundUsers).containsAll(expected);
    }

    @Test
    @Transactional
    public void testUserSearchByFavouriteBook() {
        authenticationService.registerUser(registrationRequest);
        authenticationService.registerUser(registrationRequest2);

        Iterable<AppUser> users = userRepository.findAll();
        AppUser user = users.iterator().next();

        Book favBook = bookRepository.findAll().get(0);

        user.setFavouriteBook(favBook);
        userRepository.save(user);

        List<String> foundUsers = userLookupService.getUsersByFavouriteBook(favBook.getId())
                .stream().map(UserModel::getUsername).collect(Collectors.toList());
        List<String> expected = List.of("user");

        assertThat(foundUsers).containsAll(expected);
    }

    @Test
    public void testUserSearchByFavouriteBookNoResults1() {
        authenticationService.registerUser(registrationRequest);
        authenticationService.registerUser(registrationRequest2);

        Iterable<AppUser> users = userRepository.findAll();
        AppUser user = users.iterator().next();

        Book favBook = bookRepository.findAll().get(0);
        user.setFavouriteBook(favBook);
        userRepository.save(user);

        assertThatThrownBy(() -> userLookupService.getUsersByFavouriteBook(UUID.randomUUID()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("No users with this favourite book found!");
    }

    @Test
    public void testUserSearchByFavouriteBookNoResults2() {
        authenticationService.registerUser(registrationRequest);
        authenticationService.registerUser(registrationRequest2);

        List<AppUser> users = userRepository.findAll();
        AppUser user1 = users.get(0);
        AppUser user2 = users.get(1);

        Book favBook = bookRepository.findAll().get(0);

        user1.setFavouriteBook(favBook);
        user2.setFavouriteBook(favBook);
        userRepository.saveAll(List.of(user1, user2));

        List<String> foundUsers = userLookupService.getUsersByFavouriteBook(favBook.getId())
                .stream().map(UserModel::getUsername).collect(Collectors.toList());

        assertThat(foundUsers).containsExactlyInAnyOrder("user", "andrei");

        Book newBook = new Book("newBook", List.of("auth1", "auth2"), List.of(Genre.CRIME), "desc", 255);
        bookRepository.saveAndFlush(newBook);
        newBook = bookRepository.findByTitle("newBook").get(0);

        user2.setFavouriteBook(newBook);
        userRepository.save(user2);
        foundUsers = userLookupService.getUsersByFavouriteBook(favBook.getId())
                .stream().map(UserModel::getUsername).collect(Collectors.toList());

        assertThat(foundUsers).containsExactlyInAnyOrder("user");
    }

    @Test
    public void testUserSearchByFavouriteBookNoResults3() {
        authenticationService.registerUser(registrationRequest);
        authenticationService.registerUser(registrationRequest2);

        Iterable<AppUser> users = userRepository.findAll();
        AppUser user = users.iterator().next();

        Book favBook = bookRepository.findAll().get(0);
        user.setFavouriteBook(favBook);
        user.setDeactivated(true);
        userRepository.save(user);

        assertThatThrownBy(() -> userLookupService.getUsersByFavouriteBook(favBook.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("404 NOT_FOUND \"No users found!\"");

        user.setDeactivated(false);
        user.setPrivate(true);
        userRepository.save(user);

        assertThatThrownBy(() -> userLookupService.getUsersByFavouriteBook(favBook.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("404 NOT_FOUND \"No users found!\"");

        user.setPrivate(false);
        user.setFavouriteBook(null);
        userRepository.save(user);

        assertThatThrownBy(() -> userLookupService.getUsersByFavouriteBook(favBook.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("404 NOT_FOUND \"No users found!\"");
    }

    @Test
    public void testNoUsersFoundWhileSearch() {
        assertThatThrownBy(() -> userLookupService.getUsersByFavouriteBook(UUID.randomUUID()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("404 NOT_FOUND \"No users found!\"");
    }

    @Test
    @Transactional
    public void testUserSearchByFavGenre() {
        AppUser user = new AppUser(new Username("user"), "email@gmail.com", new HashedPassword("Password123!"));

        user.setFavouriteGenres(List.of(Genre.CRIME));

        userRepository.save(user);

        List<String> foundUsers = userLookupService.getUsersByFavouriteGenres(List.of(Genre.CRIME))
                .stream().map(UserModel::getUsername).collect(Collectors.toList());
        List<String> expected = List.of("user");

        assertThat(foundUsers).containsAll(expected);
    }

    @Test
    @Transactional
    public void testNoUsersFoundWhileSearchByGenre() {
        AppUser user = new AppUser(new Username("user"), "email@gmail.com", new HashedPassword("Password123!"));
        userRepository.save(user);

        assertThatThrownBy(() -> userLookupService.getUsersByFavouriteGenres(List.of(Genre.CRIME)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("404 NOT_FOUND \"No users found!\"");

        user.setDeactivated(true);
        userRepository.save(user);

        assertThatThrownBy(() -> userLookupService.getUsersByFavouriteGenres(List.of(Genre.CRIME)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("404 NOT_FOUND \"No users found!\"");

        user.setDeactivated(false);
        user.setPrivate(true);
        userRepository.save(user);

        assertThatThrownBy(() -> userLookupService.getUsersByFavouriteGenres(List.of(Genre.CRIME)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("404 NOT_FOUND \"No users found!\"");

        user.setPrivate(false);
        user.setFavouriteGenres(null);
        userRepository.save(user);

        assertThatThrownBy(() -> userLookupService.getUsersByFavouriteGenres(List.of(Genre.CRIME)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("404 NOT_FOUND \"No users found!\"");

        user.setFavouriteGenres(new ArrayList<>());
        userRepository.save(user);

        assertThatThrownBy(() -> userLookupService.getUsersByFavouriteGenres(List.of(Genre.CRIME)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("404 NOT_FOUND \"No users found!\"");

        user.setFavouriteGenres(new ArrayList<>(List.of(Genre.ROMANCE, Genre.BIOGRAPHY)));
        userRepository.save(user);

        assertThatThrownBy(() -> userLookupService.getUsersByFavouriteGenres(List.of(Genre.CRIME)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("404 NOT_FOUND \"No users found!\"");
    }

    @AfterAll
    public static void stop() {
        wireMockServer.stop();
    }
}
