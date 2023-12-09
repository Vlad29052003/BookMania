package nl.tudelft.sem.template.authentication.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.transaction.Transactional;
import nl.tudelft.sem.template.authentication.authentication.JwtService;
import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.book.BookRepository;
import nl.tudelft.sem.template.authentication.domain.book.Genre;
import nl.tudelft.sem.template.authentication.domain.report.Report;
import nl.tudelft.sem.template.authentication.domain.report.ReportRepository;
import nl.tudelft.sem.template.authentication.domain.report.ReportType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;

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
    public void testUpdateBannedStatus() {
        UserRepository ur = mock(UserRepository.class);
        ReportRepository rr = mock(ReportRepository.class);
        UserService us = new UserService(ur, rr, mock(BookRepository.class), mock(JwtService.class));

        String adminToken = "adminToken";
        String userToken = "userToken";
        Username username = new Username("user");
        AppUser appUser = new AppUser(username, "mail", new HashedPassword("pwd"));
        appUser.setId(UUID.randomUUID());

        when(us.getAuthority(adminToken)).thenReturn(Authority.ADMIN);
        when(us.getAuthority(userToken)).thenReturn(Authority.REGULAR_USER);

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                                    () -> us.updateBannedStatus(username, userToken));
        assertEquals(e.getStatus(), HttpStatus.UNAUTHORIZED);
        verify(ur, never()).saveAndFlush(any(AppUser.class));

        when(ur.findByUsername(username)).thenReturn(Optional.empty());
        e = assertThrows(ResponseStatusException.class,
                                    () -> us.updateBannedStatus(username, adminToken));
        assertEquals(e.getStatus(), HttpStatus.NOT_FOUND);
        verify(ur, never()).saveAndFlush(any(AppUser.class));

        when(ur.findByUsername(username)).thenReturn(Optional.of(appUser));
        when(rr.getByUserId(appUser.getId().toString())).thenReturn(Optional.empty());
        e = assertThrows(ResponseStatusException.class,
                                    () -> us.updateBannedStatus(username, adminToken));
        assertEquals(e.getStatus(), HttpStatus.NOT_FOUND);
        verify(ur, never()).saveAndFlush(any(AppUser.class));

        Report report = new Report(UUID.randomUUID(), ReportType.REVIEW, appUser.getId().toString(), "text");
        when(rr.getByUserId(appUser.getId().toString())).thenReturn(Optional.of(List.of(report)));
        assertDoesNotThrow(() -> us.updateBannedStatus(username, adminToken));
        assertDoesNotThrow(() -> us.updateBannedStatus(username, adminToken));
        verify(ur, times(2)).saveAndFlush(any(AppUser.class));
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

        userService.delete(retrievedUser.getUsername());

        assertThatThrownBy(() -> userService.getUserByUsername(retrievedUser.getUsername()))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage(UserService.NO_SUCH_USER);
    }
}
