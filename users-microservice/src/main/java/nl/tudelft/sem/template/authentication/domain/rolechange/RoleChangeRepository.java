package nl.tudelft.sem.template.authentication.domain.rolechange;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleChangeRepository extends JpaRepository<RoleChange, UUID> {

    @Query("SELECT r FROM RoleChange r WHERE r.username = :username")
    Optional<List<RoleChange>> findByUsername(@Param("username") String username);

    @Modifying
    @Query("DELETE FROM RoleChange r WHERE r.username = :username")
    void deleteByUsername(@Param("username") String username);
}
