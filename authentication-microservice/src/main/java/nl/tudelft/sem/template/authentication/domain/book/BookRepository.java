package nl.tudelft.sem.template.authentication.domain.book;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * A DDD repository for querying and persisting book aggregate roots.
 */
@Repository
public interface BookRepository extends JpaRepository<Book, String> {
    /**
     * Find a book by id.
     */
    Optional<Book> findById(int id);
}
