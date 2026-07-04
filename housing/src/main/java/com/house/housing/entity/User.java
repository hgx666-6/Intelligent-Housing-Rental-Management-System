package com.house.housing.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.house.housing.enums.UserRoleEnum;
import com.house.housing.enums.UserStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@TableName("users")
public class User {

    /**
     * 用户ID（自增主键）
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 用户名（登录用，唯一）
     */
    private String username;

    /**
     * 加密密码（BCrypt）
     */
    private String password;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 真实姓名
     */
    @TableField("real_name")
    private String realName;

    /**
     * 角色：1-租客，2-房东，3-管理员
     */
    private Integer role;

    /**
     * 状态：1-正常，0-禁用
     */
    private Integer status;

    /**
     * 创建时间（自动填充）
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间（自动填充）
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // ========== 辅助方法（非数据库字段） ==========

    /**
     * 获取角色描述
     */
    
    public String getRoleDesc() {
        return UserRoleEnum.getDescByCode(this.role);
    }

    /**
     * 获取状态描述
     */
    
    public String getStatusDesc() {
        return UserStatusEnum.getDescByCode(this.status);
    }

    /**
     * 是否为管理员
     */
    
    public boolean isAdmin() {
        return UserRoleEnum.isAdmin(this.role);
    }

    /**
     * 是否为房东
     */
    
    public boolean isLandlord() {
        return UserRoleEnum.isLandlord(this.role);
    }

    /**
     * 是否为租客
     */
    
    public boolean isTenant() {
        return UserRoleEnum.isTenant(this.role);
    }

    /**
     * 账号是否正常
     */
    
    public boolean isNormal() {
        return UserStatusEnum.isNormal(this.status);
    }

    /**
     * 账号是否禁用
     */
    
    public boolean isDisabled() {
        return UserStatusEnum.isDisabled(this.status);
    }

    /**
     * 账号是否有效（正常且未禁用）
     */
    public boolean isValid() {
        return this.status != null && this.status.equals(UserStatusEnum.NORMAL.getCode());
    }
}