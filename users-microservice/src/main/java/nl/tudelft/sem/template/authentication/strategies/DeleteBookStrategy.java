package nl.tudelft.sem.template.authentication.strategies;

import java.util.List;
import lombok.Getter;
import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.book.BookService;
import nl.tudelft.sem.template.authentication.domain.user.Authority;

public class DeleteBookStrategy implements Strategy {
    private final transient BookService bookService;
    @Getter
    private final transient List<Authority> allowedAuthorities = List.of(Authority.ADMIN);

    public DeleteBookStrategy(BookService bookService) {
        this.bookService = bookService;
    }

    @Override
    public void passToService(Book book) {
        this.bookService.deleteBook(book.getId());
    }

    @Override
    public String getUnauthorizedErrorMessage() {
        return "Only admins may delete books from the system!";
    }

    @Override
    public String getNotAuthorErrorMessage() {
        return null;
    }
}
