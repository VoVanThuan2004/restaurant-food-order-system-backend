package com.example.restaurant_food_system.service.auth;

import com.example.restaurant_food_system.dto.request.ChangePasswordRequest;
import com.example.restaurant_food_system.dto.request.CustomerRegisterRequest;
import com.example.restaurant_food_system.dto.request.ForgotPasswordDTO;
import com.example.restaurant_food_system.dto.request.LoginRequest;
import com.example.restaurant_food_system.dto.request.ResendVerificationRequest;
import com.example.restaurant_food_system.dto.request.ResetPasswordDTO;
import com.example.restaurant_food_system.dto.request.VerifyEmailRequest;
import com.example.restaurant_food_system.dto.response.LoginResponse;
import com.example.restaurant_food_system.dto.response.RegisterResponse;
import com.example.restaurant_food_system.dto.response.RoleResponse;
import com.example.restaurant_food_system.entity.EmailVerificationToken;
import com.example.restaurant_food_system.entity.PasswordResetToken;
import com.example.restaurant_food_system.entity.RefreshToken;
import com.example.restaurant_food_system.entity.Role;
import com.example.restaurant_food_system.entity.User;
import com.example.restaurant_food_system.exception.BadRequestException;
import com.example.restaurant_food_system.exception.ResourceNotFoundException;
import com.example.restaurant_food_system.repository.EmailVerificationTokenRepository;
import com.example.restaurant_food_system.repository.PasswordResetTokenRepository;
import com.example.restaurant_food_system.repository.RefreshTokenRepository;
import com.example.restaurant_food_system.repository.RoleRepository;
import com.example.restaurant_food_system.repository.UserRepository;
import com.example.restaurant_food_system.security.TokenPayload;
import com.example.restaurant_food_system.service.EmailService;
import com.example.restaurant_food_system.utils.EmailContent;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final EmailContent emailContent;

    @Value("${fe_url}")
    private String feUrl;

    @Value("${app.cookie.secure}")
    private Boolean cookieSecure;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest loginRequest,
                               HttpServletRequest httpServletRequest,
                               HttpServletResponse httpServletResponse) {
        // 1. Kiểm người dùng có tồn tại
        Optional<User> user = userRepository.findByEmailIgnoreCase(normalizeEmail(loginRequest.getEmail()));
        if (user.isEmpty()) {
            throw new ResourceNotFoundException("Email hoặc mật khẩu không chính xác");
        }

        // Kiểm tra tài khoản có bị khóa hay không
        if (!user.get().isActive()) {
            throw new BadRequestException("Tài khoản đã bị khóa");
        }

        // Kiểm tra tài khoản đã xác thực email hay chưa
        if (!user.get().isEmailVerified()) {
            throw new BadRequestException("Tài khoản chưa xác thực email. Vui lòng kiểm tra hộp thư hoặc gửi lại email xác thực");
        }

        // 2. Kiểm tra password
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.get().getPassword())) {
            throw new BadRequestException("Email hoặc mật khẩu không chính xác");
        }

        // 3. Lấy danh sách vai trò của người dùng
        List<String> roles = user.get().getRoles().stream()
                .map(Role::getRoleName)
                .toList();

        // 4. Tạo mã access, refresh token
        String accessToken = jwtTokenUtil.generateToken(TokenPayload.builder()
                .userId(user.get().getUserId())
                .fullName(user.get().getFullName())
                .roles(roles)
                .build(), TokenConstant.ACCESS_TOKEN_EXPIRATION);

        String refreshToken = jwtTokenUtil.generateToken(TokenPayload.builder()
                .userId(user.get().getUserId())
                .fullName(user.get().getFullName())
                .roles(roles)
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
                .secure(cookieSecure)
                .path("/api/v1/auth/")
                .maxAge(0)  // set maxAge=0 để xóa cookie
                .sameSite("None")
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
        User userExist = userRepository.findByEmailIgnoreCase(normalizeEmail(forgotPasswordDTO.getEmail()))
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

    @Override
    @Transactional
    public RegisterResponse register(CustomerRegisterRequest customerRegisterRequest,
                                    HttpServletRequest httpServletRequest) {
        String email = normalizeEmail(customerRegisterRequest.getEmail());

        // 1. Kiểm tra email đã tồn tại
        Optional<User> userExist = userRepository.findByEmailIgnoreCase(email);
        if (userExist.isPresent()) {
            if (userExist.get().isEmailVerified()) {
                throw new BadRequestException("Email đã được sử dụng");
            }
            throw new BadRequestException("Email đã được đăng ký nhưng chưa xác thực. Vui lòng gửi lại email xác thực");
        }

        // 2. Kiểm tra mật khẩu xác nhận
        if (!customerRegisterRequest.getPassword().equals(customerRegisterRequest.getConfirmPassword())) {
            throw new BadRequestException("Mật khẩu xác nhận không khớp");
        }

        // 3. Lấy vai trò CUSTOMER
        Role customerRole = roleRepository.findByRoleName("CUSTOMER")
                .orElseThrow(() -> new BadRequestException("Vai trò CUSTOMER chưa được cấu hình"));

        // 4. Tạo người dùng
        User user = User.builder()
                .email(email)
                .fullName(customerRegisterRequest.getFullName())
                .phoneNumber(customerRegisterRequest.getPhoneNumber())
                .gender(customerRegisterRequest.getGender())
                .password(passwordEncoder.encode(customerRegisterRequest.getPassword()))
                .isActive(true)
                .emailVerified(false)
                .roles(new HashSet<>(Set.of(customerRole)))
                .build();
        userRepository.save(user);

        // 5. Gửi email xác thực
        sendVerificationEmail(user, httpServletRequest);

        return RegisterResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(List.of(customerRole.getRoleName()))
                .build();
    }

    @Override
    @Transactional
    public void verifyEmail(VerifyEmailRequest verifyEmailRequest) {
        // 1. Tìm mã xác thực
        EmailVerificationToken verificationToken = emailVerificationTokenRepository
                .findByTokenHash(DigestUtils.sha256Hex(verifyEmailRequest.getToken()))
                .orElseThrow(() -> new ResourceNotFoundException("Mã xác thực không tồn tại"));

        User user = verificationToken.getUser();

        // 2. Nếu tài khoản đã xác thực thì trả về thành công (idempotent)
        if (user.isEmailVerified()) {
            if (!verificationToken.isUsed()) {
                verificationToken.setUsed(true);
                emailVerificationTokenRepository.save(verificationToken);
            }
            return;
        }

        // 3. Kiểm tra mã đã sử dụng hoặc hết hạn
        if (verificationToken.isUsed()) {
            throw new BadRequestException("Mã xác thực đã được sử dụng");
        }

        if (verificationToken.getExpiredAt().isBefore(Instant.now())) {
            throw new BadRequestException("Mã xác thực đã hết hạn. Vui lòng gửi lại email xác thực");
        }

        // 4. Cập nhật trạng thái xác thực email
        user.setEmailVerified(true);
        userRepository.save(user);

        verificationToken.setUsed(true);
        emailVerificationTokenRepository.save(verificationToken);
    }

    @Override
    @Transactional
    public void resendVerification(ResendVerificationRequest resendVerificationRequest,
                                   HttpServletRequest httpServletRequest) {
        String email = normalizeEmail(resendVerificationRequest.getEmail());
        Optional<User> userExist = userRepository.findByEmailIgnoreCase(email);

        // Không tiết lộ email có tồn tại trong hệ thống hay không
        if (userExist.isEmpty() || userExist.get().isEmailVerified()) {
            return;
        }

        User user = userExist.get();

        // Giới hạn tần suất gửi lại email
        Optional<EmailVerificationToken> latestToken = emailVerificationTokenRepository
                .findTopByUserUserIdOrderByCreatedAtDesc(user.getUserId());
        if (latestToken.isPresent()
                && latestToken.get().getCreatedAt() != null
                && latestToken.get().getCreatedAt()
                        .isAfter(Instant.now().minusSeconds(TokenConstant.VERIFY_EMAIL_RESEND_COOLDOWN))) {
            return;
        }

        // Vô hiệu hóa mã cũ và gửi mã mới
        emailVerificationTokenRepository.invalidateAllByUserId(user.getUserId());
        sendVerificationEmail(user, httpServletRequest);
    }

    private void sendVerificationEmail(User user, HttpServletRequest httpServletRequest) {
        String token = generateTokenWithUUID();

        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .user(user)
                .tokenHash(DigestUtils.sha256Hex(token))
                .expiredAt(Instant.now().plusSeconds(TokenConstant.VERIFY_EMAIL_EXPIRATION))
                .used(false)
                .userAgent(httpServletRequest.getHeader("user-agent"))
                .ipAddress(httpServletRequest.getRemoteAddr())
                .build();
        emailVerificationTokenRepository.save(verificationToken);

        String verifyLink = feUrl + "/verify-email?token=" + token;
        String subject = "Xác thực email đăng ký tài khoản";
        String content = emailContent.buildVerifyEmailContent(user.getFullName(), verifyLink);

        emailService.sendEmail(user.getEmail(), subject, content);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("None")
                .path("/api/v1/auth/")
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
