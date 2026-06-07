package com.example.restaurant_food_system.service.role;

import com.example.restaurant_food_system.dto.response.RoleOptionResponse;
import com.example.restaurant_food_system.entity.Role;
import com.example.restaurant_food_system.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;

    @Override
    public List<RoleOptionResponse> getAllRoleOptions() {
        List<Role> roles = roleRepository.findAll();

        return roles.stream()
                .map(role -> RoleOptionResponse.builder()
                        .roleId(role.getRoleId())
                        .roleName(role.getRoleName())
                        .build())
                .toList();
    }
}
