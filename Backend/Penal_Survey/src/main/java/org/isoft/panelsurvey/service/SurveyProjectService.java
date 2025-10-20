package org.isoft.panelsurvey.service;

import org.isoft.panelsurvey.dto.Step1DTO;
import org.isoft.panelsurvey.dto.Step2DTO;
import org.isoft.panelsurvey.vo.SurveyProjectVO;

import java.util.List;

public interface SurveyProjectService {
    
    SurveyProjectVO createProject(Step1DTO step1DTO, Long userId);
    
    SurveyProjectVO updateProjectStep2(Step2DTO step2DTO);
    
    SurveyProjectVO getProjectById(Long id);
    
    List<SurveyProjectVO> getUserProjects(Long userId);
    
    void deleteProject(Long id);
    
    void updateStepStatus(Long id, Integer stepStatus);
    
    void updateStatus(Long id, String status);
}

