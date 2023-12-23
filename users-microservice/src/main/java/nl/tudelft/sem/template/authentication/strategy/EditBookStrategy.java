package nl.tudelft.sem.template.authentication.strategy;

import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.book.BookService;

public class EditBookStrategy implements Strategy {
    private final transient BookService bookService;

    public EditBookStrategy(BookService bookService) {
        this.bookService = bookService;
    }


    @Override
    public void passToService(Book book) {
        this.bookService.updateBook(book);
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
