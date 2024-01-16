package nl.tudelft.sem.template.authentication.config;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class ConfigureMockServers {
    private transient WireMockServer bookshelfMockServer;
    private transient WireMockServer reviewMockServer;

    @PostConstruct
    public void startWireMocks() {
        startBookshelfMockServer();
        startReviewMockServer();
    }

    @PreDestroy
    public void stopWireMocks() {
        stopMockServer(bookshelfMockServer);
        stopMockServer(reviewMockServer);
    }

    private void startBookshelfMockServer() {
        bookshelfMockServer = new WireMockServer(new WireMockConfiguration().port(8080));
        bookshelfMockServer.start();

        configureFor("localhost", 8080);
        stubFor(any(urlPathMatching(".*"))
                .willReturn(aResponse()
                        .withStatus(200)));
    }

    private void startReviewMockServer() {
        reviewMockServer = new WireMockServer(new WireMockConfiguration().port(8082));
        reviewMockServer.start();

        configureFor("localhost", 8082);
        stubFor(any(urlPathMatching(".*"))
                .willReturn(aResponse()
                        .withStatus(200)));
    }

    private void stopMockServer(WireMockServer mockServer) {
        if (mockServer != null) {
            mockServer.stop();
        }
    }
}
