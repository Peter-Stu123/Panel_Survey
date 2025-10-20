package org.isoft.panelsurvey.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyAnswer {
    private Long id;
    private Long responseId;
    private Long questionId;
    private String answerValue;
    private String answerText;
    private LocalDateTime createTime;
}

