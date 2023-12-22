package nl.tudelft.sem.template.authentication.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import nl.tudelft.sem.template.authentication.application.book.BookWasUpdatedListener;
import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.book.BookWasCreatedEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class BookshelfServiceIntegrationTests {
        @Autowired
        private transient MockMvc mockMvc;

        @Autowired
        private transient BookWasUpdatedListener bookWasUpdatedListener;

        private WireMockServer bookshelfServer;

        /**
         * Set up for the testing environment before all tests.
         */
        @BeforeEach
        public void setup() {
            bookshelfServer = new WireMockServer();
            bookshelfServer.start();
            WireMock.configureFor("http://localhost", 8081, "/a");
            stubFor(post(urlEqualTo("/catalog")).willReturn(aResponse().withStatus(200)));
            stubFor(put(urlEqualTo("/catalog")).willReturn(aResponse().withStatus(200)));
            stubFor(delete(urlEqualTo("/catalog")).willReturn(aResponse().withStatus(200)));

        }

        @Test
        public void test() {

        }

        /**
         * Set up for the testing environment after each tests.
         */
        @AfterEach
        public void afterEach() {
            bookshelfServer.stop();
        }
}
