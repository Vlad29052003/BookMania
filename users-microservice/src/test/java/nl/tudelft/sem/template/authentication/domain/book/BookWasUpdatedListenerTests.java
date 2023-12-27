package nl.tudelft.sem.template.authentication.domain.book;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import nl.tudelft.sem.template.authentication.application.book.BookWasUpdatedListener;
import nl.tudelft.sem.template.authentication.domain.book.BookWasCreatedEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import wiremock.org.apache.hc.core5.http.HttpEntity;
import wiremock.org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class BookWasUpdatedListenerTests {

    private transient BookWasUpdatedListener bookWasUpdatedListener = new BookWasUpdatedListener();

    private static WireMockServer bookshelfServer;

    private static ByteArrayOutputStream outputStreamCaptor;

    /**
     * Set up before all tests.
     */
    @BeforeAll
    static void init() {
        //bookWasUpdatedListener = new BookWasUpdatedListener();

        bookshelfServer = new WireMockServer(
                new WireMockConfiguration().port(8081)
        );
        bookshelfServer.start();
        WireMock.configureFor("localhost", 8081);

        stubFor(post(urlEqualTo("/a/catalog")).willReturn(aResponse().withStatus(200)));
        stubFor(put(urlEqualTo("/a/catalog")).willReturn(aResponse().withStatus(200)));
        stubFor(delete(urlEqualTo("/a/catalog")).willReturn(aResponse().withStatus(200)));

        outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));
    }

//    @Test
//    public void testStubs() throws IOException {
//        HttpClient client = HttpClient.newHttpClient();
//
//        try {
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create("http://localhost:8081/a/catalog"))
//                    .PUT(HttpRequest.BodyPublishers.noBody())
//                    .build();
//
//            client.send(request, HttpResponse.BodyHandlers.ofString());
//
//            verify(putRequestedFor(urlPathEqualTo("/a/catalog")));
//
//            HttpRequest request2 = HttpRequest.newBuilder()
//                    .uri(URI.create("http://localhost:8081/a/catalog"))
//                    .POST(HttpRequest.BodyPublishers.noBody())
//                    .build();
//
//            client.send(request2, HttpResponse.BodyHandlers.ofString());
//
//            verify(postRequestedFor(urlPathEqualTo("/a/catalog")));
//
//            HttpRequest request3 = HttpRequest.newBuilder()
//                    .uri(URI.create("http://localhost:8081/a/catalog"))
//                    .DELETE()
//                    .build();
//
//            client.send(request3, HttpResponse.BodyHandlers.ofString());
//
//            verify(deleteRequestedFor(urlPathEqualTo("/a/catalog")));
//        } catch (IOException | InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//
//    }

    @Test
    public void testPut() {

        Book book = new Book("title", null, null, "", 1);

        bookWasUpdatedListener.onBookWasCreated(new BookWasCreatedEvent(book));

        assertThat(outputStreamCaptor.toString().trim())
                .contains("Book (" + book.getTitle() + ") was created.");

        verify(putRequestedFor(urlPathEqualTo("/a/catalog")));

    }

    @Test
    public void testPost() {
        Book book = new Book("title", null, null, "", 1);

        bookWasUpdatedListener.onBookWasEdited(new BookWasEditedEvent(book));

        assertThat(outputStreamCaptor.toString().trim())
                .contains("Book (" + book.getTitle() + ") was edited.");

        verify(postRequestedFor(urlPathEqualTo("/a/catalog")));
    }

    /**
     * Set up for the testing environment after each test.
     */
    @AfterEach
    public void afterEach() {
        bookshelfServer.stop();
    }
}
