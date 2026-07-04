package com.house.housing.service.impl;

import com.house.housing.dto.request.*;
import com.house.housing.dto.response.*;
import com.house.housing.vo.*;
import com.house.housing.common.Result;
import com.house.housing.entity.User;
import com.house.housing.enums.UserRoleEnum;
import com.house.housing.enums.UserStatusEnum;
import com.house.housing.mapper.UserMapper;
import com.house.housing.service.UserService;
import com.house.housing.util.JwtUtil;
import com.house.housing.util.PasswordUtil;
import com.house.housing.vo.LoginVO;
import com.house.housing.vo.PageResult;
import com.house.housing.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    public UserServiceImpl(UserMapper userMapper, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    // ========== 认证相关 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<UserResponseDTO> register(RegisterRequestDTO request) {
        // 检查用户名是否已存在
        User existUser = userMapper.selectByUsername(request.getUsername());
        if (existUser != null) {
            return Result.error("用户名已存在");
        }

        // 检查手机号是否已注册
        User existPhone = userMapper.selectByPhone(request.getPhone());
        if (existPhone != null) {
            return Result.error("手机号已注册");
        }

        // 检查角色是否合法（仅允许租客和房东注册）
        if (!request.getRole().equals(UserRoleEnum.TENANT.getCode())
                && !request.getRole().equals(UserRoleEnum.LANDLORD.getCode())) {
            return Result.error("只能注册租客或房东账号");
        }

        // 创建用户
        User user = new User();
        BeanUtils.copyProperties(request, user);
        user.setPassword(PasswordUtil.encode(request.getPassword()));
        user.setStatus(UserStatusEnum.NORMAL.getCode());

        int result = userMapper.insert(user);
        if (result <= 0) {
            return Result.error("注册失败");
        }

        return Result.success("注册成功", convertToResponseDTO(user));
    }

    @Override
    public Result<LoginVO> login(LoginRequestDTO request) {
        // 查询用户
        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            return Result.error("用户名或密码错误");
        }

        // 校验密码
        if (!PasswordUtil.matches(request.getPassword(), user.getPassword())) {
            return Result.error("用户名或密码错误");
        }

        // 检查账号状态
        if (user.getStatus().equals(UserStatusEnum.DISABLED.getCode())) {
            return Result.error("账号已被禁用，请联系管理员");
        }

        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        UserResponseDTO userInfo = convertToResponseDTO(user);
        LoginVO loginVO = new LoginVO(token, refreshToken, userInfo);

        return Result.success("登录成功", loginVO);
    }

    @Override
    public Result<LoginVO> refreshToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            return Result.error("Refresh Token无效或已过期");
        }

        Integer userId = jwtUtil.getUserIdFromToken(refreshToken);
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if (user.getStatus().equals(UserStatusEnum.DISABLED.getCode())) {
            return Result.error("账号已被禁用");
        }

        // 生成新Token
        String newToken = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId());

        UserResponseDTO userInfo = convertToResponseDTO(user);
        LoginVO loginVO = new LoginVO(newToken, newRefreshToken, userInfo);

        return Result.success("刷新成功", loginVO);
    }

    @Override
    public Result<Void> logout(Integer userId) {
        // 可以存入黑名单或清除Redis缓存
        log.info("用户 {} 退出登录", userId);
        return Result.success("退出成功", null);
    }

    // ========== 用户信息相关 ==========

    @Override
    public Result<UserResponseDTO> getUserInfo(Integer userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(convertToResponseDTO(user));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<UserResponseDTO> updateUserInfo(Integer userId, UserUpdateDTO request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 更新字段
        if (StringUtils.hasText(request.getRealName())) {
            user.setRealName(request.getRealName());
        }
        if (StringUtils.hasText(request.getPhone())) {
            // 检查手机号是否被其他用户占用
            User existPhone = userMapper.selectByPhone(request.getPhone());
            if (existPhone != null && !existPhone.getId().equals(userId)) {
                return Result.error("手机号已被其他用户使用");
            }
            user.setPhone(request.getPhone());
        }

        user.setUpdatedAt(LocalDateTime.now());
        int result = userMapper.updateById(user);
        if (result <= 0) {
            return Result.error("更新失败");
        }

        return Result.success("更新成功", convertToResponseDTO(user));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> changePassword(Integer userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 校验旧密码
        if (!PasswordUtil.matches(oldPassword, user.getPassword())) {
            return Result.error("原密码错误");
        }

        // 校验新密码强度
        if (!PasswordUtil.isValidPassword(newPassword)) {
            return Result.error("密码长度为6-20位");
        }

        // 更新密码
        user.setPassword(PasswordUtil.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        int result = userMapper.updateById(user);

        if (result <= 0) {
            return Result.error("密码修改失败");
        }

        return Result.success("密码修改成功", true);
    }

    // ========== 管理员相关 ==========

    @Override
    public Result<PageResult<UserResponseDTO>> getUserList(UserQueryDTO query) {
        List<User> users = userMapper.selectUserList(query);
        Long total = userMapper.countUserList(query);

        List<UserResponseDTO> records = users.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        PageResult<UserResponseDTO> pageResult = new PageResult<>(records, total, query.getPage(), query.getLimit());
        return Result.success(pageResult);
    }

    @Override
    public Result<UserResponseDTO> getUserDetail(Integer userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(convertToResponseDTO(user));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> updateUserStatus(Integer userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        if (!status.equals(UserStatusEnum.NORMAL.getCode())
                && !status.equals(UserStatusEnum.DISABLED.getCode())) {
            return Result.error("状态值不正确");
        }

        int result = userMapper.updateStatus(userId, status);
        if (result <= 0) {
            return Result.error("操作失败");
        }

        String message = status.equals(UserStatusEnum.NORMAL.getCode()) ? "已启用" : "已禁用";
        return Result.success(message, true);
    }

    @Override
    public Result<UserStatisticsVO> getStatistics() {
        // 统计总用户数
        Long totalUsers = userMapper.selectCount(null);

        // 统计各角色数量
        List<RoleCountVO> roleCounts = userMapper.countByRole();
        Long tenantCount = 0L, landlordCount = 0L, adminCount = 0L;
        for (RoleCountVO rc : roleCounts) {
            if (rc.getRole().equals(UserRoleEnum.TENANT.getCode())) {
                tenantCount = rc.getCount();
            } else if (rc.getRole().equals(UserRoleEnum.LANDLORD.getCode())) {
                landlordCount = rc.getCount();
            } else if (rc.getRole().equals(UserRoleEnum.ADMIN.getCode())) {
                adminCount = rc.getCount();
            }
        }

        // 今日新增
        int todayNew = userMapper.countTodayNewUsers();

        UserStatisticsVO statistics = UserStatisticsVO.builder()
                .totalUsers(totalUsers)
                .tenantCount(tenantCount)
                .landlordCount(landlordCount)
                .adminCount(adminCount)
                .todayNewUsers(todayNew)
                .build();

        return Result.success(statistics);
    }

    // ========== 私有方法 ==========

    /**
     * 将 User 实体转换为 UserResponseDTO
     */
    private UserResponseDTO convertToResponseDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        BeanUtils.copyProperties(user, dto);
        dto.setRoleDesc(UserRoleEnum.getDescByCode(user.getRole()));
        dto.setStatusDesc(UserStatusEnum.getDescByCode(user.getStatus()));
        return dto;
    }
}