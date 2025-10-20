package org.isoft.panelsurvey.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.isoft.panelsurvey.common.Result;
import org.isoft.panelsurvey.dto.PolishedBackgroundDTO;
import org.isoft.panelsurvey.dto.UpdateQuestionDTO;
import org.isoft.panelsurvey.service.QuestionnaireService;
import org.isoft.panelsurvey.vo.QuestionnaireVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "问卷管理", description = "问卷生成、审核、发布、更新等操作")
@RestController
@RequestMapping("/api/questionnaire")
public class QuestionnaireController {

    private static final Logger log = LoggerFactory.getLogger(QuestionnaireController.class);

    @Autowired
    private QuestionnaireService questionnaireService;

    @Operation(summary = "生成问卷", description = "根据项目ID生成问卷")
    @PostMapping("/generate/{projectId}")
    public Result<QuestionnaireVO> generateQuestionnaire(
            @Parameter(description = "项目ID", required = true)
            @PathVariable Long projectId) {
        QuestionnaireVO questionnaire = questionnaireService.generateQuestionnaire(projectId);
        return Result.success("问卷生成成功", questionnaire);
    }

    @Operation(summary = "获取问卷详情", description = "根据问卷ID获取问卷详细信息")
    @GetMapping("/{id}")
    public Result<QuestionnaireVO> getQuestionnaire(
            @Parameter(description = "问卷ID", required = true)
            @PathVariable Long id) {
        QuestionnaireVO questionnaire = questionnaireService.getQuestionnaireById(id);
        return Result.success(questionnaire);
    }

    @Operation(summary = "根据项目ID获取问卷", description = "根据项目ID获取对应的问卷")
    @GetMapping("/project/{projectId}")
    public Result<QuestionnaireVO> getQuestionnaireByProjectId(
            @Parameter(description = "项目ID", required = true)
            @PathVariable Long projectId) {
        QuestionnaireVO questionnaire = questionnaireService.getByProjectId(projectId);
        return Result.success(questionnaire);
    }

    @Operation(summary = "更新问卷题目", description = "更新问卷中的题目内容")
    @PutMapping("/question/{questionId}")
    public Result<String> updateQuestion(
            @Parameter(description = "题目ID", required = true)
            @PathVariable Long questionId,
            @Parameter(description = "更新题目信息", required = true)
            @RequestBody UpdateQuestionDTO updateQuestionDTO) {
        questionnaireService.updateQuestion(questionId, updateQuestionDTO.getQuestionText());
        return Result.success("题目更新成功", null);
    }

    @Operation(summary = "AI审核问卷", description = "使用AI对问卷进行审核和优化")
    @PostMapping("/review/{id}")
    public Result<QuestionnaireVO> reviewQuestionnaire(
            @Parameter(description = "问卷ID", required = true)
            @PathVariable Long id) {
        QuestionnaireVO questionnaire = questionnaireService.reviewByAI(id);
        return Result.success("问卷审核完成", questionnaire);
    }
}