package org.isoft.panelsurvey.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.isoft.panelsurvey.entity.SurveyReport;

import java.util.List;

@Mapper
public interface SurveyReportMapper {
    
    SurveyReport selectById(@Param("id") Long id);
    
    SurveyReport selectByQuestionnaireId(@Param("questionnaireId") Long questionnaireId);
    
    List<SurveyReport> selectByProjectId(@Param("projectId") Long projectId);
    
    int insert(SurveyReport report);
    
    int update(SurveyReport report);
    
    int deleteById(@Param("id") Long id);
}

