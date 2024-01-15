package nl.tudelft.sem.template.authentication.domain.stats;


import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Type;

/**
 * A DDD entity representing an application user in our domain.
 */
@Getter
@Entity
@Table(name = "stats")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Stats {
    /**
     * Identifier for the application user.
     */
    @Setter
    @Id
    @Column(name = "id", nullable = false)
    @Type(type = "uuid-char")
    private UUID id;


    @Setter
    @Column(name = "numberOfLogins", nullable = false)
    private int numberOfLogins;

}
