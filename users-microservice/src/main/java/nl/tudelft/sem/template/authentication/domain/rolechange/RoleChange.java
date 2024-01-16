package nl.tudelft.sem.template.authentication.domain.rolechange;

import java.util.Objects;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "rolechanges")
@NoArgsConstructor
@ToString
@Getter
@Setter
public class RoleChange {

    @Id
    @Column(name = "user_id", nullable = false)
    @Type(type = "uuid-char")
    private UUID userId;

    @Column(name = "newRole", nullable = false)
    private Authority newRole;

    @Column(name = "ssn", nullable = false)
    private String ssn;

    /**
     * Create a new RequestRoleChange.
     *
     * @param userId the id of the user.
     * @param newRole role the user wishes to change to.
     * @param ssn ssn of the user, as proof of identity.
     */
    public RoleChange(UUID userId, Authority newRole, String ssn) {
        this.userId = userId;
        this.newRole = newRole;
        this.ssn = ssn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        RoleChange that = (RoleChange) o;
        return this.userId.equals(that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
