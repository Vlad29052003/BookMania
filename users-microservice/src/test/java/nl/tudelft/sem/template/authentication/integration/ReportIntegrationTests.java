package nl.tudelft.sem.template.authentication.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import nl.tudelft.sem.template.authentication.authentication.JwtTokenGenerator;
import nl.tudelft.sem.template.authentication.domain.report.Report;
import nl.tudelft.sem.template.authentication.domain.report.ReportRepository;
import nl.tudelft.sem.template.authentication.domain.report.ReportType;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.domain.user.HashedPassword;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import nl.tudelft.sem.template.authentication.domain.user.Username;
import nl.tudelft.sem.template.authentication.integration.utils.JsonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class ReportIntegrationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private transient JwtTokenGenerator jwtTokenGenerator;

    @Autowired
    private transient ReportRepository reportRepository;

    @Autowired
    private transient UserRepository userRepository;

    private AppUser madeReport;
    private AppUser isReported;
    private AppUser admin;
    private String userToken;
    private String adminToken;

    /**
     * Set up the testing environment.
     */
    @BeforeEach
    public void setUp() {
        madeReport = new AppUser(new Username("user"), "user@regular.com", new HashedPassword("pass"));
        madeReport.setAuthority(Authority.REGULAR_USER);
        Collection<SimpleGrantedAuthority> rolesUser = new ArrayList<>();
        rolesUser.add(new SimpleGrantedAuthority(Authority.REGULAR_USER.toString()));
        userToken = jwtTokenGenerator.generateToken(new User(madeReport.getUsername().toString(),
                madeReport.getPassword().toString(), rolesUser));

        isReported = new AppUser(new Username("reported"), "oopsie@report.com", new HashedPassword("????"));
        isReported.setAuthority(Authority.REGULAR_USER);

        admin = new AppUser(new Username("admin"), "admin@admin.com", new HashedPassword("pwd"));
        admin.setAuthority(Authority.ADMIN);
        Collection<SimpleGrantedAuthority> rolesAdmin = new ArrayList<>();
        rolesAdmin.add(new SimpleGrantedAuthority(Authority.ADMIN.toString()));
        adminToken = jwtTokenGenerator.generateToken(new User(admin.getUsername().toString(),
                admin.getPassword().toString(), rolesAdmin));
    }

    @Test
    public void testGetAllReports() throws Exception {
        userRepository.save(madeReport);
        userRepository.save(admin);

        mockMvc.perform(get("/c/reports")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/c/reports")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testAddReport() throws Exception {
        Report report = new Report(UUID.randomUUID(), ReportType.REVIEW, UUID.randomUUID().toString(), "text");
        userRepository.save(madeReport);
        mockMvc.perform(post("/c/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.serialize(report))
                        .header("Authorization", "Bearer " + userToken))
                        .andExpect(status().isNotFound());

        userRepository.save(isReported);
        report.setUserId(isReported.getId().toString());
        mockMvc.perform(post("/c/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.serialize(report))
                        .header("Authorization", "Bearer " + userToken))
                        .andExpect(status().isOk());
    }

    @Test
    public void testRemove() throws Exception {
        userRepository.save(admin);
        userRepository.save(madeReport);
        userRepository.save(isReported);
        Report report = new Report(UUID.randomUUID(), ReportType.REVIEW, UUID.randomUUID().toString(), "text");
        mockMvc.perform(delete("/c/reports/" + report.getId())
                        .header("Authorization", "Bearer " + userToken))
                        .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/c/reports/" + report.getId())
                        .header("Authorization", "Bearer " + adminToken))
                        .andExpect(status().isNotFound());

        report.setUserId(isReported.getId().toString());
        reportRepository.save(report);
        mockMvc.perform(delete("/c/reports/" + report.getId())
                        .header("Authorization", "Bearer " + adminToken))
                        .andExpect(status().isOk());
        assertThat(reportRepository.findAll()).isEmpty();
    }
}