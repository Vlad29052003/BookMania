package nl.tudelft.sem.template.authentication.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.book.BookRepository;
import nl.tudelft.sem.template.authentication.domain.book.Genre;
import nl.tudelft.sem.template.authentication.models.UserModel;
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
public class UserLookupServiceTests {
    @Autowired
    private transient UserLookupService userLookupService;
    @Autowired
    private transient UserRepository userRepository;
    @Autowired
    private transient BookRepository bookRepository;
    private transient AppUser user1;
    private transient AppUser user2;
    private transient Book book;

    /**
     * Sets up the testing environment.
     */
    @BeforeEach
    public void setUp() {
        PasswordHashingService passwordHashingService = mock(PasswordHashingService.class);
        when(passwordHashingService.hash(new Password("someOtherHash1!"))).thenReturn(new HashedPassword("someHash"));

        Username username1 = new Username("user");
        String email = "email@gmail.com";
        HashedPassword password = passwordHashingService.hash(new Password("someHash1!"));
        user1 = new AppUser(username1, email, password);
        userRepository.saveAndFlush(user1);
        user1 = userRepository.findByUsername(username1).get();

        String email2 = "email2@gmail.com";
        Username username2 = new Username("andrei");
        HashedPassword password2 = passwordHashingService.hash(new Password("someHash2!"));
        user2 = new AppUser(username2, email2, password2);
        userRepository.saveAndFlush(user2);
        user2 = userRepository.findByUsername(username2).get();


        book = new Book("title", List.of("authorName"), List.of(Genre.CRIME),
                "description", 20);
        bookRepository.save(book);
        book = bookRepository.findAll().get(0);
    }

    /**
     * Tests whether the user is correctly returned when using the proper query string.
     *
     */
    @Test
    public void userSearchByName_worksCorrectly() {
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
        List<String> foundUsers = userLookupService.getUsersByName("")
                .stream().map(UserModel::getUsername).collect(Collectors.toList());

        assertThat(foundUsers).containsExactlyInAnyOrder("user", "andrei");
    }

    @Test
    public void userSearchByName_worksCorrectly_deactivatedUser() {
        user1.setDeactivated(true);
        userRepository.saveAndFlush(user1);

        List<String> foundUsers = userLookupService.getUsersByName("user")
                .stream().map(UserModel::getUsername).collect(Collectors.toList());
        List<String> expected = new ArrayList<>();


        assertThat(foundUsers).isEqualTo(expected);
    }

    @Test
    public void userSearchByName_withPrivateUser() {
        user1.setPrivate(true);
        userRepository.saveAndFlush(user1);

        List<String> foundUsers = userLookupService.getUsersByName("")
                .stream().map(UserModel::getUsername).collect(Collectors.toList());
        List<String> expected = List.of("andrei");

        assertThat(foundUsers).containsAll(expected);
    }

    @Test
    @Transactional
    public void testUserSearchByFavouriteBook() {
        user1.setFavouriteBook(book);
        userRepository.save(user1);

        List<String> foundUsers = userLookupService.getUsersByFavouriteBook(book.getId())
                .stream().map(UserModel::getUsername).collect(Collectors.toList());
        List<String> expected = List.of("user");

        assertThat(foundUsers).containsAll(expected);
    }

    @Test
    public void testUserSearchByFavouriteBookNoResults1() {
        user1.setFavouriteBook(book);
        userRepository.save(user1);

        assertThatThrownBy(() -> userLookupService.getUsersByFavouriteBook(UUID.randomUUID()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("No users with this favourite book found!");
    }

    @Test
    public void testUserSearchByFavouriteBookNoResults2() {
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
        user1.setFavouriteGenres(new ArrayList<>(List.of(Genre.CRIME)));
        userRepository.saveAndFlush(user1);

        List<String> foundUsers = userLookupService.getUsersByFavouriteGenres(List.of(Genre.CRIME))
                .stream().map(UserModel::getUsername).collect(Collectors.toList());
        List<String> expected = List.of("user");

        assertThat(foundUsers).containsAll(expected);
    }

    @Test
    @Transactional
    public void testNoUsersFoundWhileSearchByGenre() {
        assertThatThrownBy(() -> userLookupService.getUsersByFavouriteGenres(List.of(Genre.CRIME)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("404 NOT_FOUND \"No users found!\"");

        user1.setDeactivated(true);
        userRepository.save(user1);

        assertThatThrownBy(() -> userLookupService.getUsersByFavouriteGenres(List.of(Genre.CRIME)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("404 NOT_FOUND \"No users found!\"");

        user1.setDeactivated(false);
        user1.setPrivate(true);
        userRepository.save(user1);

        assertThatThrownBy(() -> userLookupService.getUsersByFavouriteGenres(List.of(Genre.CRIME)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("404 NOT_FOUND \"No users found!\"");

        user1.setPrivate(false);
        user1.setFavouriteGenres(null);
        userRepository.save(user1);

        assertThatThrownBy(() -> userLookupService.getUsersByFavouriteGenres(List.of(Genre.CRIME)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("404 NOT_FOUND \"No users found!\"");

        user1.setFavouriteGenres(new ArrayList<>());
        userRepository.save(user1);

        assertThatThrownBy(() -> userLookupService.getUsersByFavouriteGenres(List.of(Genre.CRIME)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("404 NOT_FOUND \"No users found!\"");

        user1.setFavouriteGenres(new ArrayList<>(List.of(Genre.ROMANCE, Genre.BIOGRAPHY)));
        userRepository.save(user1);

        assertThatThrownBy(() -> userLookupService.getUsersByFavouriteGenres(List.of(Genre.CRIME)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("404 NOT_FOUND \"No users found!\"");
    }
}
