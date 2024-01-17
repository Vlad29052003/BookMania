package nl.tudelft.sem.template.authentication.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.book.Genre;

/**
 * A DDD object to be used in the communication with
 * Review microservice as they have the id of a book named bookId instead of id as us.
 */
@Data
public class BookshelfBookModel {
    private UUID bookId;
    private String title;
    private List<String> authors;
    private List<Genre> genres;
    private String description;
    private int numPages;

    /**
     * Creates a new BookshelfBookModel object given a book.
     *
     * @param book is the book.
     */
    public BookshelfBookModel(Book book) {
        this.bookId = book.getId();
        this.title = book.getTitle();
        this.authors = new ArrayList<>(book.getAuthors());
        this.genres = new ArrayList<>(book.getGenre());
        this.description = book.getDescription();
        this.numPages = book.getNumPages();
    }
}
