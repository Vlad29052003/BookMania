package nl.tudelft.sem.template.authentication.controllers;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import nl.tudelft.sem.template.authentication.domain.report.Report;
import nl.tudelft.sem.template.authentication.domain.report.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/c/reports")
public class ReportController {
    public final transient ReportService reportService;

    /**
     * Creates a new ReportController.
     *
     * @param reportService report service.
     */
    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Get request for viewing all the reports.
     *
     * @param bearerToken the jwt token.
     * @return request status.
     */
    @GetMapping("")
    public ResponseEntity<?> getAllReports(@RequestHeader(name = AUTHORIZATION) String bearerToken) {
        try {
            return ResponseEntity.ok(reportService.getAllReports(bearerToken));
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getMessage(), e.getStatus());
        }
    }

    /**
     * Post request for adding a new report.
     *
     * @param report new report.
     * @param bearerToken jwt token.
     * @return request status.
     */
    @PostMapping("")
    public ResponseEntity<?> addReport(@RequestBody Report report,
                                       @RequestHeader(name = AUTHORIZATION) String bearerToken) {
        try {
            reportService.addReport(report, bearerToken);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getMessage(), e.getStatus());
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Delete request for removing a report.
     *
     * @param reportId id of report.
     * @param bearerToken jwt token.
     * @return request status.
     */
    @DeleteMapping("{reportId}")
    public ResponseEntity<?> deleteReport(@PathVariable String reportId,
                                          @RequestHeader(name = AUTHORIZATION) String bearerToken) {
        try {
            reportService.remove(reportId, bearerToken);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getMessage(), e.getStatus());
        }
        return ResponseEntity.ok().build();
    }
}
