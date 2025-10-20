package org.isoft.panelsurvey.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyResponseDTO {
    @NotNull(message = "问卷ID不能为空")
    private Long questionnaireId;
    
    private String respondentCode;
    private String deviceType;
    private Integer durationSeconds;
    
    @NotNull(message = "答案不能为空")
    private Map<Long, Object> answers;
}

