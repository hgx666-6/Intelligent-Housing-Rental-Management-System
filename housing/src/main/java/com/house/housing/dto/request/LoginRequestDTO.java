package com.house.housing.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求 DTO
 */
@Data
public class LoginRequestDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}