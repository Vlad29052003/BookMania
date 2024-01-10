package nl.tudelft.sem.template.authentication.domain.user;

import java.util.Optional;
import java.util.UUID;
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

}
