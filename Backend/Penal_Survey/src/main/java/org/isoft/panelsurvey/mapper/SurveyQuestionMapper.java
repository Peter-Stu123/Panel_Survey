package org.isoft.panelsurvey.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.isoft.panelsurvey.entity.SurveyQuestion;

import java.util.List;

@Mapper
public interface SurveyQuestionMapper {
    
    SurveyQuestion selectById(@Param("id") Long id);
    
    List<SurveyQuestion> selectByQuestionnaireId(@Param("questionnaireId") Long questionnaireId);
    
    int insert(SurveyQuestion question);
    
    int insertBatch(List<SurveyQuestion> questions);
    
    int update(SurveyQuestion question);
    
    int deleteById(@Param("id") Long id);
    
    int deleteByQuestionnaireId(@Param("questionnaireId") Long questionnaireId);
    
    int updateRefinedText(@Param("id") Long id, @Param("refinedText") String refinedText, @Param("isRefined") Integer isRefined);
}

