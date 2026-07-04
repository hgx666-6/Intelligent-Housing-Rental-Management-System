package com.house.housing.enums;

import lombok.Getter;

/**
 * 用户状态枚举
 */
@Getter
public enum UserStatusEnum {

    DISABLED(0, "禁用"),
    NORMAL(1, "正常");

    private final Integer code;
    private final String desc;

    UserStatusEnum(Integer code, String desc) {
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
        for (UserStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status.desc;
            }
        }
        return "未知";
    }

    /**
     * 根据 code 获取枚举
     */
    public static UserStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (UserStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断是否为正常状态
     */
    public static boolean isNormal(Integer code) {
        return NORMAL.getCode().equals(code);
    }

    /**
     * 判断是否为禁用状态
     */
    public static boolean isDisabled(Integer code) {
        return DISABLED.getCode().equals(code);
    }
}