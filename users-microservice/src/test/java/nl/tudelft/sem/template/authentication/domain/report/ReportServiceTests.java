package nl.tudelft.sem.template.authentication.domain.report;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import nl.tudelft.sem.template.authentication.authentication.JwtService;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class ReportServiceTests {

    @Mock
    private ReportRepository reportRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtService jwtService;
    @InjectMocks
    private ReportService reportService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void getAllReportsGoodTest() {
        when(jwtService.extractAuthorization(anyString())).thenReturn(Authority.ADMIN);
        when(reportRepository.findAll()).thenReturn(Collections.emptyList());
        assertDoesNotThrow(() -> reportService.getAllReports("Admin token"));
        verify(reportRepository, times(1)).findAll();
    }

    @Test
    public void getAllReportsNotAdminTest() {
        when(jwtService.extractAuthorization(anyString())).thenReturn(Authority.REGULAR_USER);
        assertThrows(ResponseStatusException.class, () -> reportService.getAllReports("User token"));
    }

    @Test
    public void getAllReportsExpiredTest() {
        when(jwtService.isTokenExpired(anyString())).thenReturn(true);
        when(jwtService.extractAuthorization(anyString())).thenReturn(Authority.REGULAR_USER);
        assertThrows(ResponseStatusException.class, () -> reportService.getAllReports("Expired token"));
    }

    @Test
    void addReportGoodTest() {
        when(jwtService.isTokenExpired(anyString())).thenReturn(false);
        when(userRepository.existsById(any())).thenReturn(true);
        Report report = new Report(ReportType.REVIEW, UUID.randomUUID().toString(), "Some language");
        assertDoesNotThrow(() -> reportService.addReport(report, "User token"));
        verify(reportRepository, times(1)).saveAndFlush(any());
    }

    @Test
    void addReportExpiredTest() {
        when(jwtService.isTokenExpired(anyString())).thenReturn(true);
        Report report = new Report(ReportType.REVIEW, UUID.randomUUID().toString(), "Some language");
        assertThrows(ResponseStatusException.class, () -> reportService.addReport(report, "Expired token"));
    }

    @Test
    void removeGoodTest() {
        when(jwtService.extractAuthorization(anyString())).thenReturn(Authority.ADMIN);
        UUID reportId = UUID.randomUUID();
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(new Report()));
        assertDoesNotThrow(() -> reportService.remove(reportId.toString(), "Admin token"));
        verify(reportRepository, times(1)).deleteById(reportId);
    }

    @Test
    void removeNotFoundTest() {
        when(jwtService.extractAuthorization(anyString())).thenReturn(Authority.ADMIN);
        UUID reportId = UUID.randomUUID();
        when(reportRepository.findById(reportId)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> reportService.remove(reportId.toString(), "Admin token"));
        assert (exception.getStatus() == HttpStatus.NOT_FOUND);
    }

    @Test
    void removeNotAdminTest() {
        when(jwtService.extractAuthorization(anyString())).thenReturn(Authority.REGULAR_USER);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> reportService.remove(UUID.randomUUID().toString(), "User token"));
        assert (exception.getStatus() == HttpStatus.UNAUTHORIZED);
    }

}
