package nl.tudelft.sem.template.authentication.strategy;

import nl.tudelft.sem.template.authentication.domain.book.Book;

public interface Strategy {
    void passToService(Book book);

    String getUnauthorizedErrorMessage();

    String getNotAuthorErrorMessage();
}
