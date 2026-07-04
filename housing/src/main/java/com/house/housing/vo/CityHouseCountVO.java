package com.house.housing.vo;

import lombok.Data;

/**
 * 城市房源统计 VO
 */
@Data
public class CityHouseCountVO {
    private String city;
    private Long count;
}