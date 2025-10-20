package org.isoft.panelsurvey.service.impl;

import com.alibaba.fastjson2.JSON;
import org.isoft.panelsurvey.dto.SurveyResponseDTO;
import org.isoft.panelsurvey.entity.SurveyAnswer;
import org.isoft.panelsurvey.entity.SurveyResponse;
import org.isoft.panelsurvey.mapper.SurveyAnswerMapper;
import org.isoft.panelsurvey.mapper.SurveyResponseMapper;
import org.isoft.panelsurvey.service.ResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ResponseServiceImpl implements ResponseService {

    @Autowired
    private SurveyResponseMapper responseMapper;

    @Autowired
    private SurveyAnswerMapper answerMapper;

    @Override
    @Transactional
    public void submitResponse(SurveyResponseDTO responseDTO, String clientIp) {
        SurveyResponse response = new SurveyResponse();
        response.setQuestionnaireId(responseDTO.getQuestionnaireId());
        response.setRespondentCode(generateRespondentCode());
        response.setRespondentIp(clientIp);
        response.setStartTime(LocalDateTime.now().minusSeconds(responseDTO.getDurationSeconds() != null ? responseDTO.getDurationSeconds() : 0));
        response.setSubmitTime(LocalDateTime.now());
        response.setDurationSeconds(responseDTO.getDurationSeconds());
        response.setIsCompleted(1);
        response.setDeviceType(responseDTO.getDeviceType());

        responseMapper.insert(response);

        List<SurveyAnswer> answers = new ArrayList<>();
        for (Map.Entry<Long, Object> entry : responseDTO.getAnswers().entrySet()) {
            SurveyAnswer answer = new SurveyAnswer();
            answer.setResponseId(response.getId());
            answer.setQuestionId(entry.getKey());
            answer.setAnswerValue(JSON.toJSONString(entry.getValue()));
            answer.setAnswerText(entry.getValue().toString());
            answers.add(answer);
        }

        if (!answers.isEmpty()) {
            answerMapper.insertBatch(answers);
        }
    }

    @Override
    public int getResponseCount(Long questionnaireId) {
        return responseMapper.countByQuestionnaireId(questionnaireId);
    }

    @Override
    public int getCompletedCount(Long questionnaireId) {
        return responseMapper.countCompletedByQuestionnaireId(questionnaireId);
    }

    private String generateRespondentCode() {
        return "R" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
}

