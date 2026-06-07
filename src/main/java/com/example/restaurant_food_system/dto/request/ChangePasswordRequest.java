package com.example.restaurant_food_system.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest {
    @NotBlank(message = "Mật khẩu hiện tại đang trống")
    @Size(min = 8, max = 72, message = "Mật khẩu hiện tại phải từ 8 đến 72 ký tự")
    private String oldPassword;

    @NotBlank(message = "Mật khẩu mới đang trống")
    @Size(min = 8, max = 72, message = "Mật khẩu mới phải từ 8 đến 72 ký tự")
    private String newPassword;

    @NotBlank(message = "Mật khẩu xác nhận đang trống")
    @Size(min = 8, max = 72, message = "Mật khẩu xác nhận phải từ 8 đến 72 ký tự")
    private String confirmNewPassword;
}
