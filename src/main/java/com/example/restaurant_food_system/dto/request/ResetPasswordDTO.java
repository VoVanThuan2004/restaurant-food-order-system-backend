package com.example.restaurant_food_system.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ResetPasswordDTO {
    @NotBlank(message = "Mã xác thực không hợp lệ")
    private String token;

    @NotBlank(message = "Vui lòng nhập mật khẩu")
    @Size(min = 8, max = 50, message = "Mật khẩu phải từ 8 đến 50 ký tự")
    private String newPassword;
}
