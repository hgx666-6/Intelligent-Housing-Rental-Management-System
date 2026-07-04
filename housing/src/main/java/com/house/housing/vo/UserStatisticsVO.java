package com.house.housing.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatisticsVO {
    private Long totalUsers;
    private Long tenantCount;
    private Long landlordCount;
    private Long adminCount;
    private Integer todayNewUsers;
}