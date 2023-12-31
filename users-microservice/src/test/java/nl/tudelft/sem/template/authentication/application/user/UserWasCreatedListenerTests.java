package nl.tudelft.sem.template.authentication.application.user;

import nl.tudelft.sem.template.authentication.domain.HasEvents;
import nl.tudelft.sem.template.authentication.domain.user.*;
import nl.tudelft.sem.template.authentication.models.RegistrationRequestModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class UserWasCreatedListenerTests {

    private transient UserWasCreatedListener userWasCreatedListener;

    private transient AuthenticationService authenticationService;

    @BeforeEach
    public void setUp() {
        userWasCreatedListener = mock(UserWasCreatedListener.class);
        authenticationService = mock(AuthenticationService.class);
    }

    @Test
    public void testOnUserWasCreated() {
//        AppUser user = new AppUser();
//        try {
//            authenticationService.registrationHelper(new Username("Username"), "email", new Password("Password"));
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//        verify(userWasCreatedListener, Mockito.times(1)).onAccountWasCreated(any());
    }
}
