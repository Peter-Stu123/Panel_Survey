package org.isoft.panelsurvey.service.impl;

import com.alibaba.fastjson2.JSON;
import org.isoft.panelsurvey.dto.Step1DTO;
import org.isoft.panelsurvey.dto.Step2DTO;
import org.isoft.panelsurvey.entity.SurveyProject;
import org.isoft.panelsurvey.mapper.SurveyProjectMapper;
import org.isoft.panelsurvey.service.SurveyProjectService;
import org.isoft.panelsurvey.vo.SurveyProjectVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SurveyProjectServiceImpl implements SurveyProjectService {

    @Autowired
    private SurveyProjectMapper surveyProjectMapper;

    @Autowired
    private org.isoft.panelsurvey.mapper.SurveyQuestionnaireMapper questionnaireMapper;

    @Autowired
    private org.isoft.panelsurvey.service.WenjuanIntegrationService wenjuanIntegrationService;

    @Override
    public SurveyProjectVO createProject(Step1DTO step1DTO, Long userId) {
        SurveyProject project = new SurveyProject();
        project.setUserId(userId);
        project.setProjectName(step1DTO.getProjectName());
        project.setObjectiveType(step1DTO.getObjectiveType());
        project.setStepStatus(1);
        project.setStatus("DRAFT");

        surveyProjectMapper.insert(project);
        return convertToVO(project);
    }

    @Override
    public SurveyProjectVO updateProjectStep2(Step2DTO step2DTO) {
        SurveyProject project = surveyProjectMapper.selectById(step2DTO.getProjectId());
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }

        project.setDiseaseName(step2DTO.getDiseaseName());
        project.setInterventionName(step2DTO.getInterventionName());
        project.setComparisonName(step2DTO.getComparisonName());
        project.setPatientDescription(step2DTO.getPatientDescription());
        
        if (step2DTO.getOutcomeList() != null) {
            project.setOutcomeList(JSON.toJSONString(step2DTO.getOutcomeList()));
        }
        
        project.setInterventionDetails(step2DTO.getInterventionDetails());
        project.setComparisonDetails(step2DTO.getComparisonDetails());
        project.setSideEffects(step2DTO.getSideEffects());
        project.setAdditionalInfo(step2DTO.getAdditionalInfo());
        project.setSurveyDuration(step2DTO.getSurveyDuration());
        project.setTargetRespondents(step2DTO.getTargetRespondents());
        project.setStepStatus(2);

        surveyProjectMapper.update(project);
        return convertToVO(project);
    }

    @Override
    public SurveyProjectVO getProjectById(Long id) {
        SurveyProject project = surveyProjectMapper.selectById(id);
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }
        return convertToVO(project);
    }

    @Override
    public List<SurveyProjectVO> getUserProjects(Long userId) {
        List<SurveyProject> projects = surveyProjectMapper.selectByUserId(userId);
        return projects.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteProject(Long id) {
        // 先查询项目关联的问卷
        org.isoft.panelsurvey.entity.SurveyQuestionnaire questionnaire = 
            questionnaireMapper.selectByProjectId(id);
        
        // 如果问卷已发布到问卷网，先删除问卷网上的项目
        if (questionnaire != null && questionnaire.getWenjuanShortId() != null && 
            !questionnaire.getWenjuanShortId().isEmpty()) {
            try {
                // -1 表示永久删除
                boolean deleted = wenjuanIntegrationService.deleteWenjuanProject(
                    questionnaire.getWenjuanShortId(), -1);
                if (deleted) {
                    System.out.println("已从问卷网删除项目: " + questionnaire.getWenjuanShortId());
                } else {
                    System.err.println("从问卷网删除项目失败: " + questionnaire.getWenjuanShortId());
                }
            } catch (Exception e) {
                System.err.println("删除问卷网项目出错: " + e.getMessage());
                // 即使删除失败也继续删除本地数据
            }
        }
        
        // 删除本地项目（会级联删除关联的问卷、题目等）
        surveyProjectMapper.deleteById(id);
    }

    @Override
    public void updateStepStatus(Long id, Integer stepStatus) {
        surveyProjectMapper.updateStepStatus(id, stepStatus);
    }

    @Override
    public void updateStatus(Long id, String status) {
        surveyProjectMapper.updateStatus(id, status);
    }

    private SurveyProjectVO convertToVO(SurveyProject project) {
        SurveyProjectVO vo = new SurveyProjectVO();
        BeanUtils.copyProperties(project, vo);
        
        if (project.getOutcomeList() != null) {
            vo.setOutcomeList(JSON.parseArray(project.getOutcomeList(), String.class));
        }
        
        return vo;
    }
}

