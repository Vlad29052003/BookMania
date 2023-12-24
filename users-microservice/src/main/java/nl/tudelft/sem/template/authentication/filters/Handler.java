package nl.tudelft.sem.template.authentication.filters;

import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.strategy.Strategy;

public interface Handler {
    void setNext(Handler handler);

    void setStrategy(Strategy strategy);

    void filter(Book book, String bearerToken);
}
