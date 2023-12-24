package nl.tudelft.sem.template.authentication.filters;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import nl.tudelft.sem.template.authentication.strategy.AddBookStrategy;
import nl.tudelft.sem.template.authentication.strategy.EditBookStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles({"test"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class FilterClientTests {
    @Autowired
    private transient FilterClient filterClient;

    @Test
    public void testSetAddStrategy() {
        filterClient.setAddBookStrategy();

        assertThat(filterClient.getCheckUserExistenceHandler().getStrategy()).isInstanceOf(AddBookStrategy.class);
        assertThat(filterClient.getCheckAuthorityHandler().getStrategy()).isInstanceOf(AddBookStrategy.class);
        assertThat(filterClient.getCheckAuthorHandler().getStrategy()).isInstanceOf(AddBookStrategy.class);
    }

    @Test
    public void testSetEditStrategy() {
        filterClient.setEditBookStrategy();

        assertThat(filterClient.getCheckUserExistenceHandler().getStrategy()).isInstanceOf(EditBookStrategy.class);
        assertThat(filterClient.getCheckAuthorityHandler().getStrategy()).isInstanceOf(EditBookStrategy.class);
        assertThat(filterClient.getCheckAuthorHandler().getStrategy()).isInstanceOf(EditBookStrategy.class);
    }
}
