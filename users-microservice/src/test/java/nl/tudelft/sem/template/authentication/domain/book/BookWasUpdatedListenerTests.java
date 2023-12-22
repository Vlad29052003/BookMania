package nl.tudelft.sem.template.authentication.domain.book;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import nl.tudelft.sem.template.authentication.application.book.BookWasUpdatedListener;
import nl.tudelft.sem.template.authentication.domain.book.BookWasCreatedEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayOutputStream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class BookWasUpdatedListenerTests {
        @Autowired
        private transient MockMvc mockMvc;

        @Autowired
        private transient BookWasUpdatedListener bookWasUpdatedListener;

        private static WireMockServer bookshelfServer;

        private transient ByteArrayOutputStream outputStreamCaptor;

        @BeforeAll
        static void init() {
            bookshelfServer = new WireMockServer(
                    new WireMockConfiguration().port(8081)
            );
            bookshelfServer.start();
            WireMock.configureFor("localhost", 8081);
        }

        @Test
        public void testPost() {
            stubFor(post(urlEqualTo("/a/catalog")).willReturn(aResponse().withStatus(200)));
            stubFor(put(urlEqualTo("/a/catalog")).willReturn(aResponse().withStatus(200)));
            stubFor(delete(urlEqualTo("/a/catalog")).willReturn(aResponse().withStatus(200)));

            Book book = new Book("title", null, null, "", 1);

            bookWasUpdatedListener.onBookWasCreated(new BookWasCreatedEvent(book));

            verify(putRequestedFor(urlPathEqualTo("/a/catalog")));

            assertThat(outputStreamCaptor.toString().trim())
                    .isEqualTo("Book (" + book.getTitle() + ") was created.");
        }

        /**
         * Set up for the testing environment after each test.
         */
        @AfterEach
        public void afterEach() {
            bookshelfServer.stop();
        }
}
