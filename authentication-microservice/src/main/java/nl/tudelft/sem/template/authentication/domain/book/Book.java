package nl.tudelft.sem.template.authentication.domain.book;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * A DDD entity representing a book in our domain.
 */
@Entity
@Table(name = "books")
@NoArgsConstructor
public class Book {
    /**
     * Identifier for the book.
     */
    @Id
    @Column(name = "id", nullable = false)
    private int id;

    @Getter
    @Column(name = "title", nullable = false)
    private String title;

    @Getter
    @ElementCollection
    @CollectionTable(name = "authors", joinColumns = @JoinColumn(name = "book_id"))
    @Column(name = "name", nullable = false)
    private List<String> authors;

    @Getter
    @ElementCollection(targetClass = Genre.class)
    @CollectionTable(name = "book_genres", joinColumns = @JoinColumn(name = "book_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "genre", nullable = false)
    private List<Genre> genres;

    /**
     * Create new book.
     *
     * @param title The title of the new book.
     * @param authors The list of authors of the new book.
     * @param genres The list of genres of the new book.
     */
    public Book(String title, List<String> authors, List<Genre> genres) {
        this.title = title;
        this.authors = authors == null ? new ArrayList<>() : authors;
        this.genres = genres == null ? new ArrayList<>() : genres;
    }

    /**
     * Equality is only based on the identifier.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Book book = (Book) o;
        return id == (book.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
