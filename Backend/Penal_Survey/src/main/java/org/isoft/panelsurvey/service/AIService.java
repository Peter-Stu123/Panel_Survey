package org.isoft.panelsurvey.service;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AIService {

    @Autowired
    private ChatModel chatModel;

    public String reviewAndRefineQuestion(String questionText, String questionType) {
        String prompt = buildReviewPrompt(questionText, questionType);
        return callAI(prompt);
    }

    public String reviewQuestionnaire(String questionnaireContent) {
        String prompt = "请作为一位医学问卷专家，审查以下问卷内容，检查是否存在语义不通、表述不清、逻辑混乱等问题。\n\n" +
                       "问卷内容：\n" + questionnaireContent + "\n\n" +
                       "请以JSON格式返回审查结果，包含：\n" +
                       "1. hasIssues（布尔值，是否存在问题）\n" +
                       "2. issues（数组，具体问题列表）\n" +
                       "3. suggestions（数组，改进建议）";
        return callAI(prompt);
    }

    public String refineQuestionText(String originalText, String issues) {
        String prompt = "请润色以下医学问卷题目，使其表述更加清晰、准确、专业。\n\n" +
                       "原始题目：" + originalText + "\n\n" +
                       "存在的问题：" + issues + "\n\n" +
                       "请直接返回润色后的题目文本，不要包含任何额外说明。";
        return callAI(prompt);
    }

    public String generateQuestionnaireIntroduction(String diseaseName, String interventionName, String objective) {
        String prompt = "请为一份关于" + diseaseName + "疾病的" + interventionName + "干预措施的医学问卷生成一段专业的引言。\n\n" +
                       "问卷目标：" + objective + "\n\n" +
                       "引言应包含：\n" +
                       "1. 研究背景和目的\n" +
                       "2. 患者参与的重要性\n" +
                       "3. 问卷的保密性说明\n" +
                       "4. 预计完成时间\n\n" +
                       "【关键要求】\n" +
                       "- 直接输出引言内容，不要任何前缀（如'好的'、'以下是'等）\n" +
                       "- 不要任何后缀或询问语句\n" +
                       "- 以友好、专业的语气撰写\n" +
                       "- 长度控制在200-300字\n\n" +
                       "直接输出引言内容：";
        return cleanAIResponse(callAI(prompt));
    }
    
    public String polishTreatmentInfo(String title, String details) {
        if (details == null || details.trim().isEmpty()) {
            return "";
        }
        
        String prompt = "你是医学文案专家。请润色以下治疗方案信息，使其更清晰、专业、易懂。\n\n" +
                       "方案名称：" + title + "\n\n" +
                       "原始信息：\n" + details + "\n\n" +
                       "【要求】\n" +
                       "1. 保留所有关键医学信息\n" +
                       "2. 使用患者能理解的语言\n" +
                       "3. 条理清晰，分段呈现\n" +
                       "4. 不要添加原文没有的信息\n" +
                       "5. 直接输出润色后的内容，不要前缀和后缀\n" +
                       "6. 控制在150-200字\n\n" +
                       "直接输出润色后的内容：";
        
        return callAI(prompt);
    }
    
    // 为表格生成简洁的润色内容（50-80字）
    public String polishForTable(String title, String details) {
        if (details == null || details.trim().isEmpty()) {
            return "";
        }
        
        String prompt = "你是医学文案专家。请将以下内容精炼为简洁、清晰的描述，适合在表格中展示。\n\n" +
                       "方案名称：" + title + "\n\n" +
                       "原始信息：\n" + details + "\n\n" +
                       "【要求】\n" +
                       "1. 提取核心要点，去除冗余\n" +
                       "2. 使用简洁、专业的医学术语\n" +
                       "3. 保留关键信息（如治疗方式、适用情况、特点等）\n" +
                       "4. 控制在50-80字以内\n" +
                       "5. 直接输出精炼后的内容，不要前缀和后缀\n\n" +
                       "直接输出精炼内容：";
        
        return cleanAIResponse(callAI(prompt));
    }
    
    public String generateTreatmentComparison(String interventionName, String interventionInfo, 
                                             String comparisonName, String comparisonInfo) {
        String prompt = "你是医学信息专家。请基于两种治疗方案的信息，生成一段简洁的对比说明。\n\n" +
                       "【方案A：" + interventionName + "】\n" + interventionInfo + "\n\n" +
                       "【方案B：" + comparisonName + "】\n" + comparisonInfo + "\n\n" +
                       "【要求】\n" +
                       "1. 客观中立，不偏向任何方案\n" +
                       "2. 突出两者的主要区别\n" +
                       "3. 使用表格或列表形式对比\n" +
                       "4. 患者易于理解\n" +
                       "5. 控制在200字以内\n" +
                       "6. 使用【】标记关键点，不使用Markdown格式\n\n" +
                       "直接输出对比说明：";
        
        return cleanAIResponse(callAI(prompt));
    }

    public String analyzePreferences(String statisticalData, String questionnaireInfo) {
        String prompt = "你是医学数据分析专家。基于问卷统计数据分析患者价值观和偏好。\n\n" +
                       "问卷信息：" + questionnaireInfo + "\n\n" +
                       "统计数据：" + statisticalData + "\n\n" +
                       "【严格要求】\n" +
                       "1. 禁止使用任何开场白，如\"好的\"、\"我将\"、\"基于\"、\"根据\"等\n" +
                       "2. 绝对禁止使用Markdown语法：**、##、#、---、*、-等\n" +
                       "3. 禁止在结尾添加问句或引导语\n" +
                       "4. 仅使用【】标记章节标题，「」标记关键词\n" +
                       "5. 列表使用数字格式：1. 2. 3.\n" +
                       "6. 段落间用空行分隔\n\n" +
                       "输出格式示例：\n\n" +
                       "【患者偏好主要趋势】\n" +
                       "从数据来看，患者对「疗效」和「安全性」表现出强烈关注。大多数患者倾向于选择恢复期较短的治疗方案，同时对术后「生活质量」的重视程度超过治疗费用。整体上，患者更偏好保守治疗而非激进干预。\n\n" +
                       "【不同选项接受度分析】\n" +
                       "选项A获得68%的认可度，显示患者对低风险方案的青睐。选项B虽然疗效最佳，但仅有32%接受度，主要原因是「恢复周期长」。选项C接受度最低，反映患者对高费用方案的抵触情绪较强。\n\n" +
                       "【关键发现】\n" +
                       "1. 患者优先考虑治疗安全性，其次才是疗效\n" +
                       "2. 年龄越大的患者越倾向于保守治疗\n" +
                       "3. 经济因素影响显著，但不是决定性因素\n\n" +
                       "【临床决策启示】\n" +
                       "制定治疗方案时应充分考虑患者的「风险承受能力」和「生活质量期望」。建议在术前沟通中详细解释各方案的利弊，尤其是恢复时间和并发症风险。对于老年患者，应优先推荐保守方案。\n\n" +
                       "严格按此格式输出，直接从【患者偏好主要趋势】开始，不要有任何前缀和后缀。";
        
        String result = callAI(prompt);
        return cleanAIResponse(result);
    }
    
    // 清洗AI返回的内容
    private String cleanAIResponse(String response) {
        if (response == null || response.isEmpty()) {
            return response;
        }
        
        String cleaned = response.trim();
        
        // 移除开头的常见前缀（多行）
        cleaned = cleaned.replaceAll("^(好的[，,。.！!]?|我将|让我|下面|以下是|根据|基于)[^【\\n]*[\\n]*", "");
        cleaned = cleaned.replaceAll("^[^【]*按照[^【]*[\\n]+", "");
        cleaned = cleaned.replaceAll("^---+[\\n]*", "");
        
        // 移除Markdown标题符号
        cleaned = cleaned.replaceAll("#+\\s*", "");
        
        // 移除Markdown加粗符号
        cleaned = cleaned.replaceAll("\\*\\*([^*]+)\\*\\*", "$1");
        
        // 移除Markdown分隔线
        cleaned = cleaned.replaceAll("---+", "");
        
        // 移除结尾的引导性问句和后缀（更强力的删除）
        // 从"如果您需要"或类似词语开始到文本结束的所有内容
        cleaned = cleaned.replaceAll("[。.！!\\n]*\\s*(如果您(需要|提供)|您是否希望|是否需要|请问是否|您需要|我可以(进一步|帮您)|您要我).*$", "");
        
        // 移除包含问号的最后一段（通常是AI的询问）
        String[] paragraphs = cleaned.split("\\n\\n");
        if (paragraphs.length > 0) {
            String lastParagraph = paragraphs[paragraphs.length - 1];
            if (lastParagraph.contains("？") || lastParagraph.contains("?")) {
                // 删除最后一段
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < paragraphs.length - 1; i++) {
                    if (i > 0) sb.append("\n\n");
                    sb.append(paragraphs[i]);
                }
                cleaned = sb.toString();
            }
        }
        
        // 移除多余的空行（超过2个连续换行）
        cleaned = cleaned.replaceAll("\\n{3,}", "\n\n");
        
        return cleaned.trim();
    }

    private String buildReviewPrompt(String questionText, String questionType) {
        return "作为医学问卷专家，请审查并优化以下问卷题目：\n\n" +
               "题目类型：" + questionType + "\n" +
               "题目内容：" + questionText + "\n\n" +
               "请检查：\n" +
               "1. 语义是否清晰通顺\n" +
               "2. 医学术语使用是否准确\n" +
               "3. 是否存在歧义或误导\n" +
               "4. 表述是否专业且易于理解\n\n" +
               "如果需要改进，请直接返回优化后的题目。如果已经很好，返回'无需修改'。";
    }

    private String callAI(String promptText) {
        try {
            List<Message> messages = new ArrayList<>();
            messages.add(new UserMessage(promptText));
            Prompt prompt = new Prompt(messages);
            
            return chatModel.call(prompt).getResult().getOutput().getText();
        } catch (Exception e) {
            throw new RuntimeException("AI服务调用失败: " + e.getMessage(), e);
        }
    }
}

