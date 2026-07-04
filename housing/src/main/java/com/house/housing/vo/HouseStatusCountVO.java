package com.house.housing.vo;

import lombok.Data;

/**
 * 房源状态统计 VO
 */
@Data
public class HouseStatusCountVO {
    private Integer status;
    private Long count;
}