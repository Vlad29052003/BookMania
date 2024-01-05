package nl.tudelft.sem.template.authentication.domain.book;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathTemplate;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import nl.tudelft.sem.template.authentication.application.book.BookEventsListener;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.domain.user.HashedPassword;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import nl.tudelft.sem.template.authentication.domain.user.Username;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class BookEventsListenerTests {

    private static final String bookshelfPath = "/a/catalog";
    private static final String reviewPath = "/b/book";
    private final transient BookEventsListener bookEventsListener = new BookEventsListener();

    @Autowired
    private transient BookRepository bookRepository;

    @Autowired
    private transient UserRepository userRepository;

    private static WireMockServer mockServer;

    private static ByteArrayOutputStream outputStreamCaptor;

    /**
     * Set up before all tests.
     */
    @BeforeAll
    static void init() {

        mockServer = new WireMockServer(
                new WireMockConfiguration().port(8080)
        );
        mockServer.start();

        WireMock.configureFor("localhost", 8080);

        outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));

        // Since wiremock is configured on 8080, we assume everything is on the same port.
        BookEventsListener.BOOKSHELF_URI = "http://localhost:8080/a/catalog";
        BookEventsListener.REVIEW_URI = "http://localhost:8080/b/book";
    }

    @Test
    public void testPost() {
        Book book = new Book("title", null, null, "", 1);
        bookRepository.saveAndFlush(book);

        stubFor(post(urlEqualTo(bookshelfPath))
                .willReturn(aResponse().withStatus(200)));

        outputStreamCaptor.reset();

        bookEventsListener.onBookWasCreated(new BookWasCreatedEvent(book));

        assertThat(outputStreamCaptor.toString().trim())
                .isEqualTo("Book (id: " + book.getId() + ", title: " + book.getTitle() + ") was created.");

        verify(postRequestedFor(urlPathEqualTo(bookshelfPath))
                .withRequestBody(containing("\"id\":\"" + book.getId().toString() + "\""))
                .withRequestBody(containing("\"title\":\"" + book.getTitle() + "\""))
                .withRequestBody(containing("\"authors\":" + book.getAuthors().toString()))
                .withRequestBody(containing("\"genres\":" + book.getGenres().toString()))
                .withRequestBody(containing("\"description\":\"" + book.getDescription() + "\""))
                .withRequestBody(containing("\"numPages\":" + book.getNumPages())));
    }

    @Test
    public void testPostException() {
        Book book = new Book("title", null, null, "", 1);
        bookRepository.saveAndFlush(book);

        stubFor(post(urlEqualTo(bookshelfPath))
                .willReturn(aResponse().withStatus(404)));

        outputStreamCaptor.reset();

        assertThrows(RuntimeException.class, () ->
            bookEventsListener.onBookWasCreated(new BookWasCreatedEvent(book)));

        assertThat(outputStreamCaptor.toString().trim())
                .isNotEqualTo("Book (id: " + book.getId() + ", title: " + book.getTitle() + ") was created.");

        verify(postRequestedFor(urlPathEqualTo(bookshelfPath))
                .withRequestBody(containing("\"id\":\"" + book.getId().toString() + "\""))
                .withRequestBody(containing("\"title\":\"" + book.getTitle() + "\""))
                .withRequestBody(containing("\"authors\":" + book.getAuthors().toString()))
                .withRequestBody(containing("\"genres\":" + book.getGenres().toString()))
                .withRequestBody(containing("\"description\":\"" + book.getDescription() + "\""))
                .withRequestBody(containing("\"numPages\":" + book.getNumPages())));
    }

    @Test
    public void testPut() {
        Book book = new Book("title", null, null, "", 1);
        bookRepository.saveAndFlush(book);

        stubFor(put(urlEqualTo(bookshelfPath))
                .willReturn(aResponse().withStatus(200)));

        outputStreamCaptor.reset();

        bookEventsListener.onBookWasEdited(new BookWasEditedEvent(book));

        assertThat(outputStreamCaptor.toString().trim())
                .isEqualTo("Book (id: " + book.getId() + ", title: " + book.getTitle() + ") was edited.");

        verify(putRequestedFor(urlPathEqualTo(bookshelfPath))
                .withRequestBody(containing("\"id\":\"" + book.getId().toString() + "\""))
                .withRequestBody(containing("\"title\":\"" + book.getTitle() + "\""))
                .withRequestBody(containing("\"authors\":" + book.getAuthors().toString()))
                .withRequestBody(containing("\"genres\":" + book.getGenres().toString()))
                .withRequestBody(containing("\"description\":\"" + book.getDescription() + "\""))
                .withRequestBody(containing("\"numPages\":" + book.getNumPages())));
    }

    @Test
    public void testPutException() {
        Book book = new Book("title", null, null, "", 1);
        bookRepository.saveAndFlush(book);

        stubFor(put(urlEqualTo(bookshelfPath))
                .willReturn(aResponse().withStatus(404)));

        outputStreamCaptor.reset();

        assertThrows(RuntimeException.class, () ->
            bookEventsListener.onBookWasEdited(new BookWasEditedEvent(book)));

        assertThat(outputStreamCaptor.toString().trim())
                .isNotEqualTo("Book (id: " + book.getId() + ", title: " + book.getTitle() + ") was edited.");

        verify(putRequestedFor(urlPathEqualTo(bookshelfPath))
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

        stubFor(delete(urlEqualTo(bookshelfPath + "?bookId=" + book.getId().toString()))
                .willReturn(aResponse().withStatus(200)));
        stubFor(delete(urlEqualTo(reviewPath + "/" + book.getId() + "/" + user.getId()))
                .willReturn(aResponse().withStatus(200)));

        outputStreamCaptor.reset();

        bookEventsListener.onBookWasDeleted(new BookWasDeletedEvent(book, user.getId()));

        assertThat(outputStreamCaptor.toString().trim())
                .isEqualTo("Book (id: " + book.getId() + ", title: " + book.getTitle() + ") was deleted.");

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
                .willReturn(aResponse().withStatus(404)));
        stubFor(delete(urlEqualTo(bookshelfPath + "?bookId=" + book.getId()))
                .willReturn(aResponse().withStatus(200)));

        outputStreamCaptor.reset();

        assertThrows(RuntimeException.class, () ->
                bookEventsListener.onBookWasDeleted(new BookWasDeletedEvent(book, user.getId())));

        assertThat(outputStreamCaptor.toString().trim())
                .isNotEqualTo("Book (id: " + book.getId() + ", title: " + book.getTitle() + ") was deleted.");


        stubFor(delete(urlEqualTo(reviewPath + "/" + book.getId() + "/" + user.getId()))
                .willReturn(aResponse().withStatus(200)));
        stubFor(delete(urlEqualTo(bookshelfPath + "?bookId=" + book.getId()))
                .willReturn(aResponse().withStatus(404)));

        outputStreamCaptor.reset();

        assertThrows(RuntimeException.class, () ->
                bookEventsListener.onBookWasDeleted(new BookWasDeletedEvent(book, user.getId())));

        assertThat(outputStreamCaptor.toString().trim())
                .isNotEqualTo("Book (id: " + book.getId() + ", title: " + book.getTitle() + ") was deleted.");

        verify(deleteRequestedFor(urlPathEqualTo(bookshelfPath))
                .withQueryParam("bookId", equalTo(book.getId().toString())));
    }

    @Test
    public void testExceptionThrown() {
        Book book = new Book("title", null, null, "", 1);
        bookRepository.saveAndFlush(book);

        AppUser user = new AppUser(new Username("user"), "test@email.com", new HashedPassword("pass"));
        userRepository.save(user);

        mockServer.stop();

        outputStreamCaptor.reset();
        assertThatThrownBy(() -> bookEventsListener.onBookWasDeleted(new BookWasDeletedEvent(book, user.getId())))
                .isInstanceOf(RuntimeException.class);
        assertThat(outputStreamCaptor.toString().trim())
                .isNotEqualTo("Book (id: " + book.getId() + ", title: " + book.getTitle() + ") was deleted.");

        outputStreamCaptor.reset();
        assertThatThrownBy(() -> bookEventsListener.onBookWasEdited(new BookWasEditedEvent(book)))
                .isInstanceOf(RuntimeException.class);
        assertThat(outputStreamCaptor.toString().trim())
                .isNotEqualTo("Book (id: " + book.getId() + ", title: " + book.getTitle() + ") was edited.");

        outputStreamCaptor.reset();
        assertThatThrownBy(() -> bookEventsListener.onBookWasCreated(new BookWasCreatedEvent(book)))
                .isInstanceOf(RuntimeException.class);
        assertThat(outputStreamCaptor.toString().trim())
                .isNotEqualTo("Book (id: " + book.getId() + ", title: " + book.getTitle() + ") was created.");

        mockServer.start();
    }

    /**
     * Set up for the testing environment after all tests.
     */
    @AfterAll
    public static void afterEach() {
        mockServer.stop();
    }
}
