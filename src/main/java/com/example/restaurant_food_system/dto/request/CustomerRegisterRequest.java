package com.example.restaurant_food_system.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class CustomerRegisterRequest {
    @NotBlank(message = "Vui lòng nhập email")
    @Email(message = "Email không đúng định dạng")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;

    @NotBlank(message = "Vui lòng nhập mật khẩu")
    @Size(min = 8, max = 50, message = "Mật khẩu phải từ 8 đến 50 ký tự")
    private String password;

    @NotBlank(message = "Vui lòng xác nhận mật khẩu")
    @Size(min = 8, max = 50, message = "Mật khẩu xác nhận phải từ 8 đến 50 ký tự")
    private String confirmPassword;

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
    private String fullName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(
            regexp = "^(0|\\+84)[0-9]{9}$",
            message = "Số điện thoại không hợp lệ"
    )
    private String phoneNumber;

    private Integer gender;
}
