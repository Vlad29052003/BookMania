package nl.tudelft.sem.template.authentication.models;

import lombok.Getter;
import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.domain.user.Username;

@Getter
public class FilterBookRequestModel {
    private final transient Username username;
    private final transient Authority userAuthority;
    private final transient Book book;

    /**
     * Creates a new FilterBookRequestModel object.
     *
     * @param username is the username of the user who made the request.
     * @param userAuthority is the authority of the user who made the request.
     * @param book is the book.
     */
    public FilterBookRequestModel(Username username, Authority userAuthority, Book book) {
        this.username = username;
        this.userAuthority = userAuthority;
        this.book = book;
    }
}
