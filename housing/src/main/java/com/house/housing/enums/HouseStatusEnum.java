package com.house.housing.enums;

import lombok.Getter;

import java.util.*;

/**
 * 房源状态枚举
 */
@Getter
public enum HouseStatusEnum {

    PENDING(0, "待审核"),
    ONLINE(1, "已上架"),
    OFFLINE(2, "已下架"),
    RENTED(3, "已租出");

    private final Integer code;
    private final String desc;

    HouseStatusEnum(Integer code, String desc) {
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
        for (HouseStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status.desc;
            }
        }
        return "未知";
    }

    /**
     * 根据 code 获取枚举
     */
    public static HouseStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (HouseStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断是否为待审核
     */
    public static boolean isPending(Integer code) {
        return PENDING.getCode().equals(code);
    }

    /**
     * 判断是否为已上架
     */
    public static boolean isOnline(Integer code) {
        return ONLINE.getCode().equals(code);
    }

    /**
     * 判断是否为已下架
     */
    public static boolean isOffline(Integer code) {
        return OFFLINE.getCode().equals(code);
    }

    /**
     * 判断是否为已租出
     */
    public static boolean isRented(Integer code) {
        return RENTED.getCode().equals(code);
    }

    /**
     * 是否可编辑（待审核或已下架可编辑）
     */
    public static boolean isEditable(Integer code) {
        return PENDING.getCode().equals(code) || OFFLINE.getCode().equals(code);
    }

    /**
     * 是否可预约看房（已上架状态可预约）
     */
    public static boolean isBookable(Integer code) {
        return ONLINE.getCode().equals(code);
    }

    /**
     * 是否可审核（只有待审核状态可审核）
     */
    public static boolean isApprovable(Integer code) {
        return PENDING.getCode().equals(code);
    }

    /**
     * 是否已上架或已租出（对外可见）
     */
    public static boolean isVisible(Integer code) {
        return ONLINE.getCode().equals(code) || RENTED.getCode().equals(code);
    }

    /**
     * 是否有效状态（非待审核）
     */
    public static boolean isValidStatus(Integer code) {
        return !PENDING.getCode().equals(code);
    }

    /**
     * 获取前台展示的状态列表（排除待审核）
     */
    public static List<Integer> getDisplayStatusList() {
        return Arrays.asList(ONLINE.getCode(), OFFLINE.getCode(), RENTED.getCode());
    }
}