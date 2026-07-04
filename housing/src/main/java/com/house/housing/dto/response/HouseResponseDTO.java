package com.house.housing.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 房源响应 DTO
 */
@Data
public class HouseResponseDTO {

    /**
     * 房源ID
     */
    private Integer id;

    /**
     * 房东ID
     */
    private Integer landlordId;

    /**
     * 房东姓名
     */
    private String landlordName;

    /**
     * 房东手机号
     */
    private String landlordPhone;

    /**
     * 房源标题
     */
    private String title;

    /**
     * 房源描述
     */
    private String description;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 城市
     */
    private String city;

    /**
     * 区域
     */
    private String district;

    /**
     * 月租金
     */
    private BigDecimal rentPrice;

    /**
     * 户型
     */
    private String houseType;

    /**
     * 面积（平方米）
     */
    private BigDecimal area;

    /**
     * 配套设施列表
     */
    private List<String> facilities;

    /**
     * 图片列表
     */
    private List<String> images;

    /**
     * 封面图（取images第一张）
     */
    private String coverImage;

    /**
     * 状态：0-待审核，1-已上架，2-已下架，3-已租出
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 获取封面图（取images第一张）
     */
    public String getCoverImage() {
        if (images != null && !images.isEmpty()) {
            return images.get(0);
        }
        return null;
    }

    /**
     * 判断是否为已上架
     */
    public boolean isOnline() {
        return status != null && status == 1;
    }

    /**
     * 判断是否为待审核
     */
    public boolean isPending() {
        return status != null && status == 0;
    }

    /**
     * 判断是否为已下架
     */
    public boolean isOffline() {
        return status != null && status == 2;
    }

    /**
     * 判断是否为已租出
     */
    public boolean isRented() {
        return status != null && status == 3;
    }
}