package com.house.housing.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserResponseDTO {
    private Integer id;
    private String username;
    private String phone;
    private String realName;
    private Integer role;
    private String roleDesc;      // 租客/房东/管理员
    private Integer status;
    private String statusDesc;    // 正常/禁用
    private LocalDateTime createdAt;
}