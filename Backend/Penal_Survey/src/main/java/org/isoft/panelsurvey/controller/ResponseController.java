package org.isoft.panelsurvey.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.isoft.panelsurvey.common.Result;
import org.isoft.panelsurvey.dto.SurveyResponseDTO;
import org.isoft.panelsurvey.service.ResponseService;
import org.isoft.panelsurvey.service.QuestionnaireService;
import org.isoft.panelsurvey.vo.QuestionnaireVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "问卷填写", description = "公开问卷填写和答卷提交相关接口")
@RestController
@RequestMapping("/api/survey")
public class ResponseController {

    private static final Logger log = LoggerFactory.getLogger(ResponseController.class);

    @Autowired
    private ResponseService responseService;

    @Autowired
    private QuestionnaireService questionnaireService;

    @Operation(summary = "获取公开问卷", description = "获取可以公开填写的问卷信息")
    @GetMapping("/public/{questionnaireId}")
    public Result<QuestionnaireVO> getPublicQuestionnaire(
            @Parameter(description = "问卷ID", required = true)
            @PathVariable Long questionnaireId) {
        QuestionnaireVO questionnaire = questionnaireService.getQuestionnaireById(questionnaireId);
        return Result.success(questionnaire);
    }

    @Operation(summary = "提交问卷答案", description = "提交用户填写的问卷答案")
    @PostMapping("/public/submit")
    public Result<String> submitResponse(
            @Parameter(description = "问卷答案", required = true)
            @Valid @RequestBody SurveyResponseDTO responseDTO,
            HttpServletRequest request) {
        
        // 获取用户IP地址
        String ipAddress = getClientIpAddress(request);
        
        responseService.submitResponse(responseDTO, ipAddress);
        return Result.success("问卷提交成功", null);
    }

    @Operation(summary = "获取问卷回答数量", description = "获取问卷的回答总数")
    @GetMapping("/public/count/{questionnaireId}")
    public Result<Integer> getResponseCount(
            @Parameter(description = "问卷ID", required = true)
            @PathVariable Long questionnaireId) {
        int count = responseService.getResponseCount(questionnaireId);
        return Result.success(count);
    }

    @Operation(summary = "获取问卷完成数量", description = "获取问卷的完成数量")
    @GetMapping("/public/completed/{questionnaireId}")
    public Result<Integer> getCompletedCount(
            @Parameter(description = "问卷ID", required = true)
            @PathVariable Long questionnaireId) {
        int count = responseService.getCompletedCount(questionnaireId);
        return Result.success(count);
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}