package nl.tudelft.sem.template.authentication.config;

import static nl.tudelft.sem.template.authentication.application.Constants.BOOKSHELF_SERVER;
import static nl.tudelft.sem.template.authentication.application.Constants.NO_SUCH_USER;
import static nl.tudelft.sem.template.authentication.application.Constants.REVIEW_SERVER;
import static nl.tudelft.sem.template.authentication.application.book.BookEventsListener.BOOKSHELF_URI;
import static nl.tudelft.sem.template.authentication.application.book.BookEventsListener.REVIEW_URI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@ActiveProfiles("test")
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class ConfigTest {
    @Autowired
    private transient DataSource dataSource;
    private final transient HttpClient client = HttpClient.newHttpClient();

    @Test
    public void testDataSourceBean() {
        DriverManagerDataSource ds = (DriverManagerDataSource) dataSource;
        assertEquals("jdbc:h2:mem:myDb;DB_CLOSE_DELAY=-1", ds.getUrl());
        assertEquals("test_user", ds.getUsername());
        assertEquals("test_pass", ds.getPassword());
    }

    @Test
    public void testConstantsSetCorrectly() {
        assertThat(NO_SUCH_USER).isEqualTo("User does not exist!");
        assertThat(BOOKSHELF_SERVER).isEqualTo("http://localhost:8080/a");
        assertThat(REVIEW_SERVER).isEqualTo("http://localhost:8082/b");
    }

    @Test
    public void testStubs() throws Exception {
        ConfigureMockServers config = new ConfigureMockServers();

        config.startWireMocks();
        HttpRequest requestBookshelf = HttpRequest.newBuilder()
                .uri(URI.create(BOOKSHELF_URI))
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .build();
        HttpResponse<?> responseBookshelf = client.send(requestBookshelf, HttpResponse.BodyHandlers.ofString());
        assertThat(responseBookshelf.statusCode()).isEqualTo(200);
        assertThat(responseBookshelf.body()).isEqualTo("Mocked Response for Bookshelf API");

        HttpRequest requestReview = HttpRequest.newBuilder()
                .uri(URI.create(REVIEW_URI))
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .build();
        HttpResponse<?> responseReview = client.send(requestReview, HttpResponse.BodyHandlers.ofString());
        assertThat(responseReview.statusCode()).isEqualTo(200);
        assertThat(responseReview.body()).isEqualTo("Mocked Response for Review API");

        config.stopWireMocks();
    }
}
