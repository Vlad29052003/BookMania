package nl.tudelft.sem.template.authentication.strategy;

import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.book.BookService;

public class AddBookStrategy implements Strategy {
    private final transient BookService bookService;

    public AddBookStrategy(BookService bookService) {
        this.bookService = bookService;
    }


    @Override
    public void passToService(Book book) {
        bookService.addBook(book);
    }

    @Override
    public String getUnauthorizedErrorMessage() {
        return "Only admins or authors may add books to the system!";
    }

    @Override
    public String getNotAuthorErrorMessage() {
        return "Only the authors of the book may add it to the system!";
    }
}
