package nl.tudelft.sem.template.authentication.controllers;

import java.util.ArrayList;
import nl.tudelft.sem.template.authentication.domain.report.Report;
import nl.tudelft.sem.template.authentication.domain.report.ReportService;
import nl.tudelft.sem.template.authentication.domain.user.Username;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
     * @return request status.
     */
    @GetMapping("")
    public ResponseEntity<?> getAllReports() {
        String authority = new ArrayList<>(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .get(0).getAuthority();
        try {
            return ResponseEntity.ok(reportService.getAllReports(authority));
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getMessage(), e.getStatus());
        }
    }

    /**
     * Post request for adding a new report.
     *
     * @param report new report.
     * @return request status.
     */
    @PostMapping("")
    public ResponseEntity<?> addReport(@RequestBody Report report) {
        Username madeRequest = new Username(SecurityContextHolder.getContext().getAuthentication().getName());
        try {
            reportService.addReport(report, madeRequest);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getMessage(), e.getStatus());
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Delete request for removing a report.
     *
     * @param reportId id of report.
     * @return request status.
     */
    @DeleteMapping("{reportId}")
    public ResponseEntity<?> deleteReport(@PathVariable String reportId) {
        String authority = new ArrayList<>(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .get(0).getAuthority();
        try {
            reportService.remove(reportId, authority);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getMessage(), e.getStatus());
        }
        return ResponseEntity.ok().build();
    }
}
