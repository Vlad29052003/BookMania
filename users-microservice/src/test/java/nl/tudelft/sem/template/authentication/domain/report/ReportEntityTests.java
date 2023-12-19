package nl.tudelft.sem.template.authentication.domain.report;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.ArrayList;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ReportEntityTests {

    private transient Report firstReport;
    private transient Report secondReport;

    /**
     * Set up the testing environment.
     */
    @BeforeEach
    public void setUp() {
        firstReport = new Report(UUID.randomUUID(), ReportType.REVIEW, UUID.randomUUID().toString(), "Some language");
        secondReport = new Report(UUID.randomUUID(), ReportType.REVIEW, UUID.randomUUID().toString(), "Some other language");

        firstReport.setId(UUID.randomUUID());
        secondReport.setId(UUID.randomUUID());
        while (secondReport.getId().equals(firstReport.getId())) {
            secondReport.setId(UUID.randomUUID());
        }
    }

    @Test
    public void equalsTest() {
        assertThat(firstReport).isNotEqualTo(null);
        assertThat(firstReport).isNotEqualTo(new ArrayList<>());
        assertThat(firstReport).isEqualTo(firstReport);
        assertThat(firstReport).isNotEqualTo(secondReport);
    }

    @Test
    public void hashTest() {
        assertThat(firstReport.hashCode()).isEqualTo(firstReport.hashCode());
        assertThat(firstReport.hashCode()).isNotEqualTo(secondReport.hashCode());
    }

    @Test
    public void testToString() {
        assertThat(firstReport.toString()).isNotEqualTo(secondReport.toString());
        assertThat(firstReport.toString()).isEqualTo(firstReport.toString());
    }
}
