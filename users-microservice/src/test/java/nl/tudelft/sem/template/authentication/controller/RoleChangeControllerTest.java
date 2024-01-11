package nl.tudelft.sem.template.authentication.controller;

import nl.tudelft.sem.template.authentication.controllers.RoleChangeController;
import nl.tudelft.sem.template.authentication.domain.rolechange.RoleChange;
import nl.tudelft.sem.template.authentication.domain.rolechange.RoleChangeService;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.domain.user.Username;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

public class RoleChangeControllerTest {

    private transient RoleChangeService roleChangeService;
    private transient RoleChangeController roleChangeController;
    private transient List<RoleChange> roleChanges;
    private transient Username username;
    private transient String authority;

    @BeforeEach
    public void setUp() {
        roleChangeService = mock(RoleChangeService.class);
        roleChangeController = new RoleChangeController(roleChangeService);
        Authentication authenticationMock = mock(Authentication.class);
        when(authenticationMock.getName()).thenReturn("user");
        doReturn(List.of(Authority.REGULAR_USER)).when(authenticationMock).getAuthorities();
        SecurityContext securityContextMock = mock(SecurityContext.class);
        when(securityContextMock.getAuthentication()).thenReturn(authenticationMock);
        SecurityContextHolder.setContext(securityContextMock);

        roleChanges = List.of(new RoleChange(), new RoleChange());
        when(roleChangeService.getAll(Authority.REGULAR_USER.toString())).thenReturn(roleChanges);

        username = new Username("user");
        authority = Authority.REGULAR_USER.getAuthority();
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testGetAll() {
        assertThat(roleChangeController.getAllRequests()).isEqualTo(ResponseEntity.ok(roleChanges));
        verify(roleChangeService, times(1)).getAll(authority);
    }

    @Test
    public void testAddRequest() {
        RoleChange added = new RoleChange();
        assertThat(roleChangeController.addRoleChangeRequest(added)).isEqualTo(ResponseEntity.ok().build());
        verify(roleChangeService, times(1)).addRequest(username, added);
    }
}
