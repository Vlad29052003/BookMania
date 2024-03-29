package nl.tudelft.sem.template.authentication.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.HashedPassword;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import nl.tudelft.sem.template.authentication.domain.user.Username;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
// activate profiles to have spring use mocks during auto-injection of certain beans.
@ActiveProfiles({"test", "mockPasswordEncoder"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class JwtUserDetailsServiceTests {

    @Autowired
    private transient JwtUserDetailsService jwtUserDetailsService;

    @Autowired
    private transient UserRepository userRepository;

    @Test
    public void loadUserByUsername_withValidUser_returnsCorrectUser() {
        // Arrange
        final Username testUser = new Username("SomeUser");
        final String email = "testEmail@gmail.com";
        final HashedPassword testHashedPassword = new HashedPassword("password123Hash!");

        AppUser appUser = new AppUser(testUser, email, testHashedPassword);
        userRepository.save(appUser);

        // Act
        UserDetails actual = jwtUserDetailsService.loadUserByUsername(testUser.toString());

        // Assert
        assertThat(actual.getUsername()).isEqualTo(testUser.toString());
        assertThat(actual.getPassword()).isEqualTo(testHashedPassword.toString());
    }

    @Test
    public void loadUserByUsername_withNonexistentUser_throwsException() {
        // Arrange
        final Username testUser = new Username("AnotherUser");
        final String email = "testEmail@gmail.com";
        final String testPasswordHash = "password123Hash!";

        AppUser appUser = new AppUser(testUser, email, new HashedPassword(testPasswordHash));
        userRepository.save(appUser);

        // Act
        ThrowableAssert.ThrowingCallable action = () -> jwtUserDetailsService.loadUserByUsername("SomeUser");

        // Assert
        assertThatExceptionOfType(UsernameNotFoundException.class)
                .isThrownBy(action);
    }

    @Test
    public void loadUserByUsername_withDisabledUser_throwsException() {
        final Username testUser = new Username("SomeUser");
        final String email = "email@mail.com";
        final String testPasswordHash = "password123Hash";

        AppUser appUser = new AppUser(testUser, email, new HashedPassword(testPasswordHash));
        appUser.setDeactivated(true);
        userRepository.save(appUser);

        // Act
        ThrowableAssert.ThrowingCallable action = () -> jwtUserDetailsService.loadUserByUsername("SomeUser");

        // Assert
        assertThatExceptionOfType(DisabledException.class)
                .isThrownBy(action);
    }
}
