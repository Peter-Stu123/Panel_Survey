package org.isoft.panelsurvey.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.isoft.panelsurvey.common.Result;
import org.isoft.panelsurvey.service.DraftService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "草稿管理", description = "草稿保存、获取、删除等操作，基于Redis存储")
@RestController
@RequestMapping("/api/draft")
public class DraftController {

    private static final Logger log = LoggerFactory.getLogger(DraftController.class);

    @Autowired
    private DraftService draftService;

    @Operation(summary = "保存草稿", description = "保存用户的草稿数据到Redis")
    @PostMapping("/save")
    public Result<Boolean> saveDraft(
            @Parameter(description = "草稿类型", required = true)
            @RequestParam String draftType,
            @Parameter(description = "草稿内容", required = true)
            @RequestBody Object draftData,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        boolean success = draftService.saveDraft(userId, draftType, draftData);
        
        if (success) {
            return Result.success("草稿保存成功", true);
        } else {
            return Result.error("草稿保存失败");
        }
    }

    @Operation(summary = "获取草稿", description = "从Redis获取用户的草稿数据")
    @GetMapping("/get")
    public Result<Object> getDraft(
            @Parameter(description = "草稿类型", required = true)
            @RequestParam String draftType,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Object draft = draftService.getDraft(userId, draftType);
        return Result.success("草稿获取成功", draft);
    }

    @Operation(summary = "删除草稿", description = "删除用户的特定类型草稿")
    @DeleteMapping("/delete")
    public Result<Boolean> deleteDraft(
            @Parameter(description = "草稿类型", required = true)
            @RequestParam String draftType,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        boolean success = draftService.deleteDraft(userId, draftType);
        
        if (success) {
            return Result.success("草稿删除成功", true);
        } else {
            return Result.error("草稿删除失败");
        }
    }

    @Operation(summary = "清空用户所有草稿", description = "清空用户的所有草稿数据")
    @DeleteMapping("/clear")
    public Result<Boolean> clearAllDrafts(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        boolean success = draftService.clearAllDrafts(userId);
        
        if (success) {
            return Result.success("所有草稿已清空", true);
        } else {
            return Result.error("草稿清空失败");
        }
    }
}