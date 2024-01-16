package nl.tudelft.sem.template.authentication.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import nl.tudelft.sem.template.authentication.application.book.BookEventsListener;
import nl.tudelft.sem.template.authentication.application.user.UserEventsListener;
import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.book.BookRepository;
import nl.tudelft.sem.template.authentication.domain.book.Genre;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import nl.tudelft.sem.template.authentication.domain.user.Username;
import nl.tudelft.sem.template.authentication.integration.utils.JsonUtil;
import nl.tudelft.sem.template.authentication.models.AuthenticationRequestModel;
import nl.tudelft.sem.template.authentication.models.AuthenticationResponseModel;
import nl.tudelft.sem.template.authentication.models.CreateBookRequestModel;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles({"test"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class BookIntegrationTests {
    @Autowired
    private transient MockMvc mockMvc;
    @Autowired
    private transient UserRepository userRepository;
    @Autowired
    private transient BookRepository bookRepository;
    private transient String tokenAdmin;
    private transient String tokenAuthor;
    private transient String tokenUser;
    private transient Book book1;
    private transient Book book2;
    private transient CreateBookRequestModel book1Request;
    private transient CreateBookRequestModel book3Request;
    private static final String bookshelfPath = "/bookshelf_service/catalog";

    private static final String reviewPath = "/b/book";

    private static final String BOOKSHELF_PATH = "/bookshelf_service/user";
    private static WireMockServer mockServer;
    private UUID adminId;

    /**
     * Initialises mock servers for book listeners.
     */
    @BeforeAll
    public static void init() {
        mockServer = new WireMockServer(
                new WireMockConfiguration().port(8080)
        );
        mockServer.start();

        configureFor("localhost", 8080);
        stubFor(WireMock.any(urlPathMatching(".*"))
                .willReturn(aResponse()
                        .withStatus(200)));

        // Since wiremock is configured on 8080, we assume everything is on the same port.
        BookEventsListener.REVIEW_URI = "http://localhost:8080/b/book";
    }

    /**
     * Sets up the testing environment.
     *
     * @throws Exception if something fails.
     */
    @BeforeEach
    public void setUp() throws Exception {
        Username adminUsername = new Username("admin");
        String adminEmail = "admin@email.com";
        String password = "Pass@123";
        RegistrationRequestModel adminRegistrationRequest = new RegistrationRequestModel();
        adminRegistrationRequest.setUsername(adminUsername.toString());
        adminRegistrationRequest.setEmail(adminEmail);
        adminRegistrationRequest.setPassword(password);
        AuthenticationRequestModel adminAuthenticationRequest = new AuthenticationRequestModel();
        adminAuthenticationRequest.setUsername(adminUsername.toString());
        adminAuthenticationRequest.setPassword(password);

        Username authorUsername = new Username("author");
        String authorEmail = "author@email.com";
        RegistrationRequestModel authorRegistrationRequest = new RegistrationRequestModel();
        authorRegistrationRequest.setUsername(authorUsername.toString());
        authorRegistrationRequest.setEmail(authorEmail);
        authorRegistrationRequest.setPassword(password);
        AuthenticationRequestModel authorAuthenticationRequest = new AuthenticationRequestModel();
        authorAuthenticationRequest.setUsername(authorUsername.toString());
        authorAuthenticationRequest.setPassword(password);

        Username userUsername = new Username("user");
        String userEmail = "user@email.com";
        RegistrationRequestModel userRegistrationRequest = new RegistrationRequestModel();
        userRegistrationRequest.setUsername(userUsername.toString());
        userRegistrationRequest.setEmail(userEmail);
        userRegistrationRequest.setPassword(password);
        AuthenticationRequestModel userAuthenticationRequest = new AuthenticationRequestModel();
        userAuthenticationRequest.setUsername(userUsername.toString());
        userAuthenticationRequest.setPassword(password);

        mockMvc.perform(post("/c/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(adminRegistrationRequest)));
        mockMvc.perform(post("/c/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(authorRegistrationRequest)));
        mockMvc.perform(post("/c/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(userRegistrationRequest)));

        AppUser adminUser = userRepository.findByUsername(adminUsername).get();
        adminUser.setAuthority(Authority.ADMIN);
        userRepository.saveAndFlush(adminUser);
        adminId = adminUser.getId();
        AppUser authorUser = userRepository.findByUsername(authorUsername).get();
        authorUser.setAuthority(Authority.AUTHOR);
        authorUser.setName("author");
        userRepository.saveAndFlush(authorUser);

        ResultActions resultActionsAdmin = mockMvc.perform(post("/c/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(adminAuthenticationRequest)));
        MvcResult resultAdmin = resultActionsAdmin
                .andExpect(status().isOk())
                .andReturn();
        AuthenticationResponseModel adminAuthenticationResponse =
                JsonUtil.deserialize(resultAdmin.getResponse().getContentAsString(),
                        AuthenticationResponseModel.class);
        this.tokenAdmin = "Bearer " + adminAuthenticationResponse.getToken();

        ResultActions resultActionsAuthor = mockMvc.perform(post("/c/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(authorAuthenticationRequest)));
        MvcResult resultAuthor = resultActionsAuthor
                .andExpect(status().isOk())
                .andReturn();
        AuthenticationResponseModel authorAuthenticationResponse =
                JsonUtil.deserialize(resultAuthor.getResponse().getContentAsString(),
                        AuthenticationResponseModel.class);
        this.tokenAuthor = "Bearer " + authorAuthenticationResponse.getToken();

        ResultActions resultActionsUser = mockMvc.perform(post("/c/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(userAuthenticationRequest)));
        MvcResult resultUser = resultActionsUser
                .andExpect(status().isOk())
                .andReturn();
        AuthenticationResponseModel userAuthenticationResponse =
                JsonUtil.deserialize(resultUser.getResponse().getContentAsString(),
                        AuthenticationResponseModel.class);
        this.tokenUser = "Bearer " + userAuthenticationResponse.getToken();

        this.book1 = new Book("book1", List.of("Author1", "Author2"),
                List.of(Genre.CRIME, Genre.DRAMA), "description1", 246);
        this.book2 = new Book("book2", List.of("Author6"),
                List.of(Genre.SCIENCE), "description2", 87);
        Book book3 = new Book("book2", List.of("author"),
                List.of(Genre.POETRY, Genre.ROMANCE, Genre.BIOGRAPHY), "description3", 784);

        this.book1Request = new CreateBookRequestModel(book1);
        this.book3Request = new CreateBookRequestModel(book3);
    }

    @Test
    public void testGetNoBooks() throws Exception {
        UUID random = UUID.randomUUID();
        ResultActions resultActions = mockMvc.perform(get("/c/books/" + random)
                .header("Authorization", tokenUser));
        resultActions.andExpect(status().isNotFound())
                .andExpect(content().string("404 NOT_FOUND \"The book does not exist!\""));
    }

    @Test
    public void testCreateBookUserNonExistent() throws Exception {
        userRepository.deleteAll();

        ResultActions resultActions = mockMvc.perform(post("/c/books/")
                .header("Authorization", tokenUser)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(book1Request)));

        //only admins or authors of the book may add them
        resultActions.andExpect(status().isUnauthorized())
                .andExpect(content().string("User does not exist!"));
    }

    @Test
    public void testCreate() throws Exception {
        ResultActions regularUserAddsBook = mockMvc.perform(post("/c/books/")
                .header("Authorization", tokenUser)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(book1Request)));

        //only admins or authors of the book may add them
        regularUserAddsBook.andExpect(status().isUnauthorized())
                .andExpect(content().string("401 UNAUTHORIZED \"Only admins or authors may add books to the system!\""));

        ResultActions notAuthorOfBookAddsBook = mockMvc.perform(post("/c/books/")
                .header("Authorization", tokenAuthor)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(book1Request)));

        //only admins or authors of the book may add them
        notAuthorOfBookAddsBook.andExpect(status().isUnauthorized())
                .andExpect(content().string("401 UNAUTHORIZED \"Only the authors of the book may add it to the system!\""));

        ResultActions authorOfBookAddsBook = mockMvc.perform(post("/c/books/")
                .header("Authorization", tokenAuthor)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(book3Request)));

        //authors of the book may add them
        authorOfBookAddsBook.andExpect(status().isOk());

        ResultActions bookAlreadyInSystem = mockMvc.perform(post("/c/books/")
                .header("Authorization", tokenAuthor)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(book3Request)));

        //authors of the book may add them
        bookAlreadyInSystem.andExpect(status().isConflict())
                .andExpect(content().string("409 CONFLICT \"The book is already in the system!\""));

        ResultActions adminAddsBook = mockMvc.perform(post("/c/books/")
                .header("Authorization", tokenAdmin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(book1Request)));

        //admins may add books
        adminAddsBook.andExpect(status().isOk());

        List<Book> books = bookRepository.findAll();
        assertThat(books.size()).isEqualTo(2);
        UUID id1 = books.get(1).getId();

        book1Request.setAuthors(List.of("newAuthor1", "newAuthor2"));
        ResultActions adminAddsBook2 = mockMvc.perform(post("/c/books/")
                .header("Authorization", tokenAdmin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(book1Request)));

        //admins may add books
        adminAddsBook2.andExpect(status().isOk());

        ResultActions getBook = mockMvc.perform(get("/c/books/" + id1)
                .header("Authorization", tokenUser));


        MvcResult getResult = getBook.andExpect(status().isOk()).andReturn();
        Book bookResponse = JsonUtil.deserialize(getResult.getResponse().getContentAsString(),
                Book.class);

        assertThat(bookResponse.getId()).isEqualTo(id1);
        assertThat(bookResponse.getTitle()).isEqualTo(book1.getTitle());
        assertThat(bookResponse.getAuthors()).isEqualTo(book1.getAuthors());
        assertThat(bookResponse.getGenres()).isEqualTo(book1.getGenres());
        assertThat(bookResponse.getDescription()).isEqualTo(book1.getDescription());
        assertThat(bookResponse.getNumPages()).isEqualTo(book1.getNumPages());
    }

    @Test
    public void testAddErrors() {

    }

    @Test
    @Transactional
    public void testUpdateAndDelete() throws Exception {
        ResultActions addBook1 = mockMvc.perform(post("/c/books/")
                .header("Authorization", tokenAuthor)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(book3Request)));
        addBook1.andExpect(status().isOk());

        ResultActions addBook2 = mockMvc.perform(post("/c/books/")
                .header("Authorization", tokenAdmin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(book1Request)));
        addBook2.andExpect(status().isOk());

        UUID id1 = bookRepository.findByTitle("book1").get(0).getId();
        book2.setId(id1);
        ResultActions updateBook1 = mockMvc.perform(put("/c/books/")
                .header("Authorization", tokenAdmin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(book2)));
        updateBook1.andExpect(status().isOk());

        Book updated = bookRepository.findById(id1).get();
        assertThat(updated.getId()).isEqualTo(book2.getId());
        assertThat(updated.getTitle()).isEqualTo(book2.getTitle());
        assertThat(new ArrayList<>(updated.getAuthors())).isEqualTo(book2.getAuthors());
        assertThat(new ArrayList<>(updated.getGenres())).isEqualTo(book2.getGenres());
        assertThat(updated.getDescription()).isEqualTo(book2.getDescription());
        assertThat(updated.getNumPages()).isEqualTo(book2.getNumPages());

        UUID id3 = bookRepository.findByTitle("book2").get(0).getId();
        book1.setId(id3);
        book1.setAuthors(List.of("author", "author f", "author y"));
        ResultActions updateBook2 = mockMvc.perform(put("/c/books/")
                .header("Authorization", tokenAuthor)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(book1)));
        updateBook2.andExpect(status().isOk());

        updated = bookRepository.findById(id3).get();
        assertThat(updated.getId()).isEqualTo(book1.getId());
        assertThat(updated.getTitle()).isEqualTo(book1.getTitle());
        assertThat(new ArrayList<>(updated.getAuthors())).isEqualTo(book1.getAuthors());
        assertThat(new ArrayList<>(updated.getGenres())).isEqualTo(book1.getGenres());
        assertThat(updated.getDescription()).isEqualTo(book1.getDescription());
        assertThat(updated.getNumPages()).isEqualTo(book1.getNumPages());

        stubFor(WireMock.delete(urlEqualTo(bookshelfPath + "?bookId=" + id1))
                .willReturn(aResponse().withStatus(200)));
        stubFor(WireMock.delete(urlEqualTo(reviewPath + "/" + id1 + "/" + adminId))
                .willReturn(aResponse().withStatus(200)));

        stubFor(WireMock.delete(urlEqualTo(bookshelfPath + "?bookId=" + id3))
                .willReturn(aResponse().withStatus(200)));
        stubFor(WireMock.delete(urlEqualTo(reviewPath + "/" + id3 + "/" + adminId))
                .willReturn(aResponse().withStatus(200)));

        ResultActions deleteBook1 = mockMvc.perform(delete("/c/books/" + id3)
                .header("Authorization", tokenAuthor));
        deleteBook1.andExpect(status().isUnauthorized())
                .andExpect(content().string("401 UNAUTHORIZED \"Only admins may delete books from the system!\""));

        ResultActions deleteBook2 = mockMvc.perform(delete("/c/books/" + id3)
                .header("Authorization", tokenAdmin));
        deleteBook2.andExpect(status().isOk());
        assertThat(bookRepository.findAll().size()).isEqualTo(1);
        assertThat(bookRepository.findAll().get(0).getId()).isEqualTo(id1);

        ResultActions deleteBook3 = mockMvc.perform(delete("/c/books/" + id1)
                .header("Authorization", tokenAdmin));
        deleteBook3.andExpect(status().isOk());
        assertThat(bookRepository.findAll().isEmpty()).isTrue();
    }

    @Test
    public void testDeleteFavouriteBook() throws Exception {
        ResultActions addBook2 = mockMvc.perform(post("/c/books/")
                .header("Authorization", tokenAdmin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(book1Request)));
        addBook2.andExpect(status().isOk());

        UUID id1 = bookRepository.findByTitle("book1").get(0).getId();

        ResultActions markBookAsFavorite = mockMvc.perform(patch("/c/users/favouriteBook")
                .header("Authorization", tokenUser)
                .contentType(MediaType.TEXT_PLAIN)
                .content(id1.toString()));
        markBookAsFavorite.andExpect(status().isOk());

        AppUser appUser = userRepository.findByUsername(new Username("user")).get();
        assertThat(appUser.getFavouriteBook().getId()).isEqualTo(id1);

        stubFor(WireMock.delete(urlEqualTo(bookshelfPath + "?bookId=" + id1))
                .willReturn(aResponse().withStatus(200)));
        stubFor(WireMock.delete(urlEqualTo(reviewPath + "/" + id1 + "/" + adminId))
                .willReturn(aResponse().withStatus(200)));

        ResultActions deleteBook3 = mockMvc.perform(delete("/c/books/" + id1)
                .header("Authorization", tokenAdmin));
        deleteBook3.andExpect(status().isOk());
        assertThat(bookRepository.findAll().isEmpty()).isTrue();
        AppUser updatedUser = userRepository.findByUsername(new Username("user")).get();
        assertThat(updatedUser.getFavouriteBook()).isNull();
    }

    @Test
    @Transactional
    public void testUpdateErrors() throws Exception {
        ResultActions addBook1 = mockMvc.perform(put("/c/books/")
                .header("Authorization", tokenAuthor)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(book3Request)));

        ResultActions updateBook1 = mockMvc.perform(put("/c/books/")
                .header("Authorization", tokenUser)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(book2)));
        updateBook1.andExpect(status().isUnauthorized())
                .andExpect(content().string("401 UNAUTHORIZED \"Only admins or authors may update books in the system!\""));

        ResultActions updateBook2 = mockMvc.perform(put("/c/books/")
                .header("Authorization", tokenAuthor)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(book2)));
        updateBook2.andExpect(status().isUnauthorized())
                .andExpect(content().string("401 UNAUTHORIZED \"Only the authors of the book may edit it!\""));
    }

    /**
     * Set up for the testing environment after all tests.
     */
    @AfterAll
    public static void stop() {
        mockServer.stop();
    }
}
