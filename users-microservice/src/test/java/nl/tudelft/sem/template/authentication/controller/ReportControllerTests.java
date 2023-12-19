package nl.tudelft.sem.template.authentication.controller;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import nl.tudelft.sem.template.authentication.controllers.ReportController;
import nl.tudelft.sem.template.authentication.domain.report.Report;
import nl.tudelft.sem.template.authentication.domain.report.ReportService;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.domain.user.Username;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class ReportControllerTests {
    private transient ReportService reportService;
    private transient ReportController reportController;
    private transient List<Report> allReports;
    private transient Username username;
    private transient String authority;

    /**
     * Sets up the testing environment.
     */
    @BeforeEach
    public void setUp() {
        this.reportService = mock(ReportService.class);
        this.reportController = new ReportController(reportService);

        Authentication authenticationMock = mock(Authentication.class);
        when(authenticationMock.getName()).thenReturn("user");
        doReturn(List.of(Authority.REGULAR_USER)).when(authenticationMock).getAuthorities();
        SecurityContext securityContextMock = mock(SecurityContext.class);
        when(securityContextMock.getAuthentication()).thenReturn(authenticationMock);
        SecurityContextHolder.setContext(securityContextMock);

        Report r1 = new Report();
        Report r2 = new Report();
        allReports = new ArrayList<>(List.of(r1, r2));
        when(reportService.getAllReports(Authority.REGULAR_USER.toString())).thenReturn(allReports);

        this.username = new Username("user");
        this.authority = Authority.REGULAR_USER.getAuthority();
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testGetAll() {
        assertThat(reportController.getAllReports()).isEqualTo(ResponseEntity.ok(allReports));
        verify(reportService, times(1)).getAllReports(authority);
    }

    @Test
    public void testAdd() {
        Report added = new Report();
        assertThat(reportController.addReport(added)).isEqualTo(ResponseEntity.ok().build());
        verify(reportService, times(1)).addReport(added, username);
    }

    @Test
    public void testDelete() {
        UUID reportId = UUID.randomUUID();
        assertThat(reportController.deleteReport(reportId.toString())).isEqualTo(ResponseEntity.ok().build());
        verify(reportService, times(1)).remove(reportId.toString(), authority);
    }
}
