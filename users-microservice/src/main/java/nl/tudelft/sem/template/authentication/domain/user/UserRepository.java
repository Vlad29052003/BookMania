package nl.tudelft.sem.template.authentication.domain.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.transaction.Transactional;
import nl.tudelft.sem.template.authentication.domain.book.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


/**
 * A DDD repository for querying and persisting user aggregate roots.
 */
@Repository
public interface UserRepository extends JpaRepository<AppUser, UUID> {
    /**
     * Find user by NetID.
     */
    Optional<AppUser> findByUsername(Username username);

    /**
     * Check if an existing user already uses a NetID.
     */
    boolean existsByUsername(Username username);

    @Modifying
    @Query("UPDATE AppUser u SET u.favouriteBook = null WHERE u.favouriteBook.id = :bookId")
    void removeBookFromUsersFavorites(@Param("bookId") UUID bookId);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM user_connections WHERE follower_id = :userId OR followed_id = :userId", nativeQuery = true)
    void deleteConnectionsByUserId(@Param("userId") String userId);

    @Query(value = "SELECT book_id FROM users GROUP BY book_id ORDER BY COUNT(book_id) DESC LIMIT 5", nativeQuery = true)
    List<UUID> getMostPopularBooks();

    @Query(value = "SELECT genre FROM user_favourite_genres GROUP BY "
            + "genre ORDER BY COUNT(genre) DESC LIMIT 5", nativeQuery = true)
    List<Genre> getMostPopularGenres();


}
