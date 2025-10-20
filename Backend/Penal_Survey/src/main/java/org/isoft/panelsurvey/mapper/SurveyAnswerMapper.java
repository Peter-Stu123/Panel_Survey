package org.isoft.panelsurvey.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.isoft.panelsurvey.entity.SurveyAnswer;

import java.util.List;

@Mapper
public interface SurveyAnswerMapper {
    
    SurveyAnswer selectById(@Param("id") Long id);
    
    List<SurveyAnswer> selectByResponseId(@Param("responseId") Long responseId);
    
    List<SurveyAnswer> selectByQuestionId(@Param("questionId") Long questionId);
    
    int insert(SurveyAnswer answer);
    
    int insertBatch(List<SurveyAnswer> answers);
    
    int deleteById(@Param("id") Long id);
    
    int deleteByResponseId(@Param("responseId") Long responseId);
}

