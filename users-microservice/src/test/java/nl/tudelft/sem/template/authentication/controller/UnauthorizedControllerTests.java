package nl.tudelft.sem.template.authentication.controller;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;
import nl.tudelft.sem.template.authentication.controllers.UnauthenticatedController;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.HashedPassword;
import nl.tudelft.sem.template.authentication.domain.user.UserService;
import nl.tudelft.sem.template.authentication.domain.user.Username;
import nl.tudelft.sem.template.authentication.models.UserModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

public class UnauthorizedControllerTests {
    private transient UserService userService;
    private transient UnauthenticatedController unauthorizedController;
    private transient UserModel userModel;
    private transient UUID userId;

    /**
     * Sets up the testing environment.
     */
    @BeforeEach
    public void setUp() {
        this.userService = mock(UserService.class);
        this.unauthorizedController = new UnauthenticatedController(userService);
        AppUser appUser = new AppUser(new Username("user"), "email@mail.com", new HashedPassword("mock"));
        this.userModel = new UserModel(appUser);
        this.userId = UUID.randomUUID();
    }

    @Test
    public void testOk() {
        when(userService.getUserDetails(userId)).thenReturn(userModel);
        assertThat(unauthorizedController.getUserDetails(userId)).isEqualTo(ResponseEntity.ok(userModel));
    }
}
