package nl.tudelft.sem.template.authentication.domain.report;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * A DDD repository for querying and persisting report aggregate roots.
 */
@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {

    /**
     * Find a report by its id.
     */
    Optional<Report> findById(UUID id);

    @Modifying
    @Query("DELETE FROM Report r WHERE r.userId = :user_id")
    void deleteByUserId(@Param("user_id") UUID id);
}
