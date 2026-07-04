package com.house.housing.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HouseQueryDTO {
    private Integer page = 1;
    private Integer limit = 10;
    private String city;
    private String district;
    private Double minPrice;
    private Double maxPrice;
    private String houseType;
    private String keyword;
    private Integer status;
    private Integer landlordId;
}