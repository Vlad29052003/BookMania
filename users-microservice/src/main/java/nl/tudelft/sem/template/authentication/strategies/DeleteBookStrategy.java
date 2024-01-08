package nl.tudelft.sem.template.authentication.strategies;

import java.util.List;
import lombok.Getter;
import nl.tudelft.sem.template.authentication.domain.book.BookService;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.models.FilterBookRequestModel;

public class DeleteBookStrategy implements Strategy {
    private final transient BookService bookService;
    @Getter
    private final transient List<Authority> allowedAuthorities = List.of(Authority.ADMIN);

    public DeleteBookStrategy(BookService bookService) {
        this.bookService = bookService;
    }

    @Override
    public void passToService(FilterBookRequestModel bookRequest) {
        this.bookService.deleteBook(bookRequest.getBook().getId(), bookRequest.getUsername());
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
