package com.example.restaurant_food_system;

import com.example.restaurant_food_system.entity.Role;
import com.example.restaurant_food_system.entity.User;
import com.example.restaurant_food_system.repository.RoleRepository;
import com.example.restaurant_food_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitialize implements CommandLineRunner {
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // **. Tạo tài khoản mặc định cho admin
        if (roleRepository.count() > 0) {
            // 1. Lấy role admin, kiểm tra user đã có chưa
            Optional<Role> adminRole = roleRepository.findByRoleName("ADMIN");
            boolean userExist = userRepository.existsUserByRoleName("ADMIN");
            if (adminRole.isPresent() && !userExist) {
                // 2. Tạo tài khoản
                User user = User.builder()
                        .fullName("Administrator")
                        .email("admin@gmail.com")
                        .password(passwordEncoder.encode("admin12345"))
                        .roles(new HashSet<>(Set.of(adminRole.get())))
                        .build();
                userRepository.save(user);
            }
        }
    }
}
