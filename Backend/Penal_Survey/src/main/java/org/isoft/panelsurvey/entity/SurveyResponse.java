package org.isoft.panelsurvey.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyResponse {
    private Long id;
    private Long questionnaireId;
    private String respondentCode;
    private String respondentIp;
    private LocalDateTime startTime;
    private LocalDateTime submitTime;
    private Integer durationSeconds;
    private Integer isCompleted;
    private String deviceType;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

