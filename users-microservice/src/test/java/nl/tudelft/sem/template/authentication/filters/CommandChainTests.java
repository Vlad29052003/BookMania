package nl.tudelft.sem.template.authentication.filters;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import nl.tudelft.sem.template.authentication.handlers.CommandChain;
import nl.tudelft.sem.template.authentication.strategies.AddBookStrategy;
import nl.tudelft.sem.template.authentication.strategies.EditBookStrategy;
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
public class CommandChainTests {
    @Autowired
    private transient CommandChain commandChain;

    @Test
    public void testChainedCorrectly() {
        assertThat(commandChain.getCheckUserExistenceHandler().getNextHandler()).isEqualTo(commandChain.getCheckAuthorityHandler());
        assertThat(commandChain.getCheckAuthorityHandler().getNextHandler()).isEqualTo(commandChain.getCheckAuthorHandler());
    }

    @Test
    public void testSetAddStrategy() {
        commandChain.setAddBookStrategy();

        assertThat(commandChain.getCheckUserExistenceHandler().getStrategy()).isInstanceOf(AddBookStrategy.class);
        assertThat(commandChain.getCheckAuthorityHandler().getStrategy()).isInstanceOf(AddBookStrategy.class);
        assertThat(commandChain.getCheckAuthorHandler().getStrategy()).isInstanceOf(AddBookStrategy.class);
    }

    @Test
    public void testSetEditStrategy() {
        commandChain.setEditBookStrategy();

        assertThat(commandChain.getCheckUserExistenceHandler().getStrategy()).isInstanceOf(EditBookStrategy.class);
        assertThat(commandChain.getCheckAuthorityHandler().getStrategy()).isInstanceOf(EditBookStrategy.class);
        assertThat(commandChain.getCheckAuthorHandler().getStrategy()).isInstanceOf(EditBookStrategy.class);
    }
}
