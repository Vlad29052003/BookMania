package nl.tudelft.sem.template.authentication.domain.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.HashedPassword;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import nl.tudelft.sem.template.authentication.domain.user.Username;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReportServiceTests {

    @Autowired
    private transient ReportService reportService;

    @Autowired
    private transient ReportRepository reportRepository;

    @Autowired
    private transient UserRepository userRepository;

    private AppUser madeReport;
    private AppUser isReported;
    private Report report;

    @BeforeEach
    public void setUp() {
        madeReport = new AppUser(new Username("madeReport"), "made@report.com", new HashedPassword("pass"));
        isReported = new AppUser(new Username("reported"), "is@reported.com", new HashedPassword("pwd"));
    }

    @Test
    public void getAllReportsNotAdminTest() {
        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> reportService.getAllReports("REGULAR_USER"));
        assertEquals(e.getStatus(), HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void getAllReportsGoodTest() {
        assertDoesNotThrow(() -> reportService.getAllReports("ADMIN"));
        assertEquals(reportService.getAllReports("ADMIN"), new ArrayList<>());
        report = new Report(UUID.randomUUID(), ReportType.COMMENT, UUID.randomUUID().toString(), "text");
        reportRepository.save(report);
        assertEquals(reportService.getAllReports("ADMIN"), List.of(report));
    }

    @Test
    public void nonExistentUserMakesReportTest() {
        report = new Report(UUID.randomUUID(), ReportType.REVIEW, UUID.randomUUID().toString(), "text");
        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> reportService.addReport(report, madeReport.getUsername()));
        assertEquals(e.getStatus(), HttpStatus.NOT_FOUND);
    }

    @Test
    public void addReportToNonExistentUserTest() {
        userRepository.save(madeReport);
        report = new Report(UUID.randomUUID(), ReportType.COMMENT, UUID.randomUUID().toString(), "text");
        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> reportService.addReport(report, madeReport.getUsername()));
        assertEquals(e.getStatus(), HttpStatus.NOT_FOUND);
    }

    @Test
    public void addReportGoodTest() {
        userRepository.save(madeReport);
        userRepository.save(isReported);
        report = new Report(UUID.randomUUID(), ReportType.COMMENT, isReported.getId().toString(), "text");
        assertDoesNotThrow(() -> reportService.addReport(report, madeReport.getUsername()));
        assertThat(reportRepository.findAll()).isEqualTo(List.of(report));
    }

    @Test
    public void removeReportNotAdminTest() {
        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> reportService.remove(UUID.randomUUID().toString(), "REGULAR_USER"));
        assertEquals(e.getStatus(), HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void removeNonExistentReportTest() {
        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> reportService.remove(UUID.randomUUID().toString(), "ADMIN"));
        assertEquals(e.getStatus(), HttpStatus.NOT_FOUND);
    }

    @Test
    public void removeGoodTest() {
        report = new Report(UUID.randomUUID(), ReportType.REVIEW, UUID.randomUUID().toString(), "text");
        reportRepository.save(report);
        assertDoesNotThrow(() -> reportService.remove(report.getId().toString(), "ADMIN"));
        assertThat(reportRepository.findAll()).isEmpty();
    }
}
