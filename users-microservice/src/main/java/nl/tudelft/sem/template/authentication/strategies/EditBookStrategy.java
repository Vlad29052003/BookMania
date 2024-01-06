package nl.tudelft.sem.template.authentication.strategies;

import java.util.List;
import lombok.Getter;
import nl.tudelft.sem.template.authentication.domain.book.BookService;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.models.FilterBookRequestModel;

public class EditBookStrategy implements Strategy {
    private final transient BookService bookService;
    @Getter
    private final transient List<Authority> allowedAuthorities = List.of(Authority.ADMIN, Authority.AUTHOR);

    public EditBookStrategy(BookService bookService) {
        this.bookService = bookService;
    }

    @Override
    public void passToService(FilterBookRequestModel bookRequest) {
        this.bookService.updateBook(bookRequest.getBook());
    }

    @Override
    public String getUnauthorizedErrorMessage() {
        return "Only admins or authors may update books in the system!";
    }

    @Override
    public String getNotAuthorErrorMessage() {
        return "Only the authors of the book may edit it!";
    }
}
