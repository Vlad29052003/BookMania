package nl.tudelft.sem.template.authentication.models;

import lombok.Data;
import nl.tudelft.sem.template.authentication.domain.report.ReportType;

@Data
public class CreateReportModel {
    public ReportType reportType;
    public String userId;
    public String text;
}
