package org.isoft.panelsurvey.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO {
    private Long id;
    private String section;
    private Integer questionNo;
    private String questionType;
    private String questionText;
    private String questionDescription;
    private List<OptionDTO> options;
    private Boolean isRequired;
    private Integer displayOrder;
}

