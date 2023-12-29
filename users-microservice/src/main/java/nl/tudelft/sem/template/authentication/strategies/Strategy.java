package nl.tudelft.sem.template.authentication.strategies;

import java.util.List;
import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.user.Authority;

public interface Strategy {
    void passToService(Book book);

    String getUnauthorizedErrorMessage();

    String getNotAuthorErrorMessage();

    List<Authority> getAllowedAuthorities();
}
