package nl.tudelft.sem.template.authentication.domain.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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
        assertEquals(firstReport, firstReport);
        assertNotEquals(firstReport, secondReport);
    }

    @Test
    public void hashTest() {
        assertEquals(firstReport.hashCode(), firstReport.hashCode());
        assertNotEquals(firstReport.hashCode(), secondReport.hashCode());
    }
}
