package nl.tudelft.sem.template.authentication.domain.report;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import nl.tudelft.sem.template.authentication.domain.user.Username;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReportService {

    private final transient ReportRepository reportRepository;
    private final transient UserRepository userRepository;

    /**
     * Create a ReportService.
     *
     * @param reportRepository report repository.
     * @param userRepository user repository.
     */
    @Autowired
    public ReportService(ReportRepository reportRepository, UserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
    }

    /**
     * Retrieves all reports from the database.
     *
     * @param authority user authority.
     * @return a list of all reports in the database.
     */
    public List<Report> getAllReports(String authority) {
        if (!authority.equals(Authority.ADMIN.toString())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Only admins can access reports!");
        }
        return reportRepository.findAll();
    }

    /**
     * Adds a new report to the database.
     *
     * @param report report to be added.
     * @param madeRequest username that made the report.
     */
    public void addReport(Report report, Username madeRequest) {
        if (!userRepository.existsByUsername(madeRequest)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Report by non-existent user!");
        }
        if (!userRepository.existsById(UUID.fromString(report.getUserId()))) {
            throw new ResponseStatusException((HttpStatus.NOT_FOUND), "Reported user not found!");
        }
        reportRepository.saveAndFlush(report);
    }

    /**
     * Removes a report from the database.
     *
     * @param id id of the report.
     * @param authority authority of user that made the request.
     */
    public void remove(String id, String authority) {
        if (!authority.equals(Authority.ADMIN.toString())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Only admins can access reports!");
        }
        Optional<Report> optReport = reportRepository.findById(UUID.fromString(id));
        if (optReport.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The report does not exist!");
        }
        reportRepository.deleteById(UUID.fromString(id));
    }
}
