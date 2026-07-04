package com.house.housing.enums;

import lombok.Getter;

/**
 * 用户角色枚举
 */
@Getter
public enum UserRoleEnum {

    TENANT(1, "租客"),
    LANDLORD(2, "房东"),
    ADMIN(3, "管理员");

    private final Integer code;
    private final String desc;

    UserRoleEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据 code 获取描述
     */
    public static String getDescByCode(Integer code) {
        if (code == null) {
            return "未知";
        }
        for (UserRoleEnum role : values()) {
            if (role.code.equals(code)) {
                return role.desc;
            }
        }
        return "未知";
    }

    /**
     * 根据 code 获取枚举
     */
    public static UserRoleEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (UserRoleEnum role : values()) {
            if (role.code.equals(code)) {
                return role;
            }
        }
        return null;
    }

    /**
     * 判断是否为租客
     */
    public static boolean isTenant(Integer code) {
        return TENANT.getCode().equals(code);
    }

    /**
     * 判断是否为房东
     */
    public static boolean isLandlord(Integer code) {
        return LANDLORD.getCode().equals(code);
    }

    /**
     * 判断是否为管理员
     */
    public static boolean isAdmin(Integer code) {
        return ADMIN.getCode().equals(code);
    }
}