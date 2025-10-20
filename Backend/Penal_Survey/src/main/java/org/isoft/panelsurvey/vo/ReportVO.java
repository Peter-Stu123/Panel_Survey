package org.isoft.panelsurvey.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportVO {
    private Long id;
    private Long projectId;
    private Long questionnaireId;
    private String reportTitle;
    private Integer totalResponses;
    private Integer validResponses;
    private BigDecimal completionRate;
    private Integer averageDuration;
    private Map<String, Object> statisticalData;
    private Map<String, Object> chartData;
    private String preferenceSummary;
    private String keyFindings;
    private String reportContent;
    private String pdfPath;
    private String excelPath;
    private LocalDateTime generateTime;
}

