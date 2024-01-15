package nl.tudelft.sem.template.authentication.domain.stats;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class StatsEntityTests {
    private transient Stats firstStats;
    private transient Stats secondStats;


    @BeforeEach
    public void setUp() {
        firstStats = new Stats(UUID.randomUUID(), 1);
        secondStats = new Stats(UUID.randomUUID(), 2);
    }

    @Test
    public void equalsTest() {
        assertThat(firstStats).isNotEqualTo(null);
        assertThat(firstStats).isNotEqualTo(new Object());
        assertThat(firstStats).isEqualTo(firstStats);
        assertThat(firstStats).isNotEqualTo(secondStats);
    }

    @Test
    public void hashTest() {
        assertThat(firstStats.hashCode()).isEqualTo(firstStats.hashCode());
        assertThat(firstStats.hashCode()).isNotEqualTo(secondStats.hashCode());
    }

    @Test
    public void testToString() {
        assertThat(firstStats.toString()).isNotEqualTo(secondStats.toString());
        assertThat(firstStats.toString()).isEqualTo(firstStats.toString());
    }

}
