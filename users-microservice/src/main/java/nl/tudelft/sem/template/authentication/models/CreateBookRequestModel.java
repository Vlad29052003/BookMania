package nl.tudelft.sem.template.authentication.models;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.tudelft.sem.template.authentication.domain.book.Book;
import nl.tudelft.sem.template.authentication.domain.book.Genre;

@Data
@NoArgsConstructor
public class CreateBookRequestModel {
    private String title;
    private List<String> authors;
    private List<Genre> genres;
    private String description;
    private int numPages;

    /**
     * Creates a new instance of this object.
     *
     * @param book is a book.
     */
    public CreateBookRequestModel(Book book) {
        this.title = book.getTitle();
        this.authors = book.getAuthors();
        this.genres = book.getGenres();
        this.description = book.getDescription();
        this.numPages = book.getNumPages();
    }
}
