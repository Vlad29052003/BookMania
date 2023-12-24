package nl.tudelft.sem.template.authentication.application;

import nl.tudelft.sem.template.authentication.Application;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ApplicationTests {

    // Just to get full coverage.
    @Test
    public void applicationContextTest() {
        Application.main(new String[] {});
    }
}
