package nl.tudelft.sem.template.authentication.config;

import static nl.tudelft.sem.template.authentication.application.Constants.BOOKSHELF_SERVER;
import static nl.tudelft.sem.template.authentication.application.Constants.NO_SUCH_USER;
import static nl.tudelft.sem.template.authentication.application.Constants.REVIEW_SERVER;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class ConfigurationTests {
    @Test
    public void test() {
        assertThat(NO_SUCH_USER).isEqualTo("User does not exist!");
        assertThat(BOOKSHELF_SERVER).isEqualTo("http://localhost:8080/a");
        assertThat(REVIEW_SERVER).isEqualTo("http://localhost:8080/b");
    }
}
