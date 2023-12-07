package nl.tudelft.sem.template.authentication.domain.report;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
