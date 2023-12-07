package nl.tudelft.sem.template.authentication.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import nl.tudelft.sem.template.authentication.controllers.ReportController;
import nl.tudelft.sem.template.authentication.domain.report.Report;
import nl.tudelft.sem.template.authentication.domain.report.ReportService;
import nl.tudelft.sem.template.authentication.domain.report.ReportType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

public class ReportControllerTests {

    private transient ReportService reportService;
    private transient ReportController reportController;
    private transient Report report;
    private transient UUID repordId;
    private transient String token;

    /**
     * Set up the testing environment.
     */
    @BeforeEach
    public void setUp() {
        this.reportService = mock(ReportService.class);
        this.reportController = new ReportController(reportService);
        this.report = new Report(ReportType.REVIEW, UUID.randomUUID().toString(), "Report");
        this.repordId = UUID.randomUUID();
        this.token = "someToken";
    }

    @Test
    public void getAllReportsTest() {
        when(reportService.getAllReports(token)).thenReturn(List.of(report));
        assertEquals(reportController.getAllReports(token), ResponseEntity.ok(List.of(report)));

        when(reportService.getAllReports(token))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Only admins can access reports!"));
        assertEquals(reportController.getAllReports(token).getStatusCodeValue(), 401);
    }

    @Test
    public void addReportTest() {
        assertEquals(reportController.addReport(report, token).getStatusCodeValue(), 200);

        doThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token!"))
                .when(reportService).addReport(any(), any());
        assertEquals(reportController.addReport(report, token).getStatusCodeValue(), 401);

        doThrow(new ResponseStatusException((HttpStatus.NOT_FOUND), "Reported user not found!"))
                .when(reportService).addReport(any(), any());
        assertEquals(reportController.addReport(report, token).getStatusCodeValue(), 404);
    }

    @Test
    public void deleteReportTest() {
        assertEquals(reportController.deleteReport(repordId.toString(), token).getStatusCodeValue(), 200);

        doThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Only admins can access reports!"))
                .when(reportService).remove(any(), any());
        assertEquals(reportController.deleteReport(repordId.toString(), token).getStatusCodeValue(), 401);

        doThrow(new ResponseStatusException((HttpStatus.NOT_FOUND), "The report does not exist!"))
                .when(reportService).remove(any(), any());
        assertEquals(reportController.deleteReport(repordId.toString(), token).getStatusCodeValue(), 404);
    }
}
