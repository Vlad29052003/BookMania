package nl.tudelft.sem.template.authentication.domain.rolechange;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleChangeRepository extends JpaRepository<RoleChange, UUID> {

    @Query("SELECT r FROM RequestRoleChange r WHERE r.username = :username")
    Optional<List<RoleChange>> findByUsername(@Param("username") String username);
}
