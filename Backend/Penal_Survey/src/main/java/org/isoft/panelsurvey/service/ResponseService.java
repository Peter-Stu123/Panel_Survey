package org.isoft.panelsurvey.service;

import org.isoft.panelsurvey.dto.SurveyResponseDTO;

public interface ResponseService {
    
    void submitResponse(SurveyResponseDTO responseDTO, String clientIp);
    
    int getResponseCount(Long questionnaireId);
    
    int getCompletedCount(Long questionnaireId);
}

