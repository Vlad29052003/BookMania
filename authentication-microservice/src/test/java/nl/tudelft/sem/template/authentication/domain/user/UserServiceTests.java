package nl.tudelft.sem.template.authentication.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import javax.transaction.Transactional;
import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.book.BookRepository;
import nl.tudelft.sem.template.authentication.domain.book.Genre;
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

    @Test
    public void testGetUserByNetId() {
        NetId username = new NetId("username");
        String email = "test@email.com";
        HashedPassword password = new HashedPassword("pass123");
        assertThatThrownBy(() -> userService.getUserByNetId(username))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage(UserService.NO_SUCH_USER);

        AppUser user = new AppUser(username, email, password);
        userRepository.save(user);
        AppUser retrievedUser = userService.getUserByNetId(username);

        assertThat(retrievedUser.getNetId().toString()).isEqualTo(username.toString());
        assertThat(retrievedUser.getEmail()).isEqualTo(email);
        assertThat(retrievedUser.getPassword().toString()).isEqualTo(password.toString());
    }

    @Test
    public void testUpdateName() {
        NetId username = new NetId("username");
        String email = "test@email.com";
        HashedPassword password = new HashedPassword("pass123");
        String newName = "Name";
        assertThatThrownBy(() -> userService.updateName(username, newName))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage(UserService.NO_SUCH_USER);

        AppUser user = new AppUser(username, email, password);
        userRepository.save(user);
        AppUser retrievedUser = userService.getUserByNetId(username);
        assertThat(retrievedUser.getName()).isNull();


        userService.updateName(username, newName);
        retrievedUser = userService.getUserByNetId(username);
        assertThat(retrievedUser.getName()).isEqualTo(newName);
    }

    @Test
    public void testUpdateBio() {
        NetId username = new NetId("username");
        String email = "test@email.com";
        HashedPassword password = new HashedPassword("pass123");
        String newBio = "Bio";
        assertThatThrownBy(() -> userService.updateBio(username, newBio))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage(UserService.NO_SUCH_USER);

        AppUser user = new AppUser(username, email, password);
        userRepository.save(user);
        AppUser retrievedUser = userService.getUserByNetId(username);
        assertThat(retrievedUser.getBio()).isNull();


        userService.updateBio(username, newBio);
        retrievedUser = userService.getUserByNetId(username);
        assertThat(retrievedUser.getBio()).isEqualTo(newBio);
    }

    @Test
    public void testUpdatePicture() {
        NetId username = new NetId("username");
        String email = "test@email.com";
        HashedPassword password = new HashedPassword("pass123");
        byte[] newPicture = new byte[]{13, 24, 51, 24, 14};
        assertThatThrownBy(() -> userService.updatePicture(username, newPicture))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage(UserService.NO_SUCH_USER);

        AppUser user = new AppUser(username, email, password);
        userRepository.save(user);
        AppUser retrievedUser = userService.getUserByNetId(username);
        assertThat(retrievedUser.getPicture()).isNull();


        userService.updatePicture(username, newPicture);
        retrievedUser = userService.getUserByNetId(username);
        assertThat(retrievedUser.getPicture()).isEqualTo(newPicture);
    }

    @Test
    public void testUpdateLocation() {
        NetId username = new NetId("username");
        String email = "test@email.com";
        HashedPassword password = new HashedPassword("pass123");
        String newLocation = "Location";
        assertThatThrownBy(() -> userService.updateLocation(username, newLocation))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage(UserService.NO_SUCH_USER);

        AppUser user = new AppUser(username, email, password);
        userRepository.save(user);
        AppUser retrievedUser = userService.getUserByNetId(username);
        assertThat(retrievedUser.getLocation()).isNull();


        userService.updateLocation(username, newLocation);
        retrievedUser = userService.getUserByNetId(username);
        assertThat(retrievedUser.getLocation()).isEqualTo(newLocation);
    }

    @Test
    @Transactional
    public void testUpdateFavouriteGenres() {
        NetId username = new NetId("username");
        String email = "test@email.com";
        HashedPassword password = new HashedPassword("pass123");
        List<Genre> newFavouriteGenres = new ArrayList<>(List.of(Genre.CRIME, Genre.SCIENCE, Genre.ROMANCE));
        assertThatThrownBy(() -> userService.updateFavouriteGenres(username, newFavouriteGenres))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage(UserService.NO_SUCH_USER);

        AppUser user = new AppUser(username, email, password);
        userRepository.save(user);
        AppUser retrievedUser = userService.getUserByNetId(username);
        assertThat(retrievedUser.getFavouriteGenres()).isEmpty();

        userService.updateFavouriteGenres(username, newFavouriteGenres);
        retrievedUser = userService.getUserByNetId(username);
        assertThat(retrievedUser.getFavouriteGenres().toArray()).isEqualTo(newFavouriteGenres.toArray());
    }

    @Test
    @Transactional
    public void testUpdateFavouriteBook() {
        NetId username = new NetId("username");
        String email = "test@email.com";
        HashedPassword password = new HashedPassword("pass123");
        assertThatThrownBy(() -> userService.updateFavouriteBook(username, 1))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage(UserService.NO_SUCH_USER);

        AppUser user = new AppUser(username, email, password);
        userRepository.save(user);
        AppUser retrievedUser = userService.getUserByNetId(username);
        assertThat(retrievedUser.getFavouriteBook()).isNull();
        assertThatThrownBy(() -> userService.updateFavouriteBook(username, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The book with the given ID does not exist!");

        Book newBook = new Book("Title",
                List.of("First Author", "Second Author"),
                List.of(Genre.CRIME, Genre.SCIENCE), "Short description.");
        bookRepository.save(newBook);
        userService.updateFavouriteBook(username, 1);
        retrievedUser = userService.getUserByNetId(username);
        assertThat(retrievedUser.getFavouriteBook().getTitle()).isEqualTo(newBook.getTitle());
        assertThat(retrievedUser.getFavouriteBook().getAuthors().toArray()).isEqualTo(newBook.getAuthors().toArray());
        assertThat(retrievedUser.getFavouriteBook().getGenres().toArray()).isEqualTo(newBook.getGenres().toArray());
        assertThat(retrievedUser.getFavouriteBook().getDescription()).isEqualTo(newBook.getDescription());
    }
}