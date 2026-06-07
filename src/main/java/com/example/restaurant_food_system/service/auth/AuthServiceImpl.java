package com.example.restaurant_food_system.service.auth;

import com.example.restaurant_food_system.dto.request.ChangePasswordRequest;
import com.example.restaurant_food_system.dto.request.ForgotPasswordDTO;
import com.example.restaurant_food_system.dto.request.LoginRequest;
import com.example.restaurant_food_system.dto.request.ResetPasswordDTO;
import com.example.restaurant_food_system.dto.response.LoginResponse;
import com.example.restaurant_food_system.dto.response.RoleResponse;
import com.example.restaurant_food_system.entity.PasswordResetToken;
import com.example.restaurant_food_system.entity.RefreshToken;
import com.example.restaurant_food_system.entity.Role;
import com.example.restaurant_food_system.entity.User;
import com.example.restaurant_food_system.exception.BadRequestException;
import com.example.restaurant_food_system.exception.ResourceNotFoundException;
import com.example.restaurant_food_system.repository.PasswordResetTokenRepository;
import com.example.restaurant_food_system.repository.RefreshTokenRepository;
import com.example.restaurant_food_system.repository.UserRepository;
import com.example.restaurant_food_system.security.TokenPayload;
import com.example.restaurant_food_system.service.EmailService;
import com.example.restaurant_food_system.utils.JwtTokenUtil;
import com.example.restaurant_food_system.utils.TokenConstant;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    @Value("${fe_url}")
    private String feUrl;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest loginRequest,
                               HttpServletRequest httpServletRequest,
                               HttpServletResponse httpServletResponse) {
        // 1. Kiểm người dùng có tồn tại
        Optional<User> user = userRepository.findByEmail(loginRequest.getEmail());
        if (user.isEmpty()) {
            throw new ResourceNotFoundException("Email không hợp lệ");
        }

        // Kiểm tra tài khoản có bị khóa hay không
        if (!user.get().isActive()) {
            throw new BadRequestException("Tài khoản đã bị khóa");
        }

        // 2. Kiểm tra password
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.get().getPassword())) {
            throw new BadRequestException("Mật khẩu không hợp lệ");
        }

        // 3. Lấy danh sách vai trò của người dùng
        List<String> roles = user.get().getRoles().stream()
                .map(Role::getRoleName)
                .toList();

        // 4. Tạo mã access, refresh token
        String accessToken = jwtTokenUtil.generateToken(TokenPayload.builder()
                .userId(user.get().getUserId())
                .fullName(loginRequest.getEmail())
                .roles(user.get().getRoles().stream()
                        .map(Role::getRoleName)
                        .toList()
                )
                .build(), TokenConstant.ACCESS_TOKEN_EXPIRATION);

        String refreshToken = jwtTokenUtil.generateToken(TokenPayload.builder()
                .userId(user.get().getUserId())
                .fullName(loginRequest.getEmail())
                .roles(user.get().getRoles().stream()
                        .map(Role::getRoleName)
                        .toList()
                )
                .build(), TokenConstant.REFRESH_TOKEN_EXPIRATION);

        // 5. Lưu refresh token xuống DB
        refreshTokenRepository.save(RefreshToken.builder()
                        .refreshToken(DigestUtils.sha256Hex(refreshToken))
                        .userAgent(httpServletRequest.getHeader("user-agent"))
                        .ipAddress(httpServletRequest.getRemoteAddr())
                        .expiredAt(Instant.now().plus(Duration.ofSeconds(TokenConstant.REFRESH_TOKEN_EXPIRATION)))
                        .user(user.get())
                .build());

        // 6. Set refresh token vào cookie response
        addRefreshTokenCookie(httpServletResponse, refreshToken);

        return LoginResponse.builder()
                .userId(user.get().getUserId())
                .fullName(user.get().getFullName())
                .avatarUrl(user.get().getAvatarUrl())
                .roles(roles)
                .accessToken(accessToken)
                .build();
    }

    @Override
    public void logout(String refreshToken, HttpServletResponse response) {
        // 1. Nếu không có refresh token -> throw lỗi
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new BadRequestException("Refresh token không hợp lệ");
        }

        // 2. Kiểm tra mã refresh token
        Optional<RefreshToken> refreshTokenExist = refreshTokenRepository.findByRefreshToken(DigestUtils.sha256Hex(refreshToken));
        if (refreshTokenExist.isEmpty()) {
            throw new ResourceNotFoundException("Mã refresh token không tồn tại");
        }

        // 3. Kiểm tra mã refresh token có đúng của user
        String userId = jwtTokenUtil.getTokenPayload(refreshToken).getUserId();
        if (!userId.equals(refreshTokenExist.get().getUser().getUserId())) {
            throw new BadRequestException("Người dùng không hợp lệ");
        }

        // 4. Xóa refresh token ra khỏi cookie response
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)  // set maxAge=0 để xóa cookie
                .sameSite("Lax")
                .build();
        response.setHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());

        // 2. Xóa mã refresh token
        refreshTokenRepository.delete(refreshTokenExist.get());
    }

    @Override
    public void changePassword(String userId, ChangePasswordRequest changePasswordRequest) {
        // Kiểm tra người dùng
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new ResourceNotFoundException("Người dùng không tồn tại");
        }

        // Kiểm tra mật khẩu hiện tại có khác mật khẩu mới
        if (changePasswordRequest.getOldPassword().equals(changePasswordRequest.getNewPassword())) {
            throw new BadRequestException("Mật khẩu mới đang trùng với mật khẩu hiện tại");
        }

        // So sánh mật khẩu mới có khớp với mật khẩu xác nhận
        if (!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmNewPassword())) {
            throw new BadRequestException("Mật khẩu mới và mật khẩu xác nhận không khớp");
        }

        // Kiểm tra mật khẩu hiện tại có đúng
        if (!passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.get().getPassword())) {
            throw new BadRequestException("Mật khẩu hiện tại không đúng");
        }

        user.get().setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        userRepository.save(user.get());
    }

    @Override
    public void forgotPassword(ForgotPasswordDTO forgotPasswordDTO, HttpServletRequest request) {
        // 1. Kiểm tra email
        User userExist = userRepository.findByEmail(forgotPasswordDTO.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Email người dùng không tồn tại"));

        // 2. Tạo mã token
        String token = generateTokenWithUUID();

        // 3. Lưu thông tin password reset token
        PasswordResetToken passwordResetToken = PasswordResetToken.builder()
                .user(userExist)
                .tokenHash(DigestUtils.sha256Hex(token))
                .expiredAt(Instant.now().plusMillis(TokenConstant.RESET_TOKEN_PASSWORD_EXPIRATION * 1000))
                .used(false)
                .userAgent(request.getHeader("user-agent"))
                .ipAddress(request.getRemoteAddr())
                .build();
        passwordResetTokenRepository.save(passwordResetToken);

        // 4. Gửi mail qua user gồm link reset mật khẩu
        sendResetPasswordEmail(userExist.getEmail(), token);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordDTO resetPasswordDTO) {
        // 1. Tìm mã token này có tồn tại
        PasswordResetToken token = passwordResetTokenRepository
                .findByTokenHash(DigestUtils.sha256Hex(resetPasswordDTO.getToken()))
                .orElseThrow(() -> new ResourceNotFoundException("Mã xác thực không tồn tại"));

        // 2. Kiểm tra thời gian hết hạn, đã sử dụng chưa
        if (token.isUsed() || !token.getExpiredAt().isAfter(Instant.now())) {
            throw new BadRequestException("Mã xác thực đã hết hạn hoặc đã sử dụng");
        }

        // 3. Set mật khẩu mới
        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(resetPasswordDTO.getNewPassword()));
        userRepository.save(user);

        // 4. Xóa tất cả refresh token của user
        refreshTokenRepository.deleteAllByUserId(user.getUserId());

        // 5. Cập nhật lại mã token thành đã sử dụng
        token.setUsed(true);
        passwordResetTokenRepository.save(token);
    }

    private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(TokenConstant.REFRESH_TOKEN_EXPIRATION)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String generateTokenWithUUID() {
        return UUID.randomUUID().toString() + "-" + System.currentTimeMillis();
    }


    /**
     * Gửi email reset password
     */
    private void sendResetPasswordEmail(String toEmail, String token) {
        String resetLink = feUrl + "/reset-password?token" + token;

        String subject = "Yêu cầu đặt lại mật khẩu";
        String content = buildEmailContent(resetLink);

        emailService.sendEmailResetPassword(toEmail, subject, content);
    }

    /**
     * Nội dung email (HTML)
     */
    private String buildEmailContent(String resetLink) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .btn {
                        background-color: #4CAF50;
                        color: white !important;
                        padding: 12px 24px;
                        text-decoration: none;
                        border-radius: 5px;
                        display: inline-block;
                    }
                    .btn:hover { background-color: #45a049; }
                    .warning { color: #ff0000; font-size: 12px; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h2>Đặt lại mật khẩu</h2>
                    <p>Bạn vừa yêu cầu đặt lại mật khẩu cho tài khoản của mình.</p>
                    <p>Nhấp vào nút bên dưới để đặt lại mật khẩu:</p>
                    <p>
                        <a href="%s" class="btn">Đặt lại mật khẩu</a>
                    </p>
                    <p>Hoặc copy link sau vào trình duyệt:</p>
                    <p style="background-color: #f4f4f4; padding: 10px; word-break: break-all;">%s</p>
                    <div class="warning">
                        <strong>Lưu ý:</strong> Link này sẽ hết hạn sau 5 phút.
                        Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.
                    </div>
                </div>
            </body>
            </html>
        """.formatted(resetLink, resetLink);
    }
}
