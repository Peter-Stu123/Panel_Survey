package org.isoft.panelsurvey.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.isoft.panelsurvey.entity.SurveyQuestionnaire;

import java.util.List;

@Mapper
public interface SurveyQuestionnaireMapper {
    
    SurveyQuestionnaire selectById(@Param("id") Long id);
    
    SurveyQuestionnaire selectByProjectId(@Param("projectId") Long projectId);
    
    List<SurveyQuestionnaire> selectAll();
    
    int insert(SurveyQuestionnaire questionnaire);
    
    int update(SurveyQuestionnaire questionnaire);
    
    int updateById(SurveyQuestionnaire questionnaire);
    
    int deleteById(@Param("id") Long id);
    
    int updateAiReviewStatus(@Param("id") Long id, @Param("isAiReviewed") Integer isAiReviewed);
    
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}

