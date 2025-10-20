package org.isoft.panelsurvey.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyReport {
    private Long id;
    private Long projectId;
    private Long questionnaireId;
    private String reportTitle;
    private Integer totalResponses;
    private Integer validResponses;
    private BigDecimal completionRate;
    private Integer averageDuration;
    private String statisticalData;
    private String chartData;
    private String preferenceSummary;
    private String keyFindings;
    private String reportContent;
    private String pdfPath;
    private String excelPath;
    private LocalDateTime generateTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

