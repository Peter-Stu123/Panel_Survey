package org.isoft.panelsurvey.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyProjectVO {
    private Long id;
    private Long userId;
    private String projectName;
    private String objectiveType;
    private String diseaseName;
    private String interventionName;
    private String comparisonName;
    private String patientDescription;
    private List<String> outcomeList;
    private String interventionDetails;
    private String comparisonDetails;
    private String sideEffects;
    private String additionalInfo;
    private Integer surveyDuration;
    private Integer targetRespondents;
    private Integer stepStatus;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

