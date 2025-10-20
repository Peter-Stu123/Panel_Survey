package org.isoft.panelsurvey.service;

import org.isoft.panelsurvey.dto.PolishedBackgroundDTO;
import org.isoft.panelsurvey.vo.QuestionnaireVO;

public interface QuestionnaireService {

    QuestionnaireVO generateQuestionnaire(Long projectId);

    QuestionnaireVO reviewByAI(Long questionnaireId);

    QuestionnaireVO getQuestionnaireById(Long id);

    QuestionnaireVO getByProjectId(Long projectId);

    void updateQuestion(Long questionId, String questionText);

    void publishQuestionnaire(Long questionnaireId, String surveyUrl);

    PolishedBackgroundDTO polishReportBackground(Long projectId);

    /**
     * 发布问卷到问卷网
     * 
     * @param questionnaireId 问卷ID
     * @return 问卷网创建结果(包含公网访问链接)
     */
    org.isoft.panelsurvey.dto.WenjuanResponseDTO.WenjuanCreateResult publishToWenjuan(Long questionnaireId);

    /**
     * 从问卷网同步答卷数据
     * 
     * @param questionnaireId 问卷ID
     * @return 同步后的答卷数量
     */
    int syncWenjuanData(Long questionnaireId);

    /**
     * 导出问卷内容为格式化文本
     * 
     * @param questionnaireId 问卷ID
     * @return 格式化的问卷内容文本
     */
    String exportFormattedContent(Long questionnaireId);
}
