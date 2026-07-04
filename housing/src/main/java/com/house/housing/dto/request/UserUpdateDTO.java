package com.house.housing.dto.request;

import lombok.Data;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 用户更新请求 DTO
 */
@Data
public class UserUpdateDTO {

    private String realName;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    private String oldPassword;

    @Size(min = 6, max = 20, message = "密码长度为 6-20 位")
    private String newPassword;
}