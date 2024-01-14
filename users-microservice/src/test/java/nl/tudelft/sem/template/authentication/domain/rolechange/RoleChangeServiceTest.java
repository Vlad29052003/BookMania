package nl.tudelft.sem.template.authentication.domain.rolechange;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.UUID;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.domain.user.HashedPassword;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import nl.tudelft.sem.template.authentication.domain.user.Username;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class RoleChangeServiceTest {

    @Autowired
    private transient RoleChangeService roleChangeService;
    @Autowired
    private transient RoleChangeRepository roleChangeRepository;
    @Autowired
    private transient UserRepository userRepository;
    private AppUser user;
    private RoleChange roleChange;

    /**
     * Set up the testing environment.
     */
    @BeforeEach
    public void setUp() {
        user = new AppUser(new Username("simona"), "simona@sem.nl", new HashedPassword("iwanttobeanauthor"));
        user.setId(UUID.randomUUID());
        roleChange = new RoleChange("simona", Authority.AUTHOR, "123-456");
        roleChange.setId(UUID.randomUUID());
    }

    @Test
    public void getAllNotAdminTest() {
        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> roleChangeService.getAll("REGULAR_USER"));
        assertEquals(e.getStatus(), HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void getAllGoodTest() {
        assertDoesNotThrow(() -> roleChangeService.getAll("ADMIN"));
        assertEquals(roleChangeService.getAll("ADMIN"), new ArrayList<>());
        roleChangeRepository.save(roleChange);
        assertEquals(roleChangeService.getAll("ADMIN").size(), 1);
    }

    @Test
    public void addReportNonExistentUserTest() {
        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> roleChangeService.addRequest(new Username("username"), roleChange));
        assertEquals(e.getStatus(), HttpStatus.NOT_FOUND);
    }

    @Test
    public void addReportBadRequestTest() {
        userRepository.save(user);
        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> roleChangeService.addRequest(user.getUsername(), null));
        assertEquals(e.getStatus(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void addReportGoodTest() {
        userRepository.save(user);
        assertDoesNotThrow(() -> roleChangeService.addRequest(user.getUsername(), roleChange));
        assertEquals(roleChangeRepository.findAll().size(), 1);
    }
}
