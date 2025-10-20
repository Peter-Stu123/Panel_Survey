package org.isoft.panelsurvey.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Step1DTO {
    private Long projectId;
    
    @NotBlank(message = "项目名称不能为空")
    private String projectName;
    
    @NotBlank(message = "问卷目标类型不能为空")
    private String objectiveType;
}

