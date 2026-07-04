package com.house.housing.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Data
public class HouseRequestDTO {

    private Integer id;  // 编辑时传入

    @NotBlank(message = "标题不能为空")
    private String title;

    private String description;

    @NotBlank(message = "地址不能为空")
    private String address;

    @NotBlank(message = "城市不能为空")
    private String city;

    private String district;

    @NotNull(message = "租金不能为空")
    private BigDecimal rentPrice;

    @NotBlank(message = "户型不能为空")
    private String houseType;

    private BigDecimal area;

    private List<String> facilities;  // 前端传数组，后端转JSON

    private List<String> images;      // 前端传数组，后端转JSON
}