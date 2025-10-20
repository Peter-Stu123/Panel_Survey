package org.isoft.panelsurvey.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.isoft.panelsurvey.entity.SurveyProject;

import java.util.List;

@Mapper
public interface SurveyProjectMapper {
    
    SurveyProject selectById(@Param("id") Long id);
    
    List<SurveyProject> selectByUserId(@Param("userId") Long userId);
    
    List<SurveyProject> selectAll();
    
    int insert(SurveyProject project);
    
    int update(SurveyProject project);
    
    int deleteById(@Param("id") Long id);
    
    int updateStepStatus(@Param("id") Long id, @Param("stepStatus") Integer stepStatus);
    
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}

