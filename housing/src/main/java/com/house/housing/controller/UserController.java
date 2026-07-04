package com.house.housing.controller;

import com.house.housing.dto.request.*;
import com.house.housing.dto.response.*;
import com.house.housing.service.UserService;
import com.house.housing.vo.PageResult;
import com.house.housing.common.Result;
import com.house.housing.vo.UserStatisticsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
@Validated
public class UserController {

    @Autowired
    private UserService userService;

    // ========== 用户个人信息（从请求上下文获取用户信息） ==========

    /**
     * 获取当前用户信息
     */

    @GetMapping("/me")
    public Result<UserResponseDTO> getCurrentUser(HttpServletRequest request) {
        Integer userId = getCurrentUserId(request);
        // Service 已经返回 Result，直接返回
        return userService.getUserInfo(userId);
    }

    /**
     * 更新当前用户信息
     */
    @PutMapping("/me")
    public Result<UserResponseDTO> updateCurrentUser(
            @Valid @RequestBody UserUpdateDTO request,
            HttpServletRequest req) {
        Integer userId = getCurrentUserId(req);
        // Service 已经返回 Result，直接返回
        return userService.updateUserInfo(userId, request);
    }

    /**
     * 修改密码
     */
    @PutMapping("/me/password")
    public Result<Boolean> changePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            HttpServletRequest req) {
        Integer userId = getCurrentUserId(req);
        // Service 已经返回 Result，直接返回
        return userService.changePassword(userId, oldPassword, newPassword);
    }

    // ========== 管理员接口 ==========

    /**
     * 获取用户列表（管理员）
     */
    @GetMapping
    public Result<PageResult<UserResponseDTO>> getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) Integer role,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            HttpServletRequest req) {

        UserQueryDTO query = UserQueryDTO.builder()
                .page(page)
                .limit(limit)
                .role(role)
                .status(status)
                .keyword(keyword)
                .build();

        // Service 已经返回 Result，直接返回
        return userService.getUserList(query);
    }

    /**
     * 获取用户详情（管理员）
     */
    @GetMapping("/{userId}")
    public Result<UserResponseDTO> getUserDetail(
            @PathVariable Integer userId,
            HttpServletRequest req) {
        validateAdmin(req);
        // Service 已经返回 Result，直接返回
        return userService.getUserDetail(userId);
    }

    /**
     * 禁用/启用用户（管理员）
     */
    @PutMapping("/{userId}/status")
    public Result<Boolean> updateUserStatus(
            @PathVariable Integer userId,
            @RequestParam Integer status,
            HttpServletRequest req) {
        validateAdmin(req);
        // Service 已经返回 Result，直接返回
        return userService.updateUserStatus(userId, status);
    }

    /**
     * 获取用户统计数据（管理员）
     */
    @GetMapping("/statistics")
    public Result<UserStatisticsVO> getStatistics(HttpServletRequest req) {
        validateAdmin(req);
        // Service 已经返回 Result，直接返回
        return userService.getStatistics();
    }

    // ========== 私有辅助方法（从请求上下文获取） ==========

    /**
     * 从请求上下文中获取当前用户ID
     */
    private Integer getCurrentUserId(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        return userId;
    }

    /**
     * 从请求上下文中获取当前用户角色
     */
    private Integer getCurrentUserRole(HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        if (role == null) {
            throw new RuntimeException("用户未登录");
        }
        return role;
    }

    /**
     * 验证是否为管理员
     */
    private void validateAdmin(HttpServletRequest request) {
        Integer role = getCurrentUserRole(request);
        if (role == null || role != 3) {
            throw new RuntimeException("权限不足，需要管理员身份");
        }
    }

    /**
     * 验证是否为房东
     */
    private void validateLandlord(HttpServletRequest request) {
        Integer role = getCurrentUserRole(request);
        if (role == null || role != 2) {
            throw new RuntimeException("权限不足，需要房东身份");
        }
    }
}