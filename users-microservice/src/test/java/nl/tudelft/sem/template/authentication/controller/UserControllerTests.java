package nl.tudelft.sem.template.authentication.controller;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import nl.tudelft.sem.template.authentication.controllers.UserController;
import nl.tudelft.sem.template.authentication.domain.book.Genre;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.domain.user.EmailAlreadyInUseException;
import nl.tudelft.sem.template.authentication.domain.user.HashedPassword;
import nl.tudelft.sem.template.authentication.domain.user.PasswordHashingService;
import nl.tudelft.sem.template.authentication.domain.user.UserService;
import nl.tudelft.sem.template.authentication.domain.user.Username;
import nl.tudelft.sem.template.authentication.domain.user.UsernameAlreadyInUseException;
import nl.tudelft.sem.template.authentication.models.BanUserRequestModel;
import nl.tudelft.sem.template.authentication.models.UserProfile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class UserControllerTests {
    private transient UserService userService;
    private transient UserController userController;
    private transient PasswordHashingService passwordHashingService;
    private transient AppUser user;
    private transient UserProfile userProfile;
    private Username username;

    /**
     * Sets up the testing environment.
     */
    @BeforeEach
    public void setUp() {
        this.userService = mock(UserService.class);
        this.passwordHashingService = mock(PasswordHashingService.class);
        this.userController = new UserController(userService, passwordHashingService);

        Authentication authenticationMock = mock(Authentication.class);
        when(authenticationMock.getName()).thenReturn("user");
        doReturn(List.of(Authority.REGULAR_USER)).when(authenticationMock).getAuthorities();
        SecurityContext securityContextMock = mock(SecurityContext.class);
        when(securityContextMock.getAuthentication()).thenReturn(authenticationMock);
        SecurityContextHolder.setContext(securityContextMock);

        this.user = new AppUser(new Username("user"), "email@mail.com", new HashedPassword("hash"));
        when(userService.getUserByUsername(new Username("user"))).thenReturn(user);

        this.userProfile = new UserProfile(user);

        this.username = new Username("user");
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testGetUser() {
        assertThat(userController.getUserByUsername()).isEqualTo(ResponseEntity.ok(userProfile));
        verify(userService, times(1)).getUserByUsername(username);
    }

    @Test
    public void testGetPicture() {
        byte[] picture = new byte[5];
        user.setPicture(picture);
        assertThat(userController.getUserPictureByUsername()).isEqualTo(ResponseEntity.ok(picture));
        verify(userService, times(1)).getUserByUsername(username);
    }

    @Test
    public void testUpdateName() {
        String newName = "new name";
        assertThat(userController.updateName(newName)).isEqualTo(ResponseEntity.ok().build());
        verify(userService, times(1)).updateName(username, newName);
    }

    @Test
    public void testUpdateBio() {
        String newBio = "new bio";
        assertThat(userController.updateBio(newBio)).isEqualTo(ResponseEntity.ok().build());
        verify(userService, times(1)).updateBio(username, newBio);
    }

    @Test
    public void testUpdatePicture() {
        byte[] picture = new byte[5];
        assertThat(userController.updatePicture(picture)).isEqualTo(ResponseEntity.ok().build());
        verify(userService, times(1)).updatePicture(username, picture);
    }

    @Test
    public void testUpdateLocation() {
        String newLocation = "new location";
        assertThat(userController.updateLocation(newLocation)).isEqualTo(ResponseEntity.ok().build());
        verify(userService, times(1)).updateLocation(username, newLocation);
    }

    @Test
    public void testUpdateFavGenres() {
        List<Genre> newFavGenres = List.of(Genre.CRIME);
        assertThat(userController.updateFavouriteGenres(newFavGenres)).isEqualTo(ResponseEntity.ok().build());
        verify(userService, times(1)).updateFavouriteGenres(username, newFavGenres);
    }

    @Test
    public void testUpdateFavBook() {
        String newFavBookId = UUID.randomUUID().toString();
        assertThat(userController.updateFavouriteBook(newFavBookId)).isEqualTo(ResponseEntity.ok().build());
        verify(userService, times(1)).updateFavouriteBook(username, newFavBookId);
    }

    @Test
    public void testDelete() {
        AppUser user = new AppUser(new Username("user"), "email@email.com", new HashedPassword("hash"));
        user.setId(UUID.randomUUID());

        when(userService.getUserByUsername(username)).thenReturn(user);

        assertThat(userController.delete()).isEqualTo(ResponseEntity.ok().build());
        verify(userService, times(1)).delete(username, username);
    }

    @Test
    public void testDeleteByAdmin() {
        Username usernameAdmin = new Username("admin");
        AppUser admin = new AppUser(usernameAdmin, "admin@email.com", new HashedPassword("hash"));
        admin.setId(UUID.randomUUID());

        when(userService.getUserByUsername(usernameAdmin)).thenReturn(admin);

        assertThat(userController.deleteByAdmin(username.toString())).isEqualTo(ResponseEntity.ok().build());
    }

    @Test
    public void testUpdateBannedStatus() {
        BanUserRequestModel banUserRequestModel = new BanUserRequestModel();
        banUserRequestModel.setUsername(username.toString());
        banUserRequestModel.setBanned(true);
        assertThat(userController.updateBannedStatus(banUserRequestModel))
                .isEqualTo(ResponseEntity.ok().build());
        verify(userService, times(1)).updateBannedStatus(username, true, Authority.REGULAR_USER.toString());
    }

    @Test
    public void testUpdatePrivacy() {
        assertThat(userController.updatePrivacy("true"))
                .isEqualTo(ResponseEntity.ok().build());
        verify(userService, times(1)).updatePrivacy(username, true);
    }

    @Test
    public void testUpdatePassword() {
        String newPass = "newPass@123";
        HashedPassword hashedPassword = new HashedPassword(newPass);
        when(passwordHashingService.hash(any())).thenReturn(hashedPassword);
        assertThat(userController.updatePassword(newPass))
                .isEqualTo(ResponseEntity.ok().build());
        verify(userService, times(1)).updatePassword(username, hashedPassword);
    }

    @Test
    public void testUpdateUsername() throws UsernameAlreadyInUseException {
        String newUsername = "newUsername";
        assertThat(userController.updateUsername(newUsername))
                .isEqualTo(ResponseEntity.ok().build());
        verify(userService, times(1)).updateUsername(username, newUsername);
    }

    @Test
    public void testUpdateEmail() throws EmailAlreadyInUseException {
        String newEmail = "new@email.com";
        assertThat(userController.updateEmail(newEmail))
                .isEqualTo(ResponseEntity.ok().build());
        verify(userService, times(1)).updateEmail(username, newEmail);
    }

}
