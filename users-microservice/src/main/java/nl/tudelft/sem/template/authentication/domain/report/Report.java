package nl.tudelft.sem.template.authentication.domain.report;

import java.util.Objects;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "reports")
@NoArgsConstructor
@ToString
public class Report {

    @Id
    @Getter
    @Setter
    @Column(name = "report_id", nullable = false)
    @Type(type = "uuid-char")
    private UUID id;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ReportType type;

    @Getter
    @Setter
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Getter
    @Column(name = "text", nullable = false)
    private String text;

    /**
     * Creates a new Report.
     *
     * @param id     id of the report as received from the Review microservice
     * @param type   report type.
     * @param userId id of the targeted user.
     * @param text   text of the report.
     */
    public Report(UUID id, ReportType type, String userId, String text) {
        this.id = id;
        this.type = type;
        this.userId = userId;
        this.text = text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Report that = (Report) o;
        return this.id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
