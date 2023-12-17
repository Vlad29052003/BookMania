package nl.tudelft.sem.template.authentication.controller;

import static nl.tudelft.sem.template.authentication.domain.user.UserService.NO_SUCH_USER;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import nl.tudelft.sem.template.authentication.controllers.UnauthorizedController;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.HashedPassword;
import nl.tudelft.sem.template.authentication.domain.user.UserService;
import nl.tudelft.sem.template.authentication.domain.user.Username;
import nl.tudelft.sem.template.authentication.models.UserModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;

public class UnauthorizedControllerTests {
    private transient UserService userService;
    private transient UnauthorizedController unauthorizedController;
    private transient UserModel userModel;
    private transient UUID userId;

    @BeforeEach
    public void setUp() {
        this.userService = mock(UserService.class);
        this.unauthorizedController = new UnauthorizedController(userService);
        AppUser appUser = new AppUser(new Username("user"), "email@mail.com", new HashedPassword("mock"));
        this.userModel = new UserModel(appUser);
        this.userId = UUID.randomUUID();
    }

    @Test
    public void testOk() {
        when(userService.getUserDetails(userId)).thenReturn(userModel);

        assertThat(unauthorizedController.getUserDetails(userId)).isEqualTo(ResponseEntity.ok(userModel));
    }

    @Test
    public void testError() {
        when(userService.getUserDetails(userId)).thenThrow(new UsernameNotFoundException(NO_SUCH_USER));

        assertThatThrownBy(() -> unauthorizedController.getUserDetails(userId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("404 NOT_FOUND \"User does not exist!\"");
    }
}
