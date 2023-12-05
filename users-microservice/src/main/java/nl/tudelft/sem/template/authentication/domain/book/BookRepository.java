package nl.tudelft.sem.template.authentication.domain.book;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * A DDD repository for querying and persisting book aggregate roots.
 */
@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {
    /**
     * Find a book by id.
     */
    Optional<Book> findById(UUID id);

    /**
     * Find a book by title.
     *
     * @param title is the title.
     * @return a list of all books with that title.
     */
    List<Book> findByTitle(String title);
}
