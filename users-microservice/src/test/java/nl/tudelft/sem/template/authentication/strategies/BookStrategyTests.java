package nl.tudelft.sem.template.authentication.strategies;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;

import nl.tudelft.sem.template.authentication.domain.book.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
public class BookStrategyTests {
    private transient AddBookStrategy addBookStrategy;
    private transient EditBookStrategy editBookStrategy;
    private transient DeleteBookStrategy deleteBookStrategy;

    /**
     * Sets up the testing environment.
     */
    @BeforeEach
    public void setUp() {
        BookService bookService = mock(BookService.class);
        this.addBookStrategy = new AddBookStrategy(bookService);
        this.editBookStrategy = new EditBookStrategy(bookService);
        this.deleteBookStrategy = new DeleteBookStrategy(bookService);
    }

    @Test
    public void testGetUnauthorizedErrorMessage() {
        assertThat(addBookStrategy.getUnauthorizedErrorMessage())
                .isEqualTo("Only admins or authors may add books to the system!");
        assertThat(editBookStrategy.getUnauthorizedErrorMessage())
                .isEqualTo("Only admins or authors may update books in the system!");
        assertThat(deleteBookStrategy.getUnauthorizedErrorMessage())
                .isEqualTo("Only admins may delete books from the system!");
    }

    @Test
    public void testGetNotAuthorErrorMessage() {
        assertThat(addBookStrategy.getNotAuthorErrorMessage())
                .isEqualTo("Only the authors of the book may add it to the system!");
        assertThat(editBookStrategy.getNotAuthorErrorMessage())
                .isEqualTo("Only the authors of the book may edit it!");
        assertThat(deleteBookStrategy.getNotAuthorErrorMessage())
                .isNull();
    }
}
