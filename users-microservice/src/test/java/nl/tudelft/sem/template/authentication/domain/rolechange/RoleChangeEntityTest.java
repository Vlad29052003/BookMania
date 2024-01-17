package nl.tudelft.sem.template.authentication.domain.rolechange;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.UUID;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
public class RoleChangeEntityTest {
    private transient RoleChange change1;
    private transient RoleChange change2;
    private transient RoleChange change3;

    /**
     * Set up the testing environment.
     */
    @BeforeEach
    public void setUp() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        while (id1.equals(id2)) {
            id2 = UUID.randomUUID();
        }

        change1 = new RoleChange(id1, Authority.AUTHOR, "123-456");
        change2 = new RoleChange(id2, Authority.ADMIN, "123-546-3460");
        change3 = new RoleChange(id1, Authority.AUTHOR, "123-456");
    }

    @Test
    public void emptyConstructorTest() {
        RoleChange newChange = new RoleChange();
        assertThat(newChange).isNotEqualTo(null);
    }

    @Test
    public void constructorTest() {
        assertThat(change1).isNotEqualTo(null);
        assertThat(change1.getId()).isEqualTo(change3.getId());
        assertThat(change1.getNewRole()).isEqualTo(Authority.AUTHOR);
        assertThat(change1.getSsn()).isEqualTo("123-456");
    }

    @Test
    public void equalsTest() {
        assertThat(change1).isNotEqualTo(null);
        assertThat(change1).isEqualTo(change1);
        assertThat(change1).isNotEqualTo(change2);
        assertThat(change1).isEqualTo(change3);
        assertThat(change1).isNotEqualTo("change1");
    }

    @Test
    public void hashCodeTest() {
        assertThat(change1.hashCode()).isEqualTo(change3.hashCode());
        assertThat(change1.hashCode()).isNotEqualTo(change2.hashCode());
        assertThat(change1.hashCode()).isEqualTo(Objects.hash(change1.getId()));
    }

    @Test
    public void toStringTest() {
        assertThat(change1.toString()).isEqualTo(change1.toString());
        assertThat(change1.toString()).isNotEqualTo(change2.toString());
    }
}
