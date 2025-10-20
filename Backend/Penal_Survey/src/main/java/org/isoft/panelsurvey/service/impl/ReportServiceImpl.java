package org.isoft.panelsurvey.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.isoft.panelsurvey.entity.*;
import org.isoft.panelsurvey.mapper.*;
import org.isoft.panelsurvey.service.AIService;
import org.isoft.panelsurvey.service.ReportService;
import org.isoft.panelsurvey.vo.ReportVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private SurveyReportMapper reportMapper;

    @Autowired
    private SurveyResponseMapper responseMapper;

    @Autowired
    private SurveyAnswerMapper answerMapper;

    @Autowired
    private SurveyQuestionnaireMapper questionnaireMapper;

    @Autowired
    private SurveyQuestionMapper questionMapper;

    @Autowired
    private SurveyProjectMapper projectMapper;

    @Autowired
    private AIService aiService;

    @Autowired
    private org.isoft.panelsurvey.service.WenjuanIntegrationService wenjuanIntegrationService;

    @Override
    @Transactional
    public ReportVO generateReport(Long questionnaireId) {
        SurveyQuestionnaire questionnaire = questionnaireMapper.selectById(questionnaireId);
        if (questionnaire == null) {
            throw new RuntimeException("问卷不存在");
        }

        // 如果问卷已发布到问卷网，尝试同步最新数据
        if (questionnaire.getWenjuanShortId() != null && !questionnaire.getWenjuanShortId().isEmpty()) {
            try {
                // 同步问卷网答卷数量
                wenjuanIntegrationService.syncAnswerCount(questionnaireId);
            } catch (Exception e) {
                // 同步失败不影响报告生成，使用本地数据
                System.err.println("同步问卷网数据失败: " + e.getMessage());
            }
        }

        int totalResponses = responseMapper.countByQuestionnaireId(questionnaireId);
        int validResponses = responseMapper.countCompletedByQuestionnaireId(questionnaireId);

        if (validResponses == 0) {
            throw new RuntimeException("暂无有效回复数据，请先在问卷网添加题目并发布问卷，等待受访者填写");
        }

        List<SurveyResponse> responses = responseMapper.selectByQuestionnaireId(questionnaireId);
        List<SurveyQuestion> questions = questionMapper.selectByQuestionnaireId(questionnaireId);

        Map<String, Object> statisticalData = calculateStatistics(responses, questions);
        Map<String, Object> chartData = generateChartData(responses, questions);

        BigDecimal completionRate = BigDecimal.valueOf(validResponses)
                .divide(BigDecimal.valueOf(totalResponses), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        double avgDuration = responses.stream()
                .filter(r -> r.getDurationSeconds() != null)
                .mapToInt(r -> r.getDurationSeconds())
                .average()
                .orElse(0.0);

        String aiAnalysis = aiService.analyzePreferences(
            JSON.toJSONString(statisticalData),
            questionnaire.getTitle() + " - " + questionnaire.getDescription()
        );

        SurveyReport report = new SurveyReport();
        report.setProjectId(questionnaire.getProjectId());
        report.setQuestionnaireId(questionnaireId);
        report.setReportTitle(questionnaire.getTitle() + " - 分析报告");
        report.setTotalResponses(totalResponses);
        report.setValidResponses(validResponses);
        report.setCompletionRate(completionRate);
        report.setAverageDuration((int) avgDuration);
        report.setStatisticalData(JSON.toJSONString(statisticalData));
        report.setChartData(JSON.toJSONString(chartData));
        report.setPreferenceSummary(aiAnalysis);
        report.setKeyFindings(extractKeyFindings(statisticalData));
        report.setReportContent(generateReportHtml(report, statisticalData, chartData));
        report.setGenerateTime(LocalDateTime.now());

        reportMapper.insert(report);

        projectMapper.updateStepStatus(questionnaire.getProjectId(), 6);
        projectMapper.updateStatus(questionnaire.getProjectId(), "COMPLETED");

        return getReportById(report.getId());
    }

    @Override
    public ReportVO getReportByQuestionnaireId(Long questionnaireId) {
        SurveyReport report = reportMapper.selectByQuestionnaireId(questionnaireId);
        if (report == null) {
            return null;
        }
        return convertToVO(report);
    }

    @Override
    public ReportVO getReportById(Long id) {
        SurveyReport report = reportMapper.selectById(id);
        if (report == null) {
            throw new RuntimeException("报告不存在");
        }
        return convertToVO(report);
    }

    @Override
    public String exportToPdf(Long reportId) {
        return "/reports/report_" + reportId + ".pdf";
    }

    @Override
    public String exportToExcel(Long reportId) {
        return "/reports/report_" + reportId + ".xlsx";
    }

    private Map<String, Object> calculateStatistics(List<SurveyResponse> responses, List<SurveyQuestion> questions) {
        Map<String, Object> statistics = new HashMap<>();

        for (SurveyQuestion question : questions) {
            Map<String, Object> questionStats = new HashMap<>();
            List<SurveyAnswer> answers = answerMapper.selectByQuestionId(question.getId());

            if ("SINGLE_CHOICE".equals(question.getQuestionType()) || "MULTIPLE_CHOICE".equals(question.getQuestionType())) {
                Map<String, Long> distribution = answers.stream()
                        .collect(Collectors.groupingBy(SurveyAnswer::getAnswerText, Collectors.counting()));
                questionStats.put("distribution", distribution);
                questionStats.put("totalAnswers", answers.size());
            } else if ("RATING".equals(question.getQuestionType())) {
                double avgRating = answers.stream()
                        .filter(a -> a.getAnswerValue() != null)
                        .mapToDouble(a -> {
                            try {
                                return Double.parseDouble(a.getAnswerValue());
                            } catch (Exception e) {
                                return 0.0;
                            }
                        })
                        .average()
                        .orElse(0.0);
                questionStats.put("averageRating", avgRating);
                questionStats.put("totalAnswers", answers.size());
            } else if ("TEXT".equals(question.getQuestionType())) {
                questionStats.put("totalAnswers", answers.size());
                questionStats.put("sampleAnswers", answers.stream()
                        .limit(5)
                        .map(SurveyAnswer::getAnswerText)
                        .collect(Collectors.toList()));
            }

            statistics.put("question_" + question.getId(), questionStats);
        }

        return statistics;
    }

    private Map<String, Object> generateChartData(List<SurveyResponse> responses, List<SurveyQuestion> questions) {
        Map<String, Object> chartData = new HashMap<>();

        for (SurveyQuestion question : questions) {
            if ("SINGLE_CHOICE".equals(question.getQuestionType())) {
                List<SurveyAnswer> answers = answerMapper.selectByQuestionId(question.getId());
                Map<String, Long> distribution = answers.stream()
                        .collect(Collectors.groupingBy(SurveyAnswer::getAnswerText, Collectors.counting()));

                Map<String, Object> chart = new HashMap<>();
                chart.put("type", "pie");
                chart.put("labels", new ArrayList<>(distribution.keySet()));
                chart.put("data", new ArrayList<>(distribution.values()));
                chart.put("title", question.getQuestionText());

                chartData.put("chart_" + question.getId(), chart);
            }
        }

        return chartData;
    }

    private String extractKeyFindings(Map<String, Object> statisticalData) {
        StringBuilder findings = new StringBuilder();
        findings.append("1. 共收集到有效问卷样本\n");
        findings.append("2. 患者对治疗方案表现出明确的偏好倾向\n");
        findings.append("3. 不同结局指标的重要性评价存在差异\n");
        findings.append("4. 建议在临床决策中考虑患者的个体化需求");
        return findings.toString();
    }

    private String generateReportHtml(SurveyReport report, Map<String, Object> stats, Map<String, Object> charts) {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta charset='UTF-8'><title>").append(report.getReportTitle()).append("</title></head>");
        html.append("<body><h1>").append(report.getReportTitle()).append("</h1>");
        html.append("<h2>基本统计</h2>");
        html.append("<p>总回复数: ").append(report.getTotalResponses()).append("</p>");
        html.append("<p>有效回复数: ").append(report.getValidResponses()).append("</p>");
        html.append("<p>完成率: ").append(report.getCompletionRate()).append("%</p>");
        html.append("<p>平均完成时间: ").append(report.getAverageDuration()).append("秒</p>");
        html.append("<h2>偏好分析</h2>");
        html.append("<p>").append(report.getPreferenceSummary()).append("</p>");
        html.append("<h2>关键发现</h2>");
        html.append("<pre>").append(report.getKeyFindings()).append("</pre>");
        html.append("</body></html>");
        return html.toString();
    }

    private ReportVO convertToVO(SurveyReport report) {
        ReportVO vo = new ReportVO();
        BeanUtils.copyProperties(report, vo);

        if (report.getStatisticalData() != null) {
            vo.setStatisticalData(JSON.parseObject(report.getStatisticalData()));
        }

        if (report.getChartData() != null) {
            vo.setChartData(JSON.parseObject(report.getChartData()));
        }

        return vo;
    }

    @Override
    @Transactional
    public ReportVO generateReportFromExcel(org.springframework.web.multipart.MultipartFile file, Long questionnaireId) {
        try {
            SurveyQuestionnaire questionnaire = questionnaireMapper.selectById(questionnaireId);
            if (questionnaire == null) {
                throw new RuntimeException("问卷不存在");
            }

            SurveyProject project = projectMapper.selectById(questionnaire.getProjectId());
            if (project == null) {
                throw new RuntimeException("项目不存在");
            }

            // 解析Excel文件
            org.apache.poi.ss.usermodel.Workbook workbook;
            try {
                workbook = org.apache.poi.ss.usermodel.WorkbookFactory.create(file.getInputStream());
            } catch (Exception e) {
                throw new RuntimeException("Excel文件解析失败: " + e.getMessage());
            }

            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getLastRowNum() < 1) {
                throw new RuntimeException("Excel文件为空或格式不正确");
            }

            // 解析数据
            Map<String, Object> analysisData = parseExcelData(sheet, questionnaire);
            
            // 生成AI分析
            String aiAnalysis = generateAIAnalysis(analysisData, project, questionnaire);
            
            // 保存报告
            SurveyReport report = new SurveyReport();
            report.setProjectId(project.getId());  // 添加 project_id
            report.setQuestionnaireId(questionnaireId);
            report.setReportTitle(project.getProjectName() + " - 患者偏好分析报告");
            report.setTotalResponses((Integer) analysisData.get("totalResponses"));
            report.setValidResponses((Integer) analysisData.get("validResponses"));
            report.setCompletionRate((BigDecimal) analysisData.get("completionRate"));
            report.setStatisticalData(JSON.toJSONString(analysisData));
            report.setChartData(JSON.toJSONString(analysisData));  // 使用相同的数据
            report.setPreferenceSummary(aiAnalysis);
            report.setKeyFindings(generateKeyFindings(analysisData));
            report.setGenerateTime(LocalDateTime.now());

            reportMapper.insert(report);

            return convertToVO(report);

        } catch (Exception e) {
            throw new RuntimeException("生成报告失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析Excel数据
     */
    private Map<String, Object> parseExcelData(org.apache.poi.ss.usermodel.Sheet sheet, SurveyQuestionnaire questionnaire) {
        Map<String, Object> result = new HashMap<>();
        
        // 读取表头（第一行）
        org.apache.poi.ss.usermodel.Row headerRow = sheet.getRow(0);
        List<String> headers = new ArrayList<>();
        if (headerRow != null) {
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.getCell(i);
                headers.add(cell != null ? cell.toString().trim() : "");
            }
        }

        // 统计数据
        int totalRows = sheet.getLastRowNum(); // 不包括表头
        int validRows = 0;

        // 统计各题目的回答
        Map<String, Map<String, Integer>> questionStats = new HashMap<>();
        
        // 遍历数据行
        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            org.apache.poi.ss.usermodel.Row row = sheet.getRow(rowIndex);
            if (row == null) continue;
            
            validRows++;
            
            // 遍历每一列（每个问题）
            for (int colIndex = 0; colIndex < headers.size(); colIndex++) {
                String header = headers.get(colIndex);
                org.apache.poi.ss.usermodel.Cell cell = row.getCell(colIndex);
                String answer = cell != null ? cell.toString().trim() : "";
                
                if (header.isEmpty() || answer.isEmpty()) continue;
                
                // 统计该题目的答案分布
                questionStats.putIfAbsent(header, new HashMap<>());
                Map<String, Integer> answerCounts = questionStats.get(header);
                answerCounts.put(answer, answerCounts.getOrDefault(answer, 0) + 1);
            }
        }

        result.put("totalResponses", validRows);
        result.put("validResponses", validRows);
        result.put("completionRate", totalRows > 0 ? 
            new BigDecimal(validRows * 100.0 / totalRows).setScale(2, RoundingMode.HALF_UP) : 
            BigDecimal.ZERO);
        result.put("questionStats", questionStats);
        result.put("headers", headers);

        return result;
    }

    /**
     * 生成AI分析（针对医生的专业分析 - 简洁版）
     */
    private String generateAIAnalysis(Map<String, Object> data, SurveyProject project, SurveyQuestionnaire questionnaire) {
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Integer>> questionStats = (Map<String, Map<String, Integer>>) data.get("questionStats");
        int totalResponses = (Integer) data.get("totalResponses");

        StringBuilder analysis = new StringBuilder();
        
        // 标题
        analysis.append("# ").append(project.getProjectName()).append("\n\n");
        analysis.append("*疾病：").append(project.getDiseaseName() != null ? project.getDiseaseName() : "未指定");
        analysis.append(" | 样本量：").append(totalResponses).append("例*\n\n");
        analysis.append("---\n\n");
        
        // ========== 核心部分：临床建议（放在最前面）==========
        analysis.append("## 💊 临床建议\n\n");
        
        // 查找治疗偏好问题并生成建议
        String treatmentPreference = null;
        double preferenceRate = 0;
        List<String> allOptions = new ArrayList<>();
        
        for (Map.Entry<String, Map<String, Integer>> entry : questionStats.entrySet()) {
            if (entry.getKey().contains("方案") || entry.getKey().contains("倾向") || entry.getKey().contains("治疗选择")) {
                Map<String, Integer> answers = entry.getValue();
                List<Map.Entry<String, Integer>> sorted = new ArrayList<>(answers.entrySet());
                sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));
                
                if (!sorted.isEmpty()) {
                    Map.Entry<String, Integer> top = sorted.get(0);
                    preferenceRate = (top.getValue() * 100.0) / totalResponses;
                    treatmentPreference = top.getKey();
                    
                    // 收集所有选项
                    for (Map.Entry<String, Integer> answer : sorted) {
                        double percentage = (answer.getValue() * 100.0) / totalResponses;
                        allOptions.add(String.format("%s (%.1f%%)", answer.getKey(), percentage));
                    }
                }
                break;
            }
        }
        
        // 生成临床建议（核心内容）
        if (treatmentPreference != null) {
            analysis.append("### 🎯 推荐治疗方案\n\n");
            analysis.append("基于 **").append(totalResponses).append("例** 患者偏好调查数据分析：\n\n");
            analysis.append("- **优先推荐：**").append(treatmentPreference).append("\n");
            analysis.append("- **患者偏好度：**").append(String.format("%.1f", preferenceRate)).append("%\n");
            analysis.append("- **证据强度：**").append(totalResponses >= 30 ? "中等" : "较弱").append("\n\n");
            
            analysis.append("### 📋 实施建议\n\n");
            analysis.append("**1. 优先方案：**\n\n");
            analysis.append("对于符合适应症的患者，优先考虑「").append(treatmentPreference)
                    .append("」，该方案获得了 ").append(String.format("%.1f", preferenceRate))
                    .append("% 患者的认可。\n\n");
            
            analysis.append("**2. 个性化评估：**\n\n");
            analysis.append("- 详细评估患者的具体病情、并发症、年龄等因素\n");
            analysis.append("- 考虑患者的个人意愿和生活质量需求\n");
            analysis.append("- 评估治疗的风险-收益比\n\n");
            
            analysis.append("**3. 知情同意：**\n\n");
            analysis.append("- 向患者详细说明各治疗方案的利弊\n");
            analysis.append("- 告知可能的风险和预期效果\n");
            analysis.append("- 尊重患者的最终选择\n\n");
            
            analysis.append("**4. 随访管理：**\n\n");
            analysis.append("- 制定详细的治疗和随访计划\n");
            analysis.append("- 定期评估治疗效果\n");
            analysis.append("- 根据患者反馈及时调整方案\n\n");
        } else {
            analysis.append("### 📋 一般临床建议\n\n");
            analysis.append("**1. 个性化治疗：**结合患者具体病情与偏好，制定个性化方案\n\n");
            analysis.append("**2. 医患沟通：**详细说明各方案利弊，确保患者理解\n\n");
            analysis.append("**3. 知情同意：**充分尊重患者意愿，确保知情同意\n\n");
            analysis.append("**4. 随访评估：**定期随访，及时调整治疗方案\n\n");
        }
        
        analysis.append("---\n\n");
        
        // 显示患者偏好分布（简洁版）
        if (treatmentPreference != null) {
            analysis.append("### 患者偏好分布\n\n");
            analysis.append("| 治疗方案选择 | 患者偏好 |\n");
            analysis.append("|------------|----------|\n");
            for (String option : allOptions) {
                analysis.append("| ").append(option.split(" \\(")[0]).append(" | ")
                        .append(option.split(" \\(")[1].replace(")", "")).append(" |\n");
            }
            analysis.append("\n");
        }
        
        // ========== 详细调查数据 ==========
        analysis.append("## 📈 详细调查数据\n\n");
        
        int questionIndex = 1;
        for (Map.Entry<String, Map<String, Integer>> entry : questionStats.entrySet()) {
            String question = entry.getKey();
            Map<String, Integer> answers = entry.getValue();
            
            analysis.append("**").append(questionIndex++).append(". ").append(question).append("**\n\n");
            
            // 计算百分比并排序
            List<Map.Entry<String, Integer>> sortedAnswers = new ArrayList<>(answers.entrySet());
            sortedAnswers.sort((a, b) -> b.getValue().compareTo(a.getValue()));
            
            // 简洁列表展示
            for (Map.Entry<String, Integer> answer : sortedAnswers) {
                double percentage = (answer.getValue() * 100.0) / totalResponses;
                analysis.append("- ").append(answer.getKey()).append("：")
                        .append(answer.getValue()).append("人（")
                        .append(String.format("%.1f", percentage)).append("%）\n");
            }
            analysis.append("\n");
        }
        
        // 数据说明
        analysis.append("---\n\n");
        analysis.append("## 📝 报告说明\n\n");
        analysis.append("- **样本量：**").append(totalResponses).append("例\n");
        analysis.append("- **数据来源：**问卷网调查平台\n");
        analysis.append("- **生成时间：**").append(LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm"))).append("\n");
        analysis.append("- **注意事项：**本报告基于患者偏好调查数据生成，临床决策需结合实际情况综合判断\n\n");
        
        return analysis.toString();
    }

    /**
     * 生成关键发现
     */
    private String generateKeyFindings(Map<String, Object> data) {
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Integer>> questionStats = (Map<String, Map<String, Integer>>) data.get("questionStats");
        int totalResponses = (Integer) data.get("totalResponses");
        
        StringBuilder findings = new StringBuilder();
        findings.append("关键发现：\n\n");
        
        int findingIndex = 1;
        for (Map.Entry<String, Map<String, Integer>> entry : questionStats.entrySet()) {
            if (findingIndex > 5) break; // 只列出前5个关键发现
            
            String question = entry.getKey();
            Map<String, Integer> answers = entry.getValue();
            
            // 找出最高频答案
            Map.Entry<String, Integer> maxEntry = answers.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .orElse(null);
            
            if (maxEntry != null) {
                double percentage = (maxEntry.getValue() * 100.0) / totalResponses;
                findings.append(findingIndex++).append(". ")
                        .append(question).append("：")
                        .append(String.format("%.1f", percentage)).append("%的受访者选择\"")
                        .append(maxEntry.getKey()).append("\"\n");
            }
        }
        
        return findings.toString();
    }
}

