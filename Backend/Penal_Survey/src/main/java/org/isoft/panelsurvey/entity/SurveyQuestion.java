package org.isoft.panelsurvey.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyQuestion {
    private Long id;
    private Long questionnaireId;
    private String section;
    private Integer questionNo;
    private String questionType;
    private String questionText;
    private String questionDescription;
    private String options;
    private Integer isRequired;
    private String logicJump;
    private String originalText;
    private String refinedText;
    private Integer isRefined;
    private Integer displayOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

