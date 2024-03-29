package nl.tudelft.sem.template.authentication.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import nl.tudelft.sem.template.authentication.controllers.UserSearchController;
import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.book.Genre;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.HashedPassword;
import nl.tudelft.sem.template.authentication.domain.user.UserLookupService;
import nl.tudelft.sem.template.authentication.domain.user.Username;
import nl.tudelft.sem.template.authentication.models.UserModel;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

@ActiveProfiles("test")
public class UserSearchControllerTests {
    private final transient UserLookupService userLookupService = mock(UserLookupService.class);
    private final transient UserSearchController userSearchController =
            new UserSearchController(userLookupService);

    @Test
    public void getUsersTest() {
        when(userLookupService.getUsersByName("name")).thenReturn(List.of(new UserModel()));

        Iterable<UserModel> res = userSearchController.getUser("name").getBody();
        Iterable<UserModel> exp = List.of(new UserModel());
        assertThat(res).containsExactlyInAnyOrderElementsOf(exp);
    }

    @Test
    public void getUsersExceptionTest() {
        when(userLookupService.getUsersByName("name")).thenThrow(new RuntimeException());

        assertThatThrownBy(() -> userSearchController.getUser("name"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("400 BAD_REQUEST");
    }

    @Test
    public void getUsersByFavBookTest() {
        AppUser user = new AppUser(new Username("username"), "email@gmail.com", new HashedPassword("Password123!"));
        Book book = new Book("title", List.of("author"), List.of(Genre.CRIME), "description", 155);
        user.setFavouriteBook(book);

        UUID bookId = UUID.randomUUID();
        book.setId(bookId);

        UserModel userModel = new UserModel(user);

        when(userLookupService.getUsersByFavouriteBook(bookId)).thenReturn(List.of(userModel));

        var res = userSearchController.getUsersByFavouriteBook(bookId).getBody();
        Iterable<UserModel> exp = List.of(userModel);
        assertThat(exp).isEqualTo(res);
    }

    @Test
    public void getUsersByFavBookExceptionTest() {
        UUID bookId = UUID.randomUUID();
        ResponseStatusException exception = new ResponseStatusException(HttpStatus.NOT_FOUND,
                "No users found!");
        when(userLookupService.getUsersByFavouriteBook(bookId)).thenThrow(exception);

        assertThat(userSearchController.getUsersByFavouriteBook(bookId))
                .isEqualTo(new ResponseEntity<>(exception.getMessage(), exception.getStatus()));
    }

    @Test
    public void getUsersByFavGenresTest() {
        AppUser user = new AppUser(new Username("username"), "email@gmail.com", new HashedPassword("Password123!"));
        user.setFavouriteGenres(List.of(Genre.CRIME));

        UserModel userModel = new UserModel(user);

        when(userLookupService.getUsersByFavouriteGenres(List.of(Genre.CRIME))).thenReturn(List.of(userModel));

        var res = userSearchController.getUsersByFavouriteGenres(List.of(Genre.CRIME)).getBody();
        Iterable<UserModel> exp = List.of(userModel);

        assertThat(exp).isEqualTo(res);
    }

    @Test
    public void getUsersByFavGenresExceptionTest() {
        ResponseStatusException exception = new ResponseStatusException(HttpStatus.NOT_FOUND,
                "No users found!");
        when(userLookupService.getUsersByFavouriteGenres(List.of(Genre.CRIME))).thenThrow(exception);

        assertThat(userSearchController.getUsersByFavouriteGenres(List.of(Genre.CRIME)))
                .isEqualTo(new ResponseEntity<>(exception.getMessage(), exception.getStatus()));
    }
}
