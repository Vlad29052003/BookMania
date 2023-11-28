package nl.tudelft.sem.template.authentication.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;



@ExtendWith(SpringExtension.class)
@SpringBootTest
// activate profiles to have spring use mocks during auto-injection of certain beans.
@ActiveProfiles({"test", "mockPasswordEncoder"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UserLookupServiceTests {

    @Autowired
    private transient UserLookupService userLookupService;

    @Autowired
    private transient RegistrationService registrationService;

    @Autowired
    private transient PasswordHashingService mockPasswordEncoder;

    @Autowired
    private transient UserRepository userRepository;

    @Test
    public void userSearchByName_worksCorrectly() throws Exception {
        // Arrange
        final NetId testUser = new NetId("SomeUser");
        final String email = "testEmail";
        final Password testPassword = new Password("password123");
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        when(mockPasswordEncoder.hash(testPassword)).thenReturn(testHashedPassword);


        final NetId testUser2 = new NetId("OtherUser");
        final String email2 = "otherEmail";
        final Password testPassword2 = new Password("password123");
        final HashedPassword testHashedPassword2 = new HashedPassword("hashedTestPassword");
        when(mockPasswordEncoder.hash(testPassword2)).thenReturn(testHashedPassword2);

        // Act
        registrationService.registerUser(testUser, email, testPassword);
        registrationService.registerUser(testUser2, email2, testPassword2);

        // Assert
        List<String> foundUsers = userLookupService.getUsersByName("other")
                .stream().map(AppUser::getEmail).collect(Collectors.toList());
        List<String> expected = List.of(email2);


        assertThat(foundUsers).containsAll(expected);
    }
}
