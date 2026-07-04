package com.house.housing.controller;

import com.house.housing.dto.request.*;
import com.house.housing.dto.response.*;
import com.house.housing.service.UserService;
import com.house.housing.vo.*;
import com.house.housing.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@Validated
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 用户注册（租客/房东）
     */
    @PostMapping("/register")
    public Result<UserResponseDTO> register(
            @Valid @RequestBody RegisterRequestDTO request) {
        // Service 已经返回 Result，直接返回即可
        return userService.register(request);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginVO> login(
            @Valid @RequestBody LoginRequestDTO request) {
        // Service 已经返回 Result，直接返回即可
        return userService.login(request);
    }

    /**
     * 刷新 Token
     */
    @PostMapping("/refresh")
    public Result<LoginVO> refreshToken(
            @RequestHeader("Authorization") String authorization) {
        // 提取 Refresh Token
        String refreshToken = extractToken(authorization);
        return userService.refreshToken(refreshToken);
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        // 由拦截器统一处理，这里无需再验证
        // 可以清除缓存等操作
        return Result.success("退出成功", null);
    }

    /**
     * 提取 Token
     */
    private String extractToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new RuntimeException("Token格式错误");
        }
        return authorization.substring(7);
    }
}