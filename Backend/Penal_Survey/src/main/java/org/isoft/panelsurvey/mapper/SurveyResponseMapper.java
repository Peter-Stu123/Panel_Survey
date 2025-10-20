package org.isoft.panelsurvey.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.isoft.panelsurvey.entity.SurveyResponse;

import java.util.List;

@Mapper
public interface SurveyResponseMapper {
    
    SurveyResponse selectById(@Param("id") Long id);
    
    List<SurveyResponse> selectByQuestionnaireId(@Param("questionnaireId") Long questionnaireId);
    
    int countByQuestionnaireId(@Param("questionnaireId") Long questionnaireId);
    
    int countCompletedByQuestionnaireId(@Param("questionnaireId") Long questionnaireId);
    
    int insert(SurveyResponse response);
    
    int update(SurveyResponse response);
    
    int deleteById(@Param("id") Long id);
}

