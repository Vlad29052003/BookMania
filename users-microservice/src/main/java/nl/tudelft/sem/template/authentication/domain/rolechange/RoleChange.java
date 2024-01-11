package nl.tudelft.sem.template.authentication.domain.rolechange;

import java.util.Objects;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "rolechanges")
@NoArgsConstructor
@ToString
@Getter
@Setter
public class RoleChange {

    @Id
    @Column(name = "rolechange_id", nullable = false)
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Type(type = "uuid-char")
    private UUID id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "newRole", nullable = false)
    private Authority newRole;

    @Column(name = "ssn", nullable = false)
    private String ssn;

    /**
     * Create a new RequestRoleChange.
     *
     * @param username user that requests role change.
     * @param newRole role the user wishes to change to.
     * @param ssn ssn of the user, as proof of identity.
     */
    public RoleChange(String username, Authority newRole, String ssn) {
        this.username = username;
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
        return this.id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
