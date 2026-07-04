package com.house.housing.service;

import com.house.housing.dto.request.*;
import com.house.housing.dto.response.*;
import com.house.housing.vo.*;
import com.house.housing.common.Result;

public interface UserService {

    // ========== 认证相关 ==========

    /**
     * 用户注册
     */
    Result<UserResponseDTO> register(RegisterRequestDTO request);

    /**
     * 用户登录
     */
    Result<LoginVO> login(LoginRequestDTO request);

    /**
     * 刷新Token
     */
    Result<LoginVO> refreshToken(String refreshToken);

    /**
     * 退出登录
     */
    Result<Void> logout(Integer userId);

    // ========== 用户信息相关 ==========

    /**
     * 获取当前用户信息
     */
    Result<UserResponseDTO> getUserInfo(Integer userId);

    /**
     * 更新用户信息
     */
    Result<UserResponseDTO> updateUserInfo(Integer userId, UserUpdateDTO request);

    /**
     * 修改密码
     */
    Result<Boolean> changePassword(Integer userId, String oldPassword, String newPassword);

    // ========== 管理员相关 ==========

    /**
     * 获取用户列表（管理员）
     */
    Result<PageResult<UserResponseDTO>> getUserList(UserQueryDTO query);

    /**
     * 获取用户详情（管理员）
     */
    Result<UserResponseDTO> getUserDetail(Integer userId);

    /**
     * 禁用/启用用户（管理员）
     */
    Result<Boolean> updateUserStatus(Integer userId, Integer status);

    /**
     * 获取统计数据（管理员）
     */
    Result<UserStatisticsVO> getStatistics();
}