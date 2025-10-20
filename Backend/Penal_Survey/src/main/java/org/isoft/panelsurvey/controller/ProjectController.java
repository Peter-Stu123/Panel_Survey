package org.isoft.panelsurvey.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.isoft.panelsurvey.common.Result;
import org.isoft.panelsurvey.dto.Step1DTO;
import org.isoft.panelsurvey.dto.Step2DTO;
import org.isoft.panelsurvey.service.DraftService;
import org.isoft.panelsurvey.service.SurveyProjectService;
import org.isoft.panelsurvey.vo.SurveyProjectVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "项目管理", description = "问卷调查项目的创建、查询、更新、删除等操作")
@RestController
@RequestMapping("/api/project")
public class ProjectController {

    private static final Logger log = LoggerFactory.getLogger(ProjectController.class);

    @Autowired
    private SurveyProjectService projectService;

    @Autowired
    private DraftService draftService;

    @Operation(summary = "创建项目-第一步", description = "创建新的问卷调查项目")
    @PostMapping("/step1")
    public Result<SurveyProjectVO> createProject(
            @Parameter(description = "项目第一步信息", required = true)
            @Valid @RequestBody Step1DTO step1DTO, 
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        SurveyProjectVO project = projectService.createProject(step1DTO, userId);

        // 创建成功后，清除step1的草稿
        draftService.deleteDraft(userId, "step1");

        return Result.success("项目创建成功", project);
    }

    @Operation(summary = "获取用户所有项目", description = "获取当前用户的所有项目列表")
    @GetMapping("/list")
    public Result<List<SurveyProjectVO>> getUserProjects(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<SurveyProjectVO> projects = projectService.getUserProjects(userId);
        return Result.success(projects);
    }

    @Operation(summary = "获取项目详情", description = "根据项目ID获取项目详细信息")
    @GetMapping("/{id}")
    public Result<SurveyProjectVO> getProject(
            @Parameter(description = "项目ID", required = true)
            @PathVariable Long id) {
        SurveyProjectVO project = projectService.getProjectById(id);
        return Result.success(project);
    }

    @Operation(summary = "删除项目", description = "删除指定的项目")
    @DeleteMapping("/{id}")
    public Result<String> deleteProject(
            @Parameter(description = "项目ID", required = true)
            @PathVariable Long id) {
        projectService.deleteProject(id);
        return Result.success("项目删除成功", null);
    }
}