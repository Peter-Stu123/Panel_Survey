package org.isoft.panelsurvey.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.isoft.panelsurvey.dto.OptionDTO;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionVO {
    private Long id;
    private String section;
    private Integer questionNo;
    private String questionType;
    private String questionText;
    private String questionDescription;
    private List<OptionDTO> options;
    private Boolean isRequired;
    private String originalText;
    private String refinedText;
    private Boolean isRefined;
    private Integer displayOrder;
}

