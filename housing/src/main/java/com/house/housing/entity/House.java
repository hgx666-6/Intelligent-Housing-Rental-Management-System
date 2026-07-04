package com.house.housing.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("houses")
public class House {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("landlord_id")
    private Integer landlordId;

    private String title;
    private String description;
    private String address;
    private String city;
    private String district;

    @TableField("rent_price")
    private BigDecimal rentPrice;

    @TableField("house_type")
    private String houseType;

    private BigDecimal area;

    private String facilities;  // JSON字符串

    private String images;      // JSON字符串

    private Integer status;     // 0-待审核 1-已上架 2-已下架 3-已租出

    @TableField("view_count")
    private Integer viewCount;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // 关联字段（非数据库字段）
    @TableField(exist = false)
    private String landlordName;

    @TableField(exist = false)
    private String landlordPhone;
}