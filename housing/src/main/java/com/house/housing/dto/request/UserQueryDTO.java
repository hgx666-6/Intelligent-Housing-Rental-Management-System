package com.house.housing.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserQueryDTO {
    private Integer page;   // 页码，从1开始
    private Integer limit;  // 每页条数
    private Integer role;
    private Integer status;
    private String keyword;

    // 计算偏移量
    public Integer getOffset() {
        return (this.page - 1) * this.limit;
    }
}