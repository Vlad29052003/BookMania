package nl.tudelft.sem.template.authentication.domain.report;

import java.util.List;
import java.util.UUID;
import nl.tudelft.sem.template.authentication.authentication.JwtService;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import nl.tudelft.sem.template.authentication.models.CreateReportModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReportService {

    private final transient ReportRepository reportRepository;
    private final transient UserRepository userRepository;
    private final transient JwtService jwtService;

    /**
     * Create a ReportService.
     *
     * @param reportRepository report repository.
     * @param userRepository user repository.
     * @param jwtService jwt service.
     */
    @Autowired
    public ReportService(ReportRepository reportRepository, UserRepository userRepository, JwtService jwtService) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    /**
     * Retrieves all reports from the database.
     *
     * @param bearerToken jwt token.
     * @return a list of all reports in the database.
     */
    public List<Report> getAllReports(String bearerToken) {
        if (!getAuthority(bearerToken).equals(Authority.ADMIN)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Only admins can access reports!");
        }
        return reportRepository.findAll();
    }

    /**
     * Adds a new report to the database.
     *
     * @param reportModel report to be added.
     * @param bearerToken jwt token.
     */
    public void addReport(CreateReportModel reportModel, String bearerToken) {
        if (!userRepository.existsById(UUID.fromString(reportModel.getUserId()))) {
            throw new ResponseStatusException((HttpStatus.NOT_FOUND), "Reported user not found!");
        }
        Report report = new Report(reportModel.getReportType(),
                                    reportModel.getUserId(),
                                    reportModel.getText());
        reportRepository.saveAndFlush(report);
    }

    /**
     * Removes a report from the database.
     *
     * @param id id of the report.
     * @param bearerToken jwt token.
     */
    public void remove(String id, String bearerToken) {
        if (!getAuthority(bearerToken).equals(Authority.ADMIN)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Only admins can access reports!");
        }
        var optReport = reportRepository.findById(UUID.fromString(id));
        if (optReport.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The report does not exist!");
        }
        reportRepository.deleteById(UUID.fromString(id));
    }

    private Authority getAuthority(String bearerToken) {
        return jwtService.extractAuthorization(bearerToken.substring(7));
    }
}
