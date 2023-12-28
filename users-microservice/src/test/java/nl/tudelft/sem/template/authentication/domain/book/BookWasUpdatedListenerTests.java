package nl.tudelft.sem.template.authentication.domain.book;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import nl.tudelft.sem.template.authentication.application.book.BookWasUpdatedListener;
import nl.tudelft.sem.template.authentication.domain.user.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class BookWasUpdatedListenerTests {

    private static final String bookshelfPath = "/a/catalog";
    private static final String reviewPath = "/b/book";
    private final transient BookWasUpdatedListener bookWasUpdatedListener = new BookWasUpdatedListener();

    @Autowired
    private transient BookRepository bookRepository;

    @Autowired
    private transient UserRepository userRepository;

    private static WireMockServer bookshelfServer;

    private static ByteArrayOutputStream outputStreamCaptor;

    /**
     * Set up before all tests.
     */
    @BeforeAll
    static void init() {

        bookshelfServer = new WireMockServer(
                new WireMockConfiguration().port(8081)
        );
        bookshelfServer.start();
        WireMock.configureFor("localhost", 8081);

        stubFor(post(urlEqualTo(bookshelfPath)).willReturn(aResponse().withStatus(200)));


        outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @Test
    public void testPut() {
        Book book = new Book("title", null, null, "", 1);
        bookRepository.saveAndFlush(book);

        stubFor(put(urlEqualTo(bookshelfPath))
                .willReturn(aResponse().withStatus(200)));

        bookWasUpdatedListener.onBookWasCreated(new BookWasCreatedEvent(book));

        assertThat(outputStreamCaptor.toString().trim())
                .contains("Book (" + book.getTitle() + ") was created.");

        verify(putRequestedFor(urlPathEqualTo(bookshelfPath))
                .withRequestBody(containing("\"id\":\"" + book.getId().toString() + "\""))
                .withRequestBody(containing("\"title\":\"" + book.getTitle() + "\""))
                .withRequestBody(containing("\"authors\":" + book.getAuthors().toString()))
                .withRequestBody(containing("\"genres\":" + book.getGenres().toString()))
                .withRequestBody(containing("\"description\":\"" + book.getDescription() + "\""))
                .withRequestBody(containing("\"numPages\":" + book.getNumPages())));
    }

    @Test
    public void testPost() {
        Book book = new Book("title", null, null, "", 1);
        bookRepository.saveAndFlush(book);

        bookWasUpdatedListener.onBookWasEdited(new BookWasEditedEvent(book));

        assertThat(outputStreamCaptor.toString().trim())
                .contains("Book (" + book.getTitle() + ") was edited.");

        verify(postRequestedFor(urlPathEqualTo(bookshelfPath))
                .withRequestBody(containing("\"id\":\"" + book.getId().toString() + "\""))
                .withRequestBody(containing("\"title\":\"" + book.getTitle() + "\""))
                .withRequestBody(containing("\"authors\":" + book.getAuthors().toString()))
                .withRequestBody(containing("\"genres\":" + book.getGenres().toString()))
                .withRequestBody(containing("\"description\":\"" + book.getDescription() + "\""))
                .withRequestBody(containing("\"numPages\":" + book.getNumPages())));
    }

    @Test
    public void testDelete() {
        Book book = new Book("title", null, null, "", 1);
        bookRepository.saveAndFlush(book);

        AppUser user = new AppUser(new Username("user"), "test@email.com", new HashedPassword("pass"));
        user.setAuthority(Authority.ADMIN);
        userRepository.save(user);

        stubFor(delete(urlEqualTo(bookshelfPath))
                .willReturn(aResponse().withStatus(200)));
        stubFor(delete(urlEqualTo(reviewPath + "/"))
                .willReturn(aResponse().withStatus(200)));

        bookWasUpdatedListener.onBookWasDeleted(new BookWasDeletedEvent(book, user.getId()));

        assertThat(outputStreamCaptor.toString().trim())
                .contains("Book (" + book.getTitle() + ") was deleted.");

        verify(deleteRequestedFor(urlPathEqualTo(bookshelfPath))
                .withQueryParam("bookId", equalTo(book.getId().toString())));
        verify(deleteRequestedFor(urlPathTemplate(reviewPath + "/{bookId}/{userId}"))
                .withPathParam("bookId", equalTo(book.getId().toString()))
                .withPathParam("userId", equalTo(user.getId().toString())));
    }

    @Test
    public void testDeleteValidAuthor() {
        List<String> authors = new ArrayList<>();
        authors.add("author");
        Book book = new Book("title", authors, null, "", 1);
        bookRepository.saveAndFlush(book);

        AppUser user = new AppUser(new Username("author"), "test@email.com", new HashedPassword("pass"));
        user.setName("author");
        user.setAuthority(Authority.AUTHOR);
        userRepository.save(user);

        stubFor(delete(urlEqualTo(bookshelfPath))
                .willReturn(aResponse().withStatus(200)));
        stubFor(delete(urlEqualTo(reviewPath + "/"))
                .willReturn(aResponse().withStatus(200)));

        bookWasUpdatedListener.onBookWasDeleted(new BookWasDeletedEvent(book, user.getId()));

        assertThat(outputStreamCaptor.toString().trim())
                .contains("Book (" + book.getTitle() + ") was deleted.");

        verify(deleteRequestedFor(urlPathEqualTo(bookshelfPath))
                .withQueryParam("bookId", equalTo(book.getId().toString())));
        verify(deleteRequestedFor(urlPathTemplate(reviewPath + "/{bookId}/{userId}"))
                .withPathParam("bookId", equalTo(book.getId().toString()))
                .withPathParam("userId", equalTo(user.getId().toString())));
    }

    @Test
    public void testDeleteInvalidAuthor() {
        List<String> authors = new ArrayList<>();
        authors.add("author");
        Book book = new Book("title", authors, null, "", 1);
        bookRepository.saveAndFlush(book);

        AppUser user = new AppUser(new Username("user"), "test@email.com", new HashedPassword("pass"));
        user.setName("different_author");
        user.setAuthority(Authority.AUTHOR);
        userRepository.save(user);

        stubFor(delete(urlEqualTo(bookshelfPath))
                .willReturn(aResponse().withStatus(200)));
        stubFor(delete(urlEqualTo(reviewPath + "/"))
                .willReturn(aResponse().withStatus(200)));

        bookWasUpdatedListener.onBookWasDeleted(new BookWasDeletedEvent(book, user.getId()));

        assertThat(outputStreamCaptor.toString().trim())
                .contains("Book (" + book.getTitle() + ") was deleted.");

        verify(deleteRequestedFor(urlPathEqualTo(bookshelfPath))
                .withQueryParam("bookId", equalTo(book.getId().toString())));
        verify(deleteRequestedFor(urlPathTemplate(reviewPath + "/{bookId}/{userId}"))
                .withPathParam("bookId", equalTo(book.getId().toString()))
                .withPathParam("userId", equalTo(user.getId().toString())));
    }

    @Test
    public void testDeleteInvalidUser() {
        Book book = new Book("title", null, null, "", 1);
        bookRepository.saveAndFlush(book);

        AppUser user = new AppUser(new Username("user"), "test@email.com", new HashedPassword("pass"));
        user.setAuthority(Authority.REGULAR_USER);
        userRepository.save(user);

        stubFor(delete(urlEqualTo(reviewPath + "/" + book.getId() + "/" + user.getId()))
                .willReturn(aResponse().withStatus(401)));

        bookWasUpdatedListener.onBookWasDeleted(new BookWasDeletedEvent(book, user.getId()));

        assertThat(outputStreamCaptor.toString().trim())
                .contains("Book (" + book.getTitle() + ") was deleted.");

        verify(deleteRequestedFor(urlPathTemplate(reviewPath + "/{bookId}/{userId}"))
                .withPathParam("bookId", equalTo(book.getId().toString()))
                .withPathParam("userId", equalTo(user.getId().toString())));
    }

    /**
     * Set up for the testing environment after each test.
     */
    @AfterAll
    public static void afterEach() {
        bookshelfServer.stop();
    }
}
