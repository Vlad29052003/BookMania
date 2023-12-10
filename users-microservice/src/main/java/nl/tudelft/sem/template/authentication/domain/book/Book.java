package nl.tudelft.sem.template.authentication.domain.book;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

/**
 * A DDD entity representing a book in our domain.
 */
@Entity
@Table(name = "books")
@NoArgsConstructor
@ToString
public class Book {
    /**
     * Identifier for the book.
     */
    @Id
    @Getter
    @Setter
    @Column(name = "book_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Type(type = "uuid-char")
    private UUID id;

    @Getter
    @Setter
    @Column(name = "title", nullable = false)
    private String title;

    @Getter
    @Setter
    @ElementCollection(targetClass = String.class)
    @CollectionTable(name = "authors", joinColumns = @JoinColumn(name = "book_id"))
    @Column(name = "name", nullable = false)
    private List<String> authors;

    @Getter
    @Setter
    @ElementCollection(targetClass = Genre.class)
    @CollectionTable(name = "book_genres", joinColumns = @JoinColumn(name = "book_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "genre", nullable = false)
    private List<Genre> genres;


    @Getter
    @Setter
    @Column(name = "description")
    private String description;

    @Getter
    @Setter
    @Column(name = "number_of_pages")
    private int numPages;

    @Getter
    @JsonIgnore
    @OneToMany(mappedBy = "favouriteBook", cascade = CascadeType.ALL)
    private List<AppUser> usersWithBookAsFavorite;

    /**
     * Create new book.
     *
     * @param title   The title of the new book.
     * @param authors The list of authors of the new book.
     * @param genres  The list of genres of the new book.
     */
    public Book(String title, List<String> authors, List<Genre> genres, String description, int numPages) {
        this.title = title;
        this.authors = authors == null ? new ArrayList<>() : authors;
        this.genres = genres == null ? new ArrayList<>() : genres;
        this.description = description;
        this.numPages = numPages;
        this.usersWithBookAsFavorite = new ArrayList<>();
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
        return id.equals(book.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
