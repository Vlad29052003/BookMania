package nl.tudelft.sem.template.authentication.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nl.tudelft.sem.template.authentication.application.user.UserEventsListener;
import nl.tudelft.sem.template.authentication.authentication.JwtTokenGenerator;
import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.book.BookRepository;
import nl.tudelft.sem.template.authentication.domain.book.Genre;
import nl.tudelft.sem.template.authentication.domain.stats.StatsRepository;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.domain.user.HashedPassword;
import nl.tudelft.sem.template.authentication.domain.user.PasswordHashingService;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import nl.tudelft.sem.template.authentication.domain.user.Username;
import nl.tudelft.sem.template.authentication.models.RegistrationRequestModel;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles({"test", "mockPasswordEncoder", "mockAuthenticationManager"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class StatsIntegrationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private transient JwtTokenGenerator jwtTokenGenerator;

    @Autowired
    private transient StatsRepository statsRepository;

    @Autowired
    private transient UserRepository userRepository;

    @Autowired
    private transient BookRepository bookRepository;

    @Autowired
    private transient PasswordHashingService mockPasswordEncoder;

    @Autowired
    private transient JwtTokenGenerator mockJwtTokenGenerator;

    @Autowired
    private transient AuthenticationManager mockAuthenticationManager;

    private RegistrationRequestModel normalUserRequest;
    private AppUser normalUser;
    private AppUser admin;
    private String userToken;
    private String adminToken;
    private Book book1;
    private static WireMockServer wireMockServer;

    private static final String BOOKSHELF_PATH = "/a/user";

    private static final String REVIEW_PATH = "/b/user";

    /**
     * Parses a json book list into a list of strings. Testing purposes.
     *
     * @param json the json list
     * @return the list of strings
     */
    private List<String> parseJsonBooks(String json) {

        return Stream.of(json.substring(1, json.length() - 1).split("},"))
                .map(s -> s + "}")
                .collect(Collectors.toList());
    }


    /**
     * Parses a json genre list into a list of strings. Testing purposes.
     *
     * @param json the json list
     * @return the list of strings
     */
    private List<String> parseJsonGenres(String json) {
        return List.of(json.substring(1, json.length() - 1).split(","));
    }

    /**
     * Sets up for testing.
     */
    @BeforeAll
    public static void init() {
        wireMockServer = new WireMockServer(new WireMockConfiguration().port(8080));
        wireMockServer.start();

        stubFor(WireMock.post(urlEqualTo(BOOKSHELF_PATH))
                .willReturn(aResponse().withStatus(200)));

        WireMock.configureFor("localhost", 8080);

        UserEventsListener.BOOKSHELF_URL = "http://localhost:8080/a/user";
        UserEventsListener.REVIEW_URL = "http://localhost:8080/b/user";
    }


    /**
     * Set up the testing environment.
     */
    @BeforeEach
    public void setUp() {
        normalUser = new AppUser(new Username("user1234a"), "userabc@regular.com", new HashedPassword("Pass1234!"));
        normalUserRequest = new RegistrationRequestModel(normalUser.getUsername().toString(),
                normalUser.getEmail(), normalUser.getPassword().toString());
        normalUser.setAuthority(Authority.REGULAR_USER);
        Collection<SimpleGrantedAuthority> rolesUser = new ArrayList<>();
        rolesUser.add(new SimpleGrantedAuthority(Authority.REGULAR_USER.toString()));
        userToken = jwtTokenGenerator.generateToken(new User(normalUser.getUsername().toString(),
                normalUser.getPassword().toString(), rolesUser));


        admin = new AppUser(new Username("admin"), "admin@admin.com", new HashedPassword("pwd"));
        admin.setAuthority(Authority.ADMIN);
        Collection<SimpleGrantedAuthority> rolesAdmin = new ArrayList<>();
        rolesAdmin.add(new SimpleGrantedAuthority(Authority.ADMIN.toString()));
        adminToken = jwtTokenGenerator.generateToken(new User(admin.getUsername().toString(),
                admin.getPassword().toString(), rolesAdmin));

        book1 = new Book("book1", List.of("Author1", "Author2"),
                List.of(Genre.CRIME, Genre.DRAMA), "description1", 246);
    }

    @Test
    public void testGetPopularBooks() throws Exception {

        bookRepository.save(book1);
        UUID bookId = bookRepository.findAll().get(0).getId();
        book1.setId(bookId);
        userRepository.save(admin);
        userRepository.save(normalUser);

        mockMvc.perform(patch("/c/users/favouriteBook")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(bookId.toString())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());


        ResultActions resultActions = mockMvc.perform(get("/c/stats/popularBooks")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
        List<String> books = parseJsonBooks(resultActions.andReturn().getResponse().getContentAsString());

        assertThat(books.size()).isEqualTo(1);
        assertThat(books).anySatisfy(s -> assertThat(s.contains(book1.getTitle())).isTrue());
    }

    @Test
    public void testGetPopularGenres() throws Exception {
        userRepository.save(admin);
        userRepository.save(normalUser);

        mockMvc.perform(patch("/c/users/favouriteGenres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[\"CRIME\", \"DRAMA\"]")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());

        ResultActions resultActions = mockMvc.perform(get("/c/stats/popularGenres")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
        List<String> genres = parseJsonGenres(resultActions.andReturn().getResponse().getContentAsString());

        assertThat(genres.size()).isEqualTo(2);
        assertThat(genres).anySatisfy(s -> assertThat(s.contains(Genre.CRIME.toString())).isTrue());
        assertThat(genres).anySatisfy(s -> assertThat(s.contains(Genre.DRAMA.toString())).isTrue());
    }


    @Test
    public void testGetPopularGenresMoreUsers() throws Exception {
        userRepository.save(admin);
        userRepository.save(normalUser);

        mockMvc.perform(patch("/c/users/favouriteGenres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[\"CRIME\", \"DRAMA\", \"HISTORY\", \"BIOGRAPHY\","
                                + " \"SCIENCE\", \"POETRY\", \"MYSTERY\", \"SCIENCE_FICTION\","
                                + " \"FANTASY\", \"ROMANCE\", \"HORROR\"]")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/c/users/favouriteGenres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[\"CRIME\", \"DRAMA\", \"HISTORY\"]")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
        ResultActions resultActions = mockMvc.perform(get("/c/stats/popularGenres")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
        List<String> genres = parseJsonGenres(resultActions.andReturn().getResponse().getContentAsString());

        assertThat(genres.size()).isEqualTo(3);
        assertThat(genres).anySatisfy(s -> assertThat(s.contains(Genre.CRIME.toString())).isTrue());
        assertThat(genres).anySatisfy(s -> assertThat(s.contains(Genre.DRAMA.toString())).isTrue());
        assertThat(genres).anySatisfy(s -> assertThat(s.contains(Genre.HISTORY.toString())).isTrue());
    }

    @Test
    public void testGetPopularGenresNoUsers() throws Exception {
        userRepository.save(normalUser);
        userRepository.save(admin);

        ResultActions resultActions = mockMvc.perform(get("/c/stats/popularGenres")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
        String genres = resultActions.andReturn().getResponse().getContentAsString();
        assertThat(genres).isEqualTo("[]");
    }

    @AfterAll
    public static void stop() {
        wireMockServer.stop();
    }


}

