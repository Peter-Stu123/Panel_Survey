package org.isoft.panelsurvey.service.impl;

import com.alibaba.fastjson2.JSON;
import org.isoft.panelsurvey.dto.OptionDTO;
import org.isoft.panelsurvey.dto.PolishedBackgroundDTO;
import org.isoft.panelsurvey.entity.SurveyProject;
import org.isoft.panelsurvey.entity.SurveyQuestion;
import org.isoft.panelsurvey.entity.SurveyQuestionnaire;
import org.isoft.panelsurvey.mapper.SurveyProjectMapper;
import org.isoft.panelsurvey.mapper.SurveyQuestionMapper;
import org.isoft.panelsurvey.mapper.SurveyQuestionnaireMapper;
import org.isoft.panelsurvey.service.AIService;
import org.isoft.panelsurvey.service.QuestionnaireService;
import org.isoft.panelsurvey.service.WenjuanIntegrationService;
import org.isoft.panelsurvey.vo.QuestionVO;
import org.isoft.panelsurvey.vo.QuestionnaireVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class QuestionnaireServiceImpl implements QuestionnaireService {

    @Autowired
    private SurveyQuestionnaireMapper questionnaireMapper;

    @Autowired
    private SurveyQuestionMapper questionMapper;

    @Autowired
    private SurveyProjectMapper projectMapper;

    @Autowired
    private AIService aiService;

    @Autowired(required = false)
    private WenjuanIntegrationService wenjuanIntegrationService;

    @Override
    @Transactional
    public QuestionnaireVO generateQuestionnaire(Long projectId) {
        SurveyProject project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }

        // 检查是否已存在问卷，如果存在则删除旧的
        SurveyQuestionnaire existingQuestionnaire = questionnaireMapper.selectByProjectId(projectId);
        if (existingQuestionnaire != null) {
            System.out.println("检测到项目ID " + projectId + " 已有问卷，删除旧问卷ID: " + existingQuestionnaire.getId());
            // 删除旧的问题
            questionMapper.deleteByQuestionnaireId(existingQuestionnaire.getId());
            // 删除旧的问卷
            questionnaireMapper.deleteById(existingQuestionnaire.getId());
            System.out.println("旧问卷已删除，准备生成新问卷");
        }

        SurveyQuestionnaire questionnaire = new SurveyQuestionnaire();
        questionnaire.setProjectId(projectId);
        questionnaire.setTitle(project.getProjectName() + " - 患者偏好调查问卷");
        questionnaire.setDescription("本问卷旨在了解患者对" + project.getDiseaseName() + "治疗方案的价值观和偏好");

        // 生成引言
        String introduction = aiService.generateQuestionnaireIntroduction(
                project.getDiseaseName(),
                project.getInterventionName(),
                project.getObjectiveType());

        // 获取润色后的报告背景（表格数据）
        PolishedBackgroundDTO polishedBg = polishReportBackground(projectId);

        // 将引言和方案信息组合成结构化文本
        StringBuilder fullIntroduction = new StringBuilder();
        fullIntroduction.append(introduction).append("\n\n");
        fullIntroduction.append("【治疗方案说明】\n\n");
        fullIntroduction.append("在回答问题前，请先了解以下两种治疗方案的详细信息：\n\n");

        // 生成表格格式的文本（使用特殊标记）
        fullIntroduction.append("##TABLE_START##\n");
        fullIntroduction.append("对比项|").append(project.getInterventionName()).append("|")
                .append(project.getComparisonName() != null ? project.getComparisonName() : "对照方案").append("\n");

        System.out.println("========== 生成问卷表格数据 ==========");
        System.out.println("表格行数: " + polishedBg.getComparisonTable().size());

        for (PolishedBackgroundDTO.ComparisonRow row : polishedBg.getComparisonTable()) {
            String rowContent = row.getCategory() + "|" + row.getIntervention() + "|" + row.getComparison();
            System.out.println("表格行: " + rowContent);
            fullIntroduction.append(row.getCategory()).append("|")
                    .append(row.getIntervention()).append("|")
                    .append(row.getComparison()).append("\n");
        }
        fullIntroduction.append("##TABLE_END##\n");

        System.out.println("========== 完整引言内容 ==========");
        System.out.println(fullIntroduction.toString());
        System.out.println("========== 引言结束 ==========");

        // 添加副作用说明（如果有）
        if (polishedBg.getSideEffects() != null && !polishedBg.getSideEffects().trim().isEmpty()) {
            fullIntroduction.append("\n【可能的副作用】\n");
            fullIntroduction.append(polishedBg.getSideEffects());
        }

        // 添加其他重要信息（如果有）
        if (polishedBg.getAdditionalInfo() != null && !polishedBg.getAdditionalInfo().trim().isEmpty()) {
            fullIntroduction.append("\n\n【其他重要信息】\n");
            fullIntroduction.append(polishedBg.getAdditionalInfo());
        }

        questionnaire.setIntroduction(fullIntroduction.toString());
        questionnaire.setInstruction("请仔细阅读上述治疗方案信息后，根据您的真实想法填写问卷。所有信息将严格保密。");
        questionnaire.setVersion(1);
        questionnaire.setIsAiReviewed(0);
        questionnaire.setStatus("GENERATED");

        // 初始化问卷网字段为默认值
        questionnaire.setWenjuanRespondentCount(0);
        questionnaire.setIsWenjuanPublished(0);

        questionnaireMapper.insert(questionnaire);

        List<SurveyQuestion> questions = generateQuestions(questionnaire.getId(), project);
        questionMapper.insertBatch(questions);

        projectMapper.updateStepStatus(projectId, 3);
        projectMapper.updateStatus(projectId, "GENERATED");

        return getQuestionnaireById(questionnaire.getId());
    }

    @Override
    @Transactional
    public QuestionnaireVO reviewByAI(Long questionnaireId) {
        SurveyQuestionnaire questionnaire = questionnaireMapper.selectById(questionnaireId);
        if (questionnaire == null) {
            throw new RuntimeException("问卷不存在");
        }

        List<SurveyQuestion> questions = questionMapper.selectByQuestionnaireId(questionnaireId);

        for (SurveyQuestion question : questions) {
            String refinedText = aiService.reviewAndRefineQuestion(
                    question.getQuestionText(),
                    question.getQuestionType());

            if (!"无需修改".equals(refinedText)) {
                questionMapper.updateRefinedText(question.getId(), refinedText, 1);
            }
        }

        questionnaireMapper.updateAiReviewStatus(questionnaireId, 1);

        return getQuestionnaireById(questionnaireId);
    }

    @Override
    public QuestionnaireVO getQuestionnaireById(Long id) {
        SurveyQuestionnaire questionnaire = questionnaireMapper.selectById(id);
        if (questionnaire == null) {
            throw new RuntimeException("问卷不存在");
        }

        List<SurveyQuestion> questions = questionMapper.selectByQuestionnaireId(id);

        QuestionnaireVO vo = new QuestionnaireVO();
        BeanUtils.copyProperties(questionnaire, vo);
        vo.setIsAiReviewed(questionnaire.getIsAiReviewed() == 1);

        List<QuestionVO> questionVOs = questions.stream()
                .map(this::convertQuestionToVO)
                .collect(Collectors.toList());
        vo.setQuestions(questionVOs);

        return vo;
    }

    @Override
    public QuestionnaireVO getByProjectId(Long projectId) {
        SurveyQuestionnaire questionnaire = questionnaireMapper.selectByProjectId(projectId);
        if (questionnaire == null) {
            return null;
        }
        return getQuestionnaireById(questionnaire.getId());
    }

    @Override
    public void updateQuestion(Long questionId, String questionText) {
        SurveyQuestion question = questionMapper.selectById(questionId);
        if (question == null) {
            throw new RuntimeException("题目不存在");
        }
        question.setQuestionText(questionText);
        questionMapper.update(question);
    }

    @Override
    public void publishQuestionnaire(Long questionnaireId, String surveyUrl) {
        SurveyQuestionnaire questionnaire = questionnaireMapper.selectById(questionnaireId);
        if (questionnaire == null) {
            throw new RuntimeException("问卷不存在");
        }
        questionnaire.setSurveyUrl(surveyUrl);
        questionnaire.setStatus("PUBLISHED");
        questionnaireMapper.update(questionnaire);

        projectMapper.updateStepStatus(questionnaire.getProjectId(), 5);
        projectMapper.updateStatus(questionnaire.getProjectId(), "PUBLISHED");
    }

    @Override
    @Transactional
    public org.isoft.panelsurvey.dto.WenjuanResponseDTO.WenjuanCreateResult publishToWenjuan(Long questionnaireId) {
        if (wenjuanIntegrationService == null) {
            throw new RuntimeException("问卷网集成服务未配置，请检查application.properties中的wenjuan.api配置");
        }

        SurveyQuestionnaire questionnaire = questionnaireMapper.selectById(questionnaireId);
        if (questionnaire == null) {
            throw new RuntimeException("问卷不存在");
        }

        if ("PUBLISHED".equals(questionnaire.getStatus()) && questionnaire.getSurveyUrl() != null) {
            throw new RuntimeException("问卷已发布，不可重复发布");
        }

        // 调用问卷网API创建问卷
        org.isoft.panelsurvey.dto.WenjuanResponseDTO.WenjuanCreateResult result = wenjuanIntegrationService
                .createWenjuanSurvey(questionnaire);

        // 保存问卷网链接、二维码和问卷网信息
        questionnaire.setSurveyUrl(result.getSurveyUrl()); // 问卷网公网链接
        questionnaire.setQrCodePath(result.getQrcodeUrl());
        questionnaire.setWenjuanShortId(result.getSurveyId()); // 保存问卷网shortId
        questionnaire.setWenjuanRespondentCount(0); // 初始化答卷数为0
        questionnaire.setIsWenjuanPublished(1); // 标记已发布到问卷网
        questionnaire.setLastSyncTime(java.time.LocalDateTime.now()); // 记录同步时间
        questionnaire.setStatus("PUBLISHED");
        questionnaireMapper.update(questionnaire);

        // 更新项目状态
        projectMapper.updateStepStatus(questionnaire.getProjectId(), 5);
        projectMapper.updateStatus(questionnaire.getProjectId(), "PUBLISHED");

        return result;
    }

    @Override
    public PolishedBackgroundDTO polishReportBackground(Long projectId) {
        SurveyProject project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }

        PolishedBackgroundDTO dto = new PolishedBackgroundDTO();
        List<PolishedBackgroundDTO.ComparisonRow> table = new ArrayList<>();

        // 1. 疾病名称行
        table.add(new PolishedBackgroundDTO.ComparisonRow(
                "疾病名称",
                project.getDiseaseName(),
                project.getDiseaseName()));

        // 2. 患者描述行（AI润色为简洁版本）
        if (project.getPatientDescription() != null && !project.getPatientDescription().trim().isEmpty()) {
            String polishedDesc = aiService.polishForTable(
                    "患者描述",
                    project.getPatientDescription());
            table.add(new PolishedBackgroundDTO.ComparisonRow(
                    "患者描述",
                    polishedDesc,
                    polishedDesc));
        }

        // 3. 治疗方案详情（AI润色为简洁版本，适合表格展示）
        String polishedIntervention = aiService.polishForTable(
                project.getInterventionName(),
                project.getInterventionDetails());

        String polishedComparison = aiService.polishForTable(
                project.getComparisonName() != null ? project.getComparisonName() : "标准治疗",
                project.getComparisonDetails());

        table.add(new PolishedBackgroundDTO.ComparisonRow(
                "治疗方案详情",
                polishedIntervention,
                polishedComparison));

        dto.setComparisonTable(table);

        // 副作用说明（AI润色，完整版本）
        if (project.getSideEffects() != null && !project.getSideEffects().trim().isEmpty()) {
            String polishedSideEffects = aiService.polishTreatmentInfo(
                    "可能的副作用",
                    project.getSideEffects());
            dto.setSideEffects(polishedSideEffects);
        }

        // 其他重要信息（AI润色，完整版本）
        if (project.getAdditionalInfo() != null && !project.getAdditionalInfo().trim().isEmpty()) {
            String polishedAdditionalInfo = aiService.polishTreatmentInfo(
                    "其他重要信息",
                    project.getAdditionalInfo());
            dto.setAdditionalInfo(polishedAdditionalInfo);
        }

        return dto;
    }

    private List<SurveyQuestion> generateQuestions(Long questionnaireId, SurveyProject project) {
        List<SurveyQuestion> questions = new ArrayList<>();
        int order = 1;

        questions.add(createBasicInfoQuestion(questionnaireId, order++, "性别", "SINGLE_CHOICE",
                List.of("男", "女", "其他")));
        questions.add(createBasicInfoQuestion(questionnaireId, order++, "年龄范围", "SINGLE_CHOICE",
                List.of("18-30岁", "31-45岁", "46-60岁", "60岁以上")));
        questions.add(createBasicInfoQuestion(questionnaireId, order++, "确诊时间", "SINGLE_CHOICE",
                List.of("1年以内", "1-3年", "3-5年", "5年以上")));

        questions.add(createInterventionQuestion(questionnaireId, order++, project));

        questions.add(createOutcomeQuestion(questionnaireId, order++, project));

        questions.add(createTextQuestion(questionnaireId, order++,
                "您对治疗方案还有哪些考虑因素或补充意见？", "TEXT"));

        return questions;
    }

    private SurveyQuestion createBasicInfoQuestion(Long questionnaireId, int order, String title, String type,
            List<String> optionLabels) {
        SurveyQuestion question = new SurveyQuestion();
        question.setQuestionnaireId(questionnaireId);
        question.setSection("基本信息");
        question.setQuestionNo(order);
        question.setQuestionType(type);
        question.setQuestionText(title);
        question.setIsRequired(1);
        question.setDisplayOrder(order);
        question.setIsRefined(0);

        List<OptionDTO> options = new ArrayList<>();
        for (int i = 0; i < optionLabels.size(); i++) {
            options.add(new OptionDTO(String.valueOf(i + 1), optionLabels.get(i), null));
        }
        question.setOptions(JSON.toJSONString(options));
        question.setOriginalText(title);

        return question;
    }

    private SurveyQuestion createInterventionQuestion(Long questionnaireId, int order, SurveyProject project) {
        SurveyQuestion question = new SurveyQuestion();
        question.setQuestionnaireId(questionnaireId);
        question.setSection("治疗偏好");
        question.setQuestionNo(order);
        question.setQuestionType("SINGLE_CHOICE");
        question.setQuestionText("如果您面临" + project.getDiseaseName() + "的治疗选择，您更倾向于接受哪种方案？");
        question.setQuestionDescription(
                "方案A：" + project.getInterventionName() + "\n" +
                        "方案B：" + (project.getComparisonName() != null ? project.getComparisonName() : "标准治疗"));
        question.setIsRequired(1);
        question.setDisplayOrder(order);
        question.setIsRefined(0);

        List<OptionDTO> options = List.of(
                new OptionDTO("1", "明确倾向方案A", null),
                new OptionDTO("2", "可能倾向方案A", null),
                new OptionDTO("3", "不确定", null),
                new OptionDTO("4", "可能倾向方案B", null),
                new OptionDTO("5", "明确倾向方案B", null));
        question.setOptions(JSON.toJSONString(options));
        question.setOriginalText(question.getQuestionText());

        return question;
    }

    private SurveyQuestion createOutcomeQuestion(Long questionnaireId, int order, SurveyProject project) {
        SurveyQuestion question = new SurveyQuestion();
        question.setQuestionnaireId(questionnaireId);
        question.setSection("结局重要性");
        question.setQuestionNo(order);
        question.setQuestionType("RATING");
        question.setQuestionText("请评价以下治疗结局对您的重要程度（1-5分，5分表示非常重要）");
        question.setIsRequired(1);
        question.setDisplayOrder(order);
        question.setIsRefined(0);

        List<String> outcomes = JSON.parseArray(project.getOutcomeList(), String.class);
        if (outcomes == null || outcomes.isEmpty()) {
            outcomes = List.of("疾病控制", "生活质量", "副作用", "治疗费用", "治疗便利性");
        }

        List<OptionDTO> options = new ArrayList<>();
        for (String outcome : outcomes) {
            options.add(new OptionDTO(outcome, outcome, "评分范围: 1-5分"));
        }
        question.setOptions(JSON.toJSONString(options));
        question.setOriginalText(question.getQuestionText());

        return question;
    }

    private SurveyQuestion createTextQuestion(Long questionnaireId, int order, String text, String type) {
        SurveyQuestion question = new SurveyQuestion();
        question.setQuestionnaireId(questionnaireId);
        question.setSection("补充意见");
        question.setQuestionNo(order);
        question.setQuestionType(type);
        question.setQuestionText(text);
        question.setIsRequired(0);
        question.setDisplayOrder(order);
        question.setIsRefined(0);
        question.setOriginalText(text);

        return question;
    }

    private QuestionVO convertQuestionToVO(SurveyQuestion question) {
        QuestionVO vo = new QuestionVO();
        BeanUtils.copyProperties(question, vo);
        vo.setIsRequired(question.getIsRequired() == 1);
        vo.setIsRefined(question.getIsRefined() == 1);

        if (question.getOptions() != null) {
            vo.setOptions(JSON.parseArray(question.getOptions(), OptionDTO.class));
        }

        return vo;
    }

    @Override
    public int syncWenjuanData(Long questionnaireId) {
        if (wenjuanIntegrationService == null) {
            throw new RuntimeException("问卷网集成服务未配置");
        }

        SurveyQuestionnaire questionnaire = questionnaireMapper.selectById(questionnaireId);
        if (questionnaire == null) {
            throw new RuntimeException("问卷不存在");
        }

        // 检查是否已发布到问卷网
        if (questionnaire.getWenjuanShortId() == null || questionnaire.getWenjuanShortId().isEmpty()) {
            throw new RuntimeException("该问卷未发布到问卷网，无法同步数据");
        }

        try {
            System.out.println("========== 开始同步问卷网数据 ==========");
            System.out.println("问卷ID: " + questionnaireId);
            System.out.println("问卷网shortId: " + questionnaire.getWenjuanShortId());

            // 调用问卷网API获取项目详情
            Map<String, Object> projectDetail = wenjuanIntegrationService
                    .getProjectDetail(questionnaire.getWenjuanShortId());

            System.out.println("问卷网API响应: " + projectDetail);

            Integer errCode = (Integer) projectDetail.get("err_code");
            if (errCode != null && errCode == 0) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) projectDetail.get("data");
                if (data != null) {
                    System.out.println("问卷网返回的data: " + data);
                    Integer respondentCount = (Integer) data.get("respondent_count");
                    System.out.println("问卷网答卷数: " + respondentCount);

                    if (respondentCount != null) {
                        // 更新答卷数量
                        questionnaire.setWenjuanRespondentCount(respondentCount);
                        questionnaire.setLastSyncTime(java.time.LocalDateTime.now());
                        questionnaireMapper.update(questionnaire);
                        System.out.println("成功同步并更新数据库，答卷数: " + respondentCount);
                        System.out.println("========== 同步完成 ==========");
                        return respondentCount;
                    }
                }
            }

            System.out.println("========== 同步失败 ==========");
            throw new RuntimeException("从问卷网获取数据失败: " + projectDetail.get("err_msg"));
        } catch (Exception e) {
            System.err.println("========== 同步异常 ==========");
            e.printStackTrace();
            throw new RuntimeException("同步问卷网数据失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String exportFormattedContent(Long questionnaireId) {
        // 获取问卷信息
        SurveyQuestionnaire questionnaire = questionnaireMapper.selectById(questionnaireId);
        if (questionnaire == null) {
            throw new RuntimeException("问卷不存在");
        }

        // 获取所有题目
        List<SurveyQuestion> questions = questionMapper.selectByQuestionnaireId(questionnaireId);
        if (questions == null || questions.isEmpty()) {
            throw new RuntimeException("问卷没有题目");
        }

        // 构建格式化的问卷内容
        StringBuilder content = new StringBuilder();

        // 1. 问卷标题
        content.append("问卷标题：");
        content.append(questionnaire.getTitle()).append("\n\n");

        // 2. 问卷说明
        content.append("问卷说明：\n");

        // 处理问卷引言，提取表格并格式化
        String introduction = questionnaire.getIntroduction();
        if (introduction != null && !introduction.isEmpty()) {
            // 处理表格部分
            if (introduction.contains("##TABLE_START##")) {
                String[] parts = introduction.split("##TABLE_START##");
                content.append(parts[0].trim()).append("\n\n");

                if (parts.length > 1) {
                    String[] tableParts = parts[1].split("##TABLE_END##");
                    if (tableParts.length > 0) {
                        // 格式化表格
                        content.append("【治疗方案对比表】\n\n");
                        String[] tableLines = tableParts[0].trim().split("\n");
                        for (String line : tableLines) {
                            String[] cells = line.split("\\|");
                            if (cells.length >= 3) {
                                content.append(String.format("%-15s %-25s %-25s\n",
                                        cells[0], cells[1], cells[2]));
                            }
                        }
                        content.append("\n");

                        // 添加表格后的内容
                        if (tableParts.length > 1) {
                            content.append(tableParts[1].trim()).append("\n\n");
                        }
                    }
                }
            } else {
                content.append(introduction).append("\n\n");
            }
        }

        // 添加填写说明
        if (questionnaire.getInstruction() != null && !questionnaire.getInstruction().isEmpty()) {
            content.append("\n【填写说明】\n");
            content.append(questionnaire.getInstruction()).append("\n");
        }

        content.append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");

        // 3. 题目列表 - 按照问卷网格式

        for (int i = 0; i < questions.size(); i++) {
            SurveyQuestion question = questions.get(i);

            // 题型说明
            String typeDesc = getQuestionTypeDesc(question.getQuestionType());
            
            // 处理题目文本，特殊处理包含"方案A"和"方案B"的题目
            String questionText = question.getQuestionText();
            if (questionText.contains("方案A") && questionText.contains("方案B")) {
                // 格式化为: 如果您面临...方案A：...和方案B：...【单选题】
                questionText = formatTreatmentChoiceQuestion(questionText);
            }

            // 问卷网格式: 题目【题型】（题目和题型在同一行）
            content.append(questionText).append("【").append(typeDesc).append("】\n");

            // 选项（如果有）- 问卷网格式：直接列出选项，不需要A、B、C编号
            if (question.getOptions() != null && !question.getOptions().isEmpty()) {
                try {
                    List<OptionDTO> options = JSON.parseArray(question.getOptions(), OptionDTO.class);
                    for (OptionDTO option : options) {
                        // 如果是评分题，添加评分说明
                        if ("RATING".equalsIgnoreCase(question.getQuestionType())) {
                            content.append(option.getLabel());
                            if (option.getDescription() != null && !option.getDescription().isEmpty()) {
                                content.append(" (").append(option.getDescription()).append(")");
                            }
                        } else {
                            // 普通选项，只输出选项文本
                            content.append(option.getLabel());
                        }
                        content.append("\n");
                    }
                } catch (Exception e) {
                    content.append("（选项解析失败，请检查数据）\n");
                }
            }

            // 题目之间空一行
            content.append("\n");
        }

        return content.toString();
    }

    /**
     * 格式化治疗方案选择题目
     * 将题目格式化为: 如果您面临...方案A：...和方案B：...
     */
    private String formatTreatmentChoiceQuestion(String questionText) {
        // 移除可能存在的换行符，确保是单行格式
        questionText = questionText.replaceAll("\\s+", " ").trim();
        
        // 如果题目中包含换行或其他分隔符，统一格式
        if (questionText.contains("方案A") && questionText.contains("方案B")) {
            // 确保格式为: ...方案A：...和方案B：...
            questionText = questionText
                .replaceAll("方案A[：:：]?\\s*", "方案A：")
                .replaceAll("方案B[：:：]?\\s*", "方案B：")
                .replaceAll("\\s+和\\s+方案B", "和方案B");
        }
        
        return questionText;
    }

    /**
     * 获取题型描述 - 符合问卷网格式
     */
    private String getQuestionTypeDesc(String questionType) {
        switch (questionType.toUpperCase()) {
            case "SINGLE_CHOICE":
                return "单选题";
            case "MULTIPLE_CHOICE":
                return "多选题";
            case "TEXT":
            case "TEXTAREA":
                return "填空题";
            case "RATING":
                return "评分题";
            default:
                return "填空题";
        }
    }
}
