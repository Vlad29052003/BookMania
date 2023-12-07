package nl.tudelft.sem.template.authentication.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import nl.tudelft.sem.template.authentication.controllers.UserSearchController;
import nl.tudelft.sem.template.authentication.domain.user.UserLookupService;
import nl.tudelft.sem.template.authentication.models.UserModel;
import org.h2.engine.User;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;



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

        assertThrows(ResponseStatusException.class, () -> userSearchController.getUser("name"));
    }
}
