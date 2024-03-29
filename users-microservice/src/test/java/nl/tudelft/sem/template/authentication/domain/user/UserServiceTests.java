package nl.tudelft.sem.template.authentication.domain.user;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static nl.tudelft.sem.template.authentication.application.Constants.NO_SUCH_USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.transaction.Transactional;
import nl.tudelft.sem.template.authentication.application.user.UserEventsListener;
import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.book.BookRepository;
import nl.tudelft.sem.template.authentication.domain.book.Genre;
import nl.tudelft.sem.template.authentication.domain.report.Report;
import nl.tudelft.sem.template.authentication.domain.report.ReportRepository;
import nl.tudelft.sem.template.authentication.domain.report.ReportType;
import nl.tudelft.sem.template.authentication.domain.rolechange.RoleChange;
import nl.tudelft.sem.template.authentication.domain.rolechange.RoleChangeRepository;
import nl.tudelft.sem.template.authentication.models.UserModel;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UserServiceTests {
    @Autowired
    private transient UserService userService;

    @Autowired
    private transient UserRepository userRepository;

    @Autowired
    private transient BookRepository bookRepository;

    @Autowired
    private transient ReportRepository reportRepository;

    @Autowired
    private transient RoleChangeRepository roleChangeRepository;
    private static ByteArrayOutputStream outputStreamCaptor;

    private static WireMockServer wireMockServer;

    private static final String BOOKSHELF_PATH = "/bookshelf_service/user";

    private static final String REVIEW_PATH = "/b/user";

    /**
     * Set up for testing.
     */
    @BeforeAll
    public static void setUp() {
        wireMockServer = new WireMockServer(new WireMockConfiguration().port(8080));
        wireMockServer.start();

        configureFor("localhost", 8080);

        outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));

        UserEventsListener.REVIEW_URL = "http://localhost:8080" + REVIEW_PATH;
    }

    @Test
    public void testGetUserByNetId() {
        Username username = new Username("username");
        String email = "test@email.com";
        HashedPassword password = new HashedPassword("pass123");
        assertThatThrownBy(() -> userService.getUserByUsername(username))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage(NO_SUCH_USER);

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
                .hasMessage(NO_SUCH_USER);

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
                .hasMessage(NO_SUCH_USER);

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
                .hasMessage(NO_SUCH_USER);

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
                .hasMessage(NO_SUCH_USER);

        AppUser user = new AppUser(username, email, password);
        userRepository.save(user);
        AppUser retrievedUser = userService.getUserByUsername(username);
        assertThat(retrievedUser.getLocation()).isNull();


        userService.updateLocation(username, newLocation);
        retrievedUser = userService.getUserByUsername(username);
        assertThat(retrievedUser.getLocation()).isEqualTo(newLocation);
    }

    @Test
    public void testUpdateUsername() throws UsernameAlreadyInUseException {
        Username username = new Username("username");
        String email = "test@email.com";
        HashedPassword password = new HashedPassword("pass123");
        String badUsername = "1User";
        String newUsername = "NewUsername";
        assertThatThrownBy(() -> userService.updateUsername(username, newUsername))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage(NO_SUCH_USER);

        AppUser user = new AppUser(username, email, password);
        userRepository.save(user);

        assertThatThrownBy(() -> userService.updateUsername(username, badUsername))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Illegal username:"
                        + " the username should start with a letter and not contain any special characters!");

        userService.updateUsername(username, newUsername);
        AppUser retrievedUser = userService.getUserByUsername(new Username(newUsername));
        assertThat(retrievedUser.getUsername().toString()).isEqualTo(newUsername);

        Username username1 = new Username("otherUsername");
        String email1 = "other@email.com";
        AppUser newUser = new AppUser(username1, email1, password);
        userRepository.save(newUser);
        assertThatThrownBy(() -> userService.updateUsername(username1, newUsername))
                .isInstanceOf(UsernameAlreadyInUseException.class)
                .hasMessage(newUsername + " username is already in use!");
    }

    @Test
    public void testUpdateEmail() throws EmailAlreadyInUseException {
        Username username = new Username("username");
        String email = "test@email.com";
        HashedPassword password = new HashedPassword("pass123");
        String badEmail = "notAnEmail.com";
        String newEmail = "goodExample@gmail.com";
        assertThatThrownBy(() -> userService.updateEmail(username, newEmail))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage(NO_SUCH_USER);

        AppUser user = new AppUser(username, email, password);
        userRepository.save(user);

        assertThatThrownBy(() -> userService.updateEmail(username, badEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Illegal email address!");

        userService.updateEmail(username, newEmail);
        AppUser retrievedUser = userService.getUserByUsername(username);
        assertThat(retrievedUser.getEmail()).isEqualTo(newEmail);

        Username username1 = new Username("otherUsername");
        String email1 = "other@email.com";
        AppUser newUser = new AppUser(username1, email1, password);
        userRepository.save(newUser);
        assertThatThrownBy(() -> userService.updateEmail(username1, newEmail))
                .isInstanceOf(EmailAlreadyInUseException.class)
                .hasMessage(newEmail + " email is already in use!");
    }

    @Test
    public void testUpdatePassword() {
        Username username = new Username("username");
        String email = "test@email.com";
        HashedPassword password = new HashedPassword("pass123");
        HashedPassword newPassword = new HashedPassword("NewPass123!");
        assertThatThrownBy(() -> userService.updatePassword(username, newPassword))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage(NO_SUCH_USER);

        AppUser user = new AppUser(username, email, password);
        userRepository.save(user);

        userService.updatePassword(username, newPassword);
        AppUser retrievedUser = userService.getUserByUsername(username);
        assertThat(retrievedUser.getPassword()).isEqualTo(newPassword);
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
                .hasMessage(NO_SUCH_USER);

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
                .hasMessage(NO_SUCH_USER);

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
        assertThat(retrievedUser.getFavouriteBook().getGenre().toArray()).isEqualTo(newBook.getGenre().toArray());
        assertThat(retrievedUser.getFavouriteBook().getDescription()).isEqualTo(newBook.getDescription());
    }

    @Test
    @Transactional
    public void testUpdateBannedStatus() {
        Username username = new Username("user");
        String email = "user@user.com";
        HashedPassword hashedPassword = new HashedPassword("pass");

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> userService.updateBannedStatus(username, true, "ADMIN"));
        assertThat(e.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(e.getMessage()).isEqualTo("404 NOT_FOUND \"User does not exist!\"");

        AppUser user = new AppUser(username, email, hashedPassword);
        userRepository.save(user);

        e = assertThrows(ResponseStatusException.class,
                () -> userService.updateBannedStatus(username, true, "REGULAR_USER"));
        assertThat(e.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(e.getMessage()).isEqualTo("401 UNAUTHORIZED \"Only admins can ban / unban a user!\"");

        Report report = new Report(UUID.randomUUID(), ReportType.REVIEW, user.getId().toString(), "text");
        while (report.getId().equals(user.getId())) {
            report.setId(UUID.randomUUID());
        }
        reportRepository.save(report);

        userService.updateBannedStatus(username, true, "ADMIN");
        assertThat(reportRepository.getByUserId(user.getId().toString())).isEmpty();
        assertThat(userRepository.findByUsername(username)).isPresent();
        assertThat(userRepository.findByUsername(username).get().isDeactivated()).isTrue();

        e = assertThrows(ResponseStatusException.class,
                () -> userService.updateBannedStatus(username, true, "ADMIN"));
        assertThat(e.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(e.getMessage()).isEqualTo("400 BAD_REQUEST \"User is already banned!\"");

        userService.updateBannedStatus(username, false, "ADMIN");
        assertThat(userRepository.findByUsername(username)).isPresent();
        assertThat(userRepository.findByUsername(username).get().isDeactivated()).isFalse();

        e = assertThrows(ResponseStatusException.class,
                () -> userService.updateBannedStatus(username, false, "ADMIN"));
        assertThat(e.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(e.getMessage()).isEqualTo("400 BAD_REQUEST \"User is already not banned!\"");
    }

    @Test
    @Transactional
    public void testUpdateAuthority() {
        UUID userId = UUID.randomUUID();

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> userService.updateAuthority(userId, Authority.AUTHOR, "REGULAR_USER"));
        assertThat(e.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);

        e = assertThrows(ResponseStatusException.class,
                () -> userService.updateAuthority(userId, Authority.AUTHOR, "ADMIN"));
        assertThat(e.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);

        String email = "user@user.com";
        HashedPassword hashedPassword = new HashedPassword("pass");
        Username username = new Username("user");
        AppUser user = new AppUser(username, email, hashedPassword);
        userRepository.save(user);
        user = userRepository.findAll().get(0);
        UUID finalUserId = user.getId();
        e = assertThrows(ResponseStatusException.class,
                () -> userService.updateAuthority(finalUserId, Authority.AUTHOR, "ADMIN"));
        assertThat(e.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);

        RoleChange roleChange = new RoleChange(user.getId(), Authority.AUTHOR, "123-456");
        roleChangeRepository.save(roleChange);

        userService.updateAuthority(finalUserId, Authority.AUTHOR, "ADMIN");
        assertThat(roleChangeRepository.findAll()).isEmpty();
        assertThat(userRepository.findByUsername(username)).isPresent();
        assertThat(userRepository.findByUsername(username).get().getAuthority()).isEqualTo(Authority.AUTHOR);
    }

    @Test
    @Transactional
    public void testDeleteUser() {
        Username username = new Username("username");
        String email = "test@email.com";
        HashedPassword password = new HashedPassword("pass123");
        assertThatThrownBy(() -> userService.getUserByUsername(username))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage(NO_SUCH_USER);

        AppUser user = new AppUser(username, email, password);
        userRepository.save(user);
        AppUser retrievedUser = userService.getUserByUsername(username);

        UUID userId = retrievedUser.getId();

        stubFor(delete(urlEqualTo(BOOKSHELF_PATH + "?userId=" + userId))
                .willReturn(aResponse().withStatus(200)));
        stubFor(delete(urlEqualTo(REVIEW_PATH + "/" + userId
                + "/" + userId))
                .willReturn(aResponse().withStatus(200)));

        outputStreamCaptor.reset();

        userService.delete(retrievedUser.getUsername(), retrievedUser.getUsername());

        assertThat(outputStreamCaptor.toString().trim()).contains("Account of user with id " + user.getId().toString()
                + " was deleted.");

        assertThatThrownBy(() -> userService.getUserByUsername(retrievedUser.getUsername()))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage(NO_SUCH_USER);
    }

    @Test
    @Transactional
    public void testDeleteUserNotFound() {
        AppUser user = new AppUser(new Username("nonExistentUser"), "email@email.com", new HashedPassword("password"));
        AppUser requester = new AppUser(new Username("nonExistentRequester"),
                "requester@email.com", new HashedPassword("password"));
        assertThatThrownBy(() ->
                userService.delete(new Username("nonExistentUser"), new Username("nonExistentUser")))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage(NO_SUCH_USER);

        userRepository.save(user);
        assertThatThrownBy(() -> userService.delete(user.getUsername(), requester.getUsername()))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Requester does not exist!");
    }

    @Test
    public void testUpdatePrivacy() {
        Username username = new Username("username");
        String email = "test@email.com";
        HashedPassword password = new HashedPassword("pass123");
        assertThatThrownBy(() -> userService.updatePrivacy(username, true))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage(NO_SUCH_USER);

        AppUser user = new AppUser(username, email, password);
        userRepository.save(user);
        AppUser retrievedUser = userService.getUserByUsername(username);
        assertThat(retrievedUser.isPrivate()).isFalse();


        userService.updatePrivacy(username, true);
        retrievedUser = userService.getUserByUsername(username);
        assertThat(retrievedUser.isPrivate()).isTrue();
    }

    @Test
    public void testUpdate2fa() {
        Username username = new Username("username");
        String email = "test@email.com";
        HashedPassword password = new HashedPassword("pass123");
        assertThatThrownBy(() -> userService.update2fa(username, true))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage(NO_SUCH_USER);

        AppUser user = new AppUser(username, email, password);
        userRepository.save(user);
        AppUser retrievedUser = userService.getUserByUsername(username);
        assertThat(retrievedUser.is2faEnabled()).isFalse();


        userService.update2fa(username, true);
        retrievedUser = userService.getUserByUsername(username);
        assertThat(retrievedUser.is2faEnabled()).isTrue();
    }

    @Test
    @Transactional
    public void testGetUserDetails() {
        assertThatThrownBy(() -> userService.getUserDetails(UUID.randomUUID()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("404 NOT_FOUND \"User does not exist!\"");

        Username username = new Username("username");
        String email = "test@email.com";
        HashedPassword password = new HashedPassword("pass123");
        AppUser user = new AppUser(username, email, password);
        userRepository.save(user);
        user = userRepository.findAll().get(0);

        UserModel expected = new UserModel(user);

        assertThat(userService.getUserDetails(user.getId())).isEqualTo(expected);
    }

    @Test
    @Transactional
    public void testUpdatePrivacyInexistentUser() {
        assertThatThrownBy(() -> userService.updatePrivacy(new Username("inexistentUsername"), true))
                .isInstanceOf(UsernameNotFoundException.class).hasMessage(NO_SUCH_USER);
    }

    @Test
    public void testFollowUser() {
        Username username = new Username("username");
        String email = "test@email.com";
        HashedPassword password = new HashedPassword("pass123");


        AppUser user = new AppUser(username, email, password);
        userRepository.save(user);


        Username username2 = new Username("usernameToFollow");
        String email2 = "test2@email.com";
        HashedPassword password2 = new HashedPassword("pass123");
        AppUser user2 = new AppUser(username2, email2, password2);
        userRepository.save(user2);

        userService.followUser(username, username2);
        AppUser retrievedUser = userService.getUserByUsername(username);
        AppUser retrievedUser2 = userService.getUserByUsername(username2);
        assertThat(retrievedUser.getFollows().get(0).getUsername()).isEqualTo(username2);
        assertThat(retrievedUser2.getFollowedBy().get(0).getUsername()).isEqualTo(username);
    }

    @Test
    public void testUnfollowUser() {
        Username username = new Username("username");
        String email = "test@email.com";
        HashedPassword password = new HashedPassword("pass123");


        AppUser user = new AppUser(username, email, password);
        userRepository.save(user);


        Username username2 = new Username("usernameToFollow");
        String email2 = "test2@email.com";
        HashedPassword password2 = new HashedPassword("pass123");
        AppUser user2 = new AppUser(username2, email2, password2);
        userRepository.save(user2);

        userService.followUser(username, username2);
        userService.unfollowUser(username, username2);
        AppUser retrievedUser = userService.getUserByUsername(username);
        AppUser retrievedUser2 = userService.getUserByUsername(username2);
        assertThat(retrievedUser.getFollows().isEmpty()).isTrue();
        assertThat(retrievedUser2.getFollowedBy().isEmpty()).isTrue();
    }


    @Test
    public void testFollowAndDelete() {
        Username username = new Username("username");
        String email = "test@email.com";
        HashedPassword password = new HashedPassword("pass123");


        AppUser user = new AppUser(username, email, password);
        userRepository.save(user);


        Username username2 = new Username("usernameToFollow");
        String email2 = "test2@email.com";
        HashedPassword password2 = new HashedPassword("pass123");
        AppUser user2 = new AppUser(username2, email2, password2);
        userRepository.save(user2);

        AppUser retrievedUser = userService.getUserByUsername(username);

        UUID userId = retrievedUser.getId();

        stubFor(delete(urlEqualTo(BOOKSHELF_PATH + "?userId=" + userId))
                .willReturn(aResponse().withStatus(200)));
        stubFor(delete(urlEqualTo(REVIEW_PATH + "/" + userId
                + "/" + userId))
                .willReturn(aResponse().withStatus(200)));


        userService.followUser(username, username2);
        userService.delete(username, username);

        AppUser retrievedUser2 = userService.getUserByUsername(username2);
        assertThatThrownBy(() -> userService.getUserByUsername(username))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage(NO_SUCH_USER);
        assertThat(retrievedUser2.getFollowedBy().isEmpty()).isTrue();
    }

    @Test
    public void testGetFollowersAndFollowing() {
        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> userService.getFollowers("user1"));
        assertThat(e.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        e = assertThrows(ResponseStatusException.class,
                () -> userService.getFollowing("user1"));
        assertThat(e.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);

        AppUser user1 = new AppUser(new Username("user1"), "user1@email.com", new HashedPassword("pass1"));
        AppUser user2 = new AppUser(new Username("user2"), "user2@email.com", new HashedPassword("pass2"));
        userRepository.save(user1);
        userRepository.save(user2);
        assertThat(userService.getFollowers("user2")).isEqualTo(new ArrayList<>());
        assertThat(userService.getFollowing("user1")).isEqualTo(new ArrayList<>());

        userService.followUser(user1.getUsername(), user2.getUsername());
        List<UserModel> followersUser2 = userService.getFollowers("user2");
        assertThat(followersUser2.size()).isEqualTo(1);
        assertThat(followersUser2.get(0).getUsername()).isEqualTo("user1");

        List<UserModel> followingUser1 = userService.getFollowing("user1");
        assertThat(followingUser1.size()).isEqualTo(1);
        assertThat(followingUser1.get(0).getUsername()).isEqualTo("user2");
    }

    @Test
    public void testFollowException() {
        Username username1 = new Username("User1");
        Username username2 = new Username("User2");
        assertThatThrownBy(() -> userService.followUser(username1, username2))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("404 NOT_FOUND \"User does not exist!\"");

        AppUser user1 = new AppUser(username1, "email@mail.com", new HashedPassword("hash"));
        userRepository.saveAndFlush(user1);

        assertThatThrownBy(() -> userService.followUser(username1, username2))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("404 NOT_FOUND \"User does not exist!\"");

        assertThatThrownBy(() -> userService.followUser(username2, username1))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("404 NOT_FOUND \"User does not exist!\"");

        assertThatThrownBy(() -> userService.followUser(username1, username1))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("400 BAD_REQUEST \"You cannot follow yourself!\"");
    }

    @AfterAll
    public static void stop() {
        wireMockServer.stop();
    }
}
