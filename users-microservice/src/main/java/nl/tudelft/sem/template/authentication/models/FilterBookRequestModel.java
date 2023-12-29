package nl.tudelft.sem.template.authentication.models;

import lombok.Getter;
import nl.tudelft.sem.template.authentication.domain.book.Book;

@Getter
public class FilterBookRequestModel {
    private final transient Book book;
    private final transient String bearerToken;

    public FilterBookRequestModel(Book book, String bearerToken) {
        this.book = book;
        this.bearerToken = bearerToken.substring(7);
    }
}
