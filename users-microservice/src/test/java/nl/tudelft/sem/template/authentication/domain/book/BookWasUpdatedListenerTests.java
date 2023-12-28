package nl.tudelft.sem.template.authentication.domain.book;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import nl.tudelft.sem.template.authentication.application.book.BookWasUpdatedListener;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.http.HttpRequest;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class BookWasUpdatedListenerTests {

    private final transient BookWasUpdatedListener bookWasUpdatedListener = new BookWasUpdatedListener();

    @Autowired
    private transient BookRepository bookRepository;

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

        stubFor(post(urlEqualTo("/a/catalog")).willReturn(aResponse().withStatus(200)));


        outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @Test
    public void testPut() {
        Book book = new Book("title", null, null, "", 1);
        bookRepository.saveAndFlush(book);

        stubFor(put(urlEqualTo("/a/catalog"))
                .willReturn(aResponse().withStatus(200)));

        bookWasUpdatedListener.onBookWasCreated(new BookWasCreatedEvent(book));

        assertThat(outputStreamCaptor.toString().trim())
                .contains("Book (" + book.getTitle() + ") was created.");

        verify(putRequestedFor(urlPathEqualTo("/a/catalog"))
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

        verify(postRequestedFor(urlPathEqualTo("/a/catalog"))
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

        stubFor(delete(urlEqualTo("/a/catalog"))
                .willReturn(aResponse().withStatus(200)));

        bookWasUpdatedListener.onBookWasDeleted(new BookWasDeletedEvent(book));

        assertThat(outputStreamCaptor.toString().trim())
                .contains("Book (" + book.getTitle() + ") was deleted.");

        verify(deleteRequestedFor(urlPathEqualTo("/a/catalog"))
                .withQueryParam("bookId", equalTo(book.getId().toString())));
    }

    /**
     * Set up for the testing environment after each test.
     */
    @AfterAll
    public static void afterEach() {
        bookshelfServer.stop();
    }
}
