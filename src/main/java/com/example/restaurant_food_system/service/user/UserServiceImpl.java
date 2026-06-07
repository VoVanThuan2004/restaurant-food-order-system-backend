package com.example.restaurant_food_system.service.user;

import com.example.restaurant_food_system.dto.request.UserCreateRequest;
import com.example.restaurant_food_system.dto.request.UserRequest;
import com.example.restaurant_food_system.dto.response.UploadResult;
import com.example.restaurant_food_system.dto.response.UserProfileResponse;
import com.example.restaurant_food_system.dto.response.UserResponse;
import com.example.restaurant_food_system.entity.Role;
import com.example.restaurant_food_system.entity.User;
import com.example.restaurant_food_system.exception.BadRequestException;
import com.example.restaurant_food_system.exception.ResourceNotFoundException;
import com.example.restaurant_food_system.mapper.UserMapper;
import com.example.restaurant_food_system.repository.RoleRepository;
import com.example.restaurant_food_system.repository.UserRepository;
import com.example.restaurant_food_system.service.CloudinaryService;
import com.example.restaurant_food_system.service.EmailService;
import com.example.restaurant_food_system.utils.EmailContent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CloudinaryService cloudinaryService;
    private final RoleRepository roleRepository;
    private final JavaMailSenderImpl mailSender;
    private final EmailService emailService;
    private final EmailContent emailContent;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserProfileResponse getProfile(String userId) {
        // 1. Kiểm tra người dùng
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));

        // 2. Mapping data trả về
        return userMapper.mapToProfileResponse(user);
    }

    @Override
    public void updateUser(String userId, UserRequest userRequest, MultipartFile file) {
        // 1. Kiểm tra người dùng
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));

        // 2. Kiểm tra có upload file
        if (file != null) {
            // 2.1 Xóa ảnh cũ trên cloudinary
            if (user.getAvatarPublicId() != null) {
                cloudinaryService.deleteFile(user.getAvatarPublicId());
            }

            // 2.2 Kiểm tra định dạng file
            if (file.getSize() > 5 * 1024 * 1024) {
                throw new BadRequestException("File vượt quá 5MB");
            }

            String contentType = file.getContentType();
            List<String> allowedTypes = List.of("image/jpeg", "image/png", "image/jpg");
            if (contentType == null || !allowedTypes.contains(contentType)) {
                throw new BadRequestException("Chỉ chấp nhận JPG, PNG");
            }

            try {
                BufferedImage image = ImageIO.read(file.getInputStream());
                if (image == null) {
                    throw new BadRequestException("File không phải ảnh hợp lệ");
                }
            } catch (IOException e) {
                throw new BadRequestException("Không đọc được file");
            }

            // 2.3 Upload ảnh lên cloudinary
            UploadResult uploadResult = cloudinaryService.uploadFile(file);

            // 2.4 Cập nhật vào user
            user.setAvatarPublicId(uploadResult.getPublicId());
            user.setAvatarUrl(uploadResult.getSecureUrl());
        }

        // 3. Set các field cập nhật thông tin user
        user.setFullName(userRequest.getFullName());
        user.setGender(userRequest.getGender());
        user.setPhoneNumber(userRequest.getPhoneNumber());
        user.setDateOfBirth(userRequest.getDateOfBirth());
        userRepository.save(user);
    }

    @Override
    public Page<UserResponse> getAllUsers(int page, int size, String search) {
        // 1. Tạo đối tượng phân trang
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // 2. Query data
        Page<User> users = userRepository.findAllUsers(search, pageable);

        // 3. Mapping data trả về
        return users.map(userMapper::mapToResponse);
    }

    @Override
    @Transactional
    public void createUser(UserCreateRequest userCreateRequest) {
        // 1. Tạo random mật khẩu
        String password = generateRandomPassword();

        // 2. Kiểm tra vai trò có hợp lệ
        List<Role> roles = roleRepository.findByRoleIdIn(userCreateRequest.getRoleIds());
        if (roles.isEmpty() || roles.size() != userCreateRequest.getRoleIds().size()) {
            throw new BadRequestException("Vai trò không hợp lệ");
        }

        // 3. Tạo người dùng
        User user = User.builder()
                .email(userCreateRequest.getEmail())
                .fullName(userCreateRequest.getFullName())
                .phoneNumber(userCreateRequest.getPhoneNumber())
                .gender(userCreateRequest.getGender())
                .dateOfBirth(userCreateRequest.getDateOfBirth())
                .roles(new HashSet<>(roles))
                .password(passwordEncoder.encode(password))
                .build();
        userRepository.save(user);

        // 4. Gửi email người dùng
        String subject = "Thông tin tài khoản đăng nhập hệ thống";
        String content = emailContent.buildAccountEmailContent(user.getFullName(), user.getEmail(), password);
        emailService.sendEmail(user.getEmail(), subject, content);
    }

    @Override
    public boolean activateUser(String userId) {
        // 1. Kiểm tra user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));

        boolean isActive = user.isActive();
        user.setActive(!isActive);
        userRepository.save(user);

        // 2. Xóa hết tất cả refresh token của user này


        return !isActive;
    }

    @Override
    public void updateUserForAdmin(String userId, UserCreateRequest userUpdateRequest) {
        // 1. Kiểm tra user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));

        // 2. Kiểm tra role
        List<Role> roles =
                roleRepository.findByRoleIdIn(userUpdateRequest.getRoleIds());

        if (roles.size() != userUpdateRequest.getRoleIds().size()) {
            throw new BadRequestException("Vai trò không hợp lệ");
        }


        // 3. Set các field cập nhật thông tin user
        user.setFullName(userUpdateRequest.getFullName());
        user.setGender(userUpdateRequest.getGender());
        user.setPhoneNumber(userUpdateRequest.getPhoneNumber());
        user.setDateOfBirth(userUpdateRequest.getDateOfBirth());
        user.setRoles(new HashSet<>(roles));
        userRepository.save(user);
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$";
        SecureRandom random = new SecureRandom();

        StringBuilder password = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }
}
