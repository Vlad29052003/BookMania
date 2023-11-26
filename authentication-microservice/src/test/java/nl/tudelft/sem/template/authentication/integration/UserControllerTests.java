package nl.tudelft.sem.template.authentication.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.transaction.Transactional;
import nl.tudelft.sem.template.authentication.authentication.JwtTokenGenerator;
import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.book.BookRepository;
import nl.tudelft.sem.template.authentication.domain.book.Genre;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.domain.user.HashedPassword;
import nl.tudelft.sem.template.authentication.domain.user.NetId;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import nl.tudelft.sem.template.authentication.integration.utils.JsonUtil;
import nl.tudelft.sem.template.authentication.models.UserModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class UserControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private transient JwtTokenGenerator jwtTokenGenerator;

    @Autowired
    private transient UserRepository userRepository;

    @Autowired
    private transient BookRepository bookRepository;

    @Test
    public void testGetUserByNetId() throws Exception {
        final NetId testUser = new NetId("SomeUser");
        final String email = "test@email.com";
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        final AppUser user = new AppUser(testUser, email, testHashedPassword);
        user.setAuthority(Authority.REGULAR_USER);
        Collection<SimpleGrantedAuthority> roles = new ArrayList<>();
        roles.add(new SimpleGrantedAuthority(Authority.REGULAR_USER.toString()));
        final String token = jwtTokenGenerator.generateToken(new User(testUser.toString(),
                                testHashedPassword.toString(), roles));
        userRepository.save(user);

        ResultActions resultActions = mockMvc.perform(
                get("/user")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        String response = resultActions.andReturn().getResponse().getContentAsString();
        UserModel userModel = new ObjectMapper().readValue(response, UserModel.class);

        assertThat(userModel.getNetId()).isEqualTo(testUser.toString());
        assertThat(userModel.getEmail()).isEqualTo(email);
        assertThat(userModel.getName()).isNull();
        assertThat(userModel.getBio()).isNull();
        assertThat(userModel.getLocation()).isNull();
        assertThat(userModel.getFavouriteGenres()).isEmpty();
        assertThat(userModel.getFavouriteBook()).isNull();
    }

    @Test
    public void testGetUserPictureByNetId() throws Exception {
        final NetId testUser = new NetId("SomeUser");
        final String email = "test@email.com";
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        final AppUser user = new AppUser(testUser, email, testHashedPassword);
        user.setAuthority(Authority.REGULAR_USER);
        final byte[] picture = new byte[]{13, 25, 12, 52, 43};
        user.setPicture(picture);
        Collection<SimpleGrantedAuthority> roles = new ArrayList<>();
        roles.add(new SimpleGrantedAuthority(Authority.REGULAR_USER.toString()));
        final String token = jwtTokenGenerator.generateToken(new User(testUser.toString(),
                testHashedPassword.toString(), roles));
        userRepository.save(user);

        ResultActions resultActions = mockMvc.perform(
                get("/user/picture")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        byte[] response = resultActions.andReturn().getResponse().getContentAsByteArray();

        assertThat(response).isEqualTo(picture);
    }

    @Test
    public void testUpdateName() throws Exception {
        final NetId testUser = new NetId("SomeUser");
        final String email = "test@email.com";
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        final AppUser user = new AppUser(testUser, email, testHashedPassword);
        final String newName = "Test Name";
        user.setAuthority(Authority.REGULAR_USER);
        Collection<SimpleGrantedAuthority> roles = new ArrayList<>();
        roles.add(new SimpleGrantedAuthority(Authority.REGULAR_USER.toString()));
        final String token = jwtTokenGenerator.generateToken(new User(testUser.toString(),
                                testHashedPassword.toString(), roles));
        userRepository.save(user);

        ResultActions resultActions = mockMvc.perform(
                patch("/user/name")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(Map.entry("name", newName)))
                .header("Authorization", "Bearer " + token));

        resultActions.andExpect(status().isOk());
        Optional<AppUser> userModel = userRepository.findByNetId(testUser);

        assertThat(userModel).isPresent();
        assertThat(userModel.get().getName()).isEqualTo(newName);
    }

    @Test
    public void testUpdateBio() throws Exception {
        final NetId testUser = new NetId("SomeUser");
        final String email = "test@email.com";
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        final AppUser user = new AppUser(testUser, email, testHashedPassword);
        final String newBio = "Short Bio.";
        user.setAuthority(Authority.REGULAR_USER);
        Collection<SimpleGrantedAuthority> roles = new ArrayList<>();
        roles.add(new SimpleGrantedAuthority(Authority.REGULAR_USER.toString()));
        final String token = jwtTokenGenerator.generateToken(new User(testUser.toString(),
                                testHashedPassword.toString(), roles));
        userRepository.save(user);

        ResultActions resultActions = mockMvc.perform(
                patch("/user/bio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.serialize(Map.entry("name", newBio)))
                        .header("Authorization", "Bearer " + token));

        resultActions.andExpect(status().isOk());
        Optional<AppUser> userModel = userRepository.findByNetId(testUser);

        assertThat(userModel).isPresent();
        assertThat(userModel.get().getBio()).isEqualTo(newBio);
    }

    @Test
    public void testUpdatePicture() throws Exception {
        final NetId testUser = new NetId("SomeUser");
        final String email = "test@email.com";
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        final AppUser user = new AppUser(testUser, email, testHashedPassword);
        final byte[] newPicture = new byte[]{13, 25, 12, 52, 43};
        user.setAuthority(Authority.REGULAR_USER);
        Collection<SimpleGrantedAuthority> roles = new ArrayList<>();
        roles.add(new SimpleGrantedAuthority(Authority.REGULAR_USER.toString()));
        final String token = jwtTokenGenerator.generateToken(new User(testUser.toString(),
                testHashedPassword.toString(), roles));
        userRepository.save(user);

        ResultActions resultActions = mockMvc.perform(
                patch("/user/picture")
                .contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .content(newPicture)
                .header("Authorization", "Bearer " + token));

        resultActions.andExpect(status().isOk());
        Optional<AppUser> userModel = userRepository.findByNetId(testUser);

        assertThat(userModel).isPresent();
        assertThat(userModel.get().getPicture()).isEqualTo(newPicture);
    }

    @Test
    public void testUpdateLocation() throws Exception {
        final NetId testUser = new NetId("SomeUser");
        final String email = "test@email.com";
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        final AppUser user = new AppUser(testUser, email, testHashedPassword);
        final String newLocation = "Delft";
        user.setAuthority(Authority.REGULAR_USER);
        Collection<SimpleGrantedAuthority> roles = new ArrayList<>();
        roles.add(new SimpleGrantedAuthority(Authority.REGULAR_USER.toString()));
        final String token = jwtTokenGenerator.generateToken(new User(testUser.toString(),
                                testHashedPassword.toString(), roles));
        userRepository.save(user);

        ResultActions resultActions = mockMvc.perform(
                patch("/user/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.serialize(Map.entry("name", newLocation)))
                        .header("Authorization", "Bearer " + token));

        resultActions.andExpect(status().isOk());
        Optional<AppUser> userModel = userRepository.findByNetId(testUser);

        assertThat(userModel).isPresent();
        assertThat(userModel.get().getLocation()).isEqualTo(newLocation);
    }

    @Test
    @Transactional
    public void testUpdateFavouriteGenres() throws Exception {
        final NetId testUser = new NetId("SomeUser");
        final String email = "test@email.com";
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        final AppUser user = new AppUser(testUser, email, testHashedPassword);
        final List<Genre> newFavouriteGenres = List.of(Genre.CRIME, Genre.SCIENCE, Genre.ROMANCE);
        user.setAuthority(Authority.REGULAR_USER);
        Collection<SimpleGrantedAuthority> roles = new ArrayList<>();
        roles.add(new SimpleGrantedAuthority(Authority.REGULAR_USER.toString()));
        final String token = jwtTokenGenerator.generateToken(new User(testUser.toString(),
                                testHashedPassword.toString(), roles));
        userRepository.save(user);

        ResultActions resultActions = mockMvc.perform(
                patch("/user/favouriteGenres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.serialize(Map.entry("name", newFavouriteGenres)))
                        .header("Authorization", "Bearer " + token));

        resultActions.andExpect(status().isOk());
        Optional<AppUser> userModel = userRepository.findByNetId(testUser);

        assertThat(userModel).isPresent();
        assertThat(userModel.get().getFavouriteGenres().toArray()).isEqualTo(newFavouriteGenres.toArray());
    }

    @Test
    @Transactional
    public void testUpdateFavouriteBook() throws Exception {
        final NetId testUser = new NetId("SomeUser");
        final String email = "test@email.com";
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        final AppUser user = new AppUser(testUser, email, testHashedPassword);
        final Book newFavouriteBook = new Book("Title",
                List.of("First Author", "Second Author"),
                List.of(Genre.SCIENCE, Genre.CRIME), "Short description.");
        user.setAuthority(Authority.REGULAR_USER);
        Collection<SimpleGrantedAuthority> roles = new ArrayList<>();
        roles.add(new SimpleGrantedAuthority(Authority.REGULAR_USER.toString()));
        final String token = jwtTokenGenerator.generateToken(new User(testUser.toString(),
                                testHashedPassword.toString(), roles));
        userRepository.save(user);

        ResultActions resultActions = mockMvc.perform(
                patch("/user/favouriteBook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.serialize(Map.entry("name", 1)))
                        .header("Authorization", "Bearer " + token));

        resultActions.andExpect(status().isNotFound());

        bookRepository.save(newFavouriteBook);
        resultActions = mockMvc.perform(
                patch("/user/favouriteBook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.serialize(Map.entry("name", 1)))
                        .header("Authorization", "Bearer " + token));

        Optional<AppUser> userModel = userRepository.findByNetId(testUser);

        assertThat(userModel).isPresent();
        assertThat(userModel.get().getFavouriteBook().getTitle()).isEqualTo(newFavouriteBook.getTitle());
        assertThat(userModel.get().getFavouriteBook().getAuthors()).isEqualTo(newFavouriteBook.getAuthors());
        assertThat(userModel.get().getFavouriteBook().getGenres()).isEqualTo(newFavouriteBook.getGenres());
        assertThat(userModel.get().getFavouriteBook().getDescription()).isEqualTo(newFavouriteBook.getDescription());
    }
}
