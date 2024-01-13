package nl.tudelft.sem.template.authentication.domain.stats;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;



/**
 * A DDD repository for querying and persisting stats.
 */
@Repository
public interface StatsRepository extends JpaRepository<Stats, UUID> {

    /**
     * Find a user's stats by id.
     */
    public Optional<Stats> findById(UUID id);

    @Modifying
    @Query("UPDATE Stats s SET s.numberOfLogins = s.numberOfLogins+1 WHERE s.id = :userId")
    void increaseStatsOnLogin(@Param("userId") UUID userId);

}
