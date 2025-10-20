package org.isoft.panelsurvey.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Step2DTO {
    @NotNull(message = "项目ID不能为空")
    private Long projectId;
    
    @NotBlank(message = "疾病名称不能为空")
    private String diseaseName;
    
    @NotBlank(message = "干预措施名称不能为空")
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
}

