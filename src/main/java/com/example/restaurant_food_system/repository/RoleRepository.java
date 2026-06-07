package com.example.restaurant_food_system.repository;

import com.example.restaurant_food_system.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, String> {

    Optional<Role> findByRoleName(String roleName);

    @Query("""
        select count(r)
        from Role r
        where r.roleId in :roleIds
    """)
    long countByRoleIds(@Param("roleIds") List<String> roleIds);

    List<Role> findByRoleIdIn(List<String> roleIds);
}
