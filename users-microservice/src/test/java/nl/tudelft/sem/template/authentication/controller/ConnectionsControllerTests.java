package nl.tudelft.sem.template.authentication.controller;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import nl.tudelft.sem.template.authentication.controllers.ConnectionsController;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.domain.user.HashedPassword;
import nl.tudelft.sem.template.authentication.domain.user.UserService;
import nl.tudelft.sem.template.authentication.domain.user.Username;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
public class ConnectionsControllerTests {
    private transient UserService userService;

    private transient ConnectionsController connectionsController;
    private transient AppUser user;
    private Username username;

    /**
     * Sets up the testing environment.
     */
    @BeforeEach
    public void setUp() {
        this.userService = mock(UserService.class);
        this.connectionsController = new ConnectionsController(userService);
        Authentication authenticationMock = mock(Authentication.class);
        when(authenticationMock.getName()).thenReturn("user");
        when(authenticationMock.isAuthenticated()).thenReturn(true);
        doReturn(List.of(Authority.REGULAR_USER)).when(authenticationMock).getAuthorities();
        SecurityContext securityContextMock = mock(SecurityContext.class);
        when(securityContextMock.getAuthentication()).thenReturn(authenticationMock);
        SecurityContextHolder.setContext(securityContextMock);

        this.user = new AppUser(new Username("user"), "email@mail.com", new HashedPassword("hash"));
        when(userService.getUserByUsername(new Username("user"))).thenReturn(user);
        this.username = new Username("user");
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testFollowUser() {
        String userToFollow = "userToFollow";

        assertThat(connectionsController.followUser(userToFollow))
                .isEqualTo(ResponseEntity.ok().build());
        verify(userService, times(1)).followUser(username, new Username(userToFollow));
    }

    @Test
    public void testUnfollowUser() {
        String userToUnfollow = "userToUnfollow";

        assertThat(connectionsController.unfollowUser(userToUnfollow))
                .isEqualTo(ResponseEntity.ok().build());
        verify(userService, times(1)).unfollowUser(username, new Username(userToUnfollow));
    }

    @Test
    public void testGetFollowers() {
        String testUser = "testUser";

        assertThat(connectionsController.getFollowers(testUser))
                .isEqualTo(ResponseEntity.ok(new ArrayList<>()));
        verify(userService, times(1)).getFollowers(testUser);
    }

    @Test
    public void testGetFollowing() {
        String testUser = "testUser";

        assertThat(connectionsController.getFollowing(testUser))
                .isEqualTo(ResponseEntity.ok(new ArrayList<>()));
        verify(userService, times(1)).getFollowing(testUser);
    }
}
