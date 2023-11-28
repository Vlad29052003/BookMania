package nl.tudelft.sem.template.authentication.domain.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * A DDD repository for querying and persisting user aggregate roots.
 */
@Repository
public interface UserRepository extends JpaRepository<AppUser, String> {
    /**
     * Find user by NetID.
     */
    Optional<AppUser> findByUsername(Username username);

    /**
     * Check if an existing user already uses a NetID.
     */
    boolean existsByUsername(Username username);

    /**
     * Find user by email.
     */
    Optional<AppUser> findByEmail(String email);
}
