package com.example.restaurant_food_system.security;

import com.example.restaurant_food_system.entity.Permission;
import com.example.restaurant_food_system.entity.Role;
import com.example.restaurant_food_system.entity.User;
import com.example.restaurant_food_system.repository.UserRepository;
import com.example.restaurant_food_system.utils.JwtTokenUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {
    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        // 1. Đọc header Authorization từ request
        String requestHeader = request.getHeader("Authorization");

        String token = null;
        TokenPayload tokenPayload = null;

        // 2. Kiểm tra header có chứa token hợp lệ hay không
        if (requestHeader != null && requestHeader.startsWith("Bearer ")) {
            token = requestHeader.substring(7);
            try {
                // Giải mã token để lấy thông tin (Payload) bên trong
                tokenPayload = jwtTokenUtil.getTokenPayload(token);
            } catch (ExpiredJwtException e) {
                // Việc không throw exception sẽ giúp tokenPayload mang giá trị null,
                // từ đó hệ thống sẽ coi như chưa đăng nhập và gọi đến EntryPoint trả về 401 JSON.
                System.out.println("Cảnh báo: Token đã hết hạn - " + e.getMessage());
            } catch (Exception e) {
                // Bắt các lỗi khác như token sai định dạng, bị sửa đổi chữ ký...
                System.out.println("Cảnh báo: Lỗi xử lý Token - " + e.getMessage());
            }
        }
        else {
            // Một số api không cần xác thực, nên cho đi qua
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Nếu giải mã token thành công và hệ thống chưa ghi nhận đăng nhập
        if (tokenPayload != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Lấy thông tin user hiện tại
            Optional<User> userExist = userRepository.findById(tokenPayload.getUserId());

            if (userExist.isPresent()) {
                // 3.1 Kiểm tra token lại thêm
                if (jwtTokenUtil.isValidToken(token, tokenPayload)) {
                    // 3.2 Chuyển đổi quyền vai trò Role của User vào Spring Security
                    Set<String> authorities = new HashSet<>();
                    for (Role role : userExist.get().getRoles()) {
                        authorities.add("ROLE_" + role.getRoleName());

                        for (Permission permission: role.getPermissions()) {
                            authorities.add(permission.getName());
                        }
                    }
                    List<GrantedAuthority> grantedAuthorities = authorities.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

                    // 3.3 Đưa thông tin user vào CustomUser để Spring Security quản lý
                    UserDetails userDetails = new CustomUserDetail(
                            userExist.get().getUserId(),
                            userExist.get().getFullName(),
                            userExist.get().getEmail(),
                            grantedAuthorities
                    );

                    // 3.4. Tạo "Chứng minh thư" (Authentication) xác nhận người dùng đã hợp lệ
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null, // Credentials (mật khẩu) để null vì mình dùng JWT
                                    userDetails.getAuthorities()
                            );

                    // 8. Đặt "Chứng minh thư" vào SecurityContextHolder.
                    // Kể từ dòng này trở đi, Spring Security ghi nhận user đã đăng nhập thành công.
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }

                else {
                    System.out.println("Cảnh báo: Token không vượt qua được hàm isValidToken");
                }

            }
            else {
                System.out.println("Không tìm thấy thông tin người dùng");
            }
        }

        // 4. Dù thành công hay thất bại, bắt buộc phải cho Request đi tiếp đến Controller
        // (hoặc bị Spring Security chặn lại nếu thiếu quyền)
        filterChain.doFilter(request, response);
    }
}
