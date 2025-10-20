package org.isoft.panelsurvey.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.isoft.panelsurvey.common.Result;
import org.isoft.panelsurvey.service.UserService;
import org.isoft.panelsurvey.vo.UserVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户管理", description = "用户信息查询、更新等操作")
@RestController
@RequestMapping("/api/user")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Operation(summary = "获取用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/info")
    public Result<UserVO> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        UserVO userInfo = userService.getUserInfo(userId);
        return Result.success(userInfo);
    }

    @Operation(summary = "获取所有用户", description = "获取系统中所有用户的列表")
    @GetMapping("/list")
    public Result<List<UserVO>> getAllUsers() {
        List<UserVO> users = userService.getAllUsers();
        return Result.success(users);
    }

    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户详细信息")
    @GetMapping("/{id}")
    public Result<UserVO> getUserById(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long id) {
        UserVO user = userService.getUserById(id);
        return Result.success(user);
    }

    @Operation(summary = "更新用户状态", description = "更新指定用户的状态")
    @PutMapping("/{id}/status")
    public Result<String> updateUserStatus(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "状态值", required = true)
            @RequestParam Integer status) {
        userService.updateStatus(id, status);
        return Result.success("用户状态更新成功", null);
    }

    @Operation(summary = "删除用户", description = "删除指定的用户")
    @DeleteMapping("/{id}")
    public Result<String> deleteUser(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success("用户删除成功", null);
    }
}