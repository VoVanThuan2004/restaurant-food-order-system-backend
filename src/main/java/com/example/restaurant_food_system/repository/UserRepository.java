package com.example.restaurant_food_system.repository;

import com.example.restaurant_food_system.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    @Query("""
        select count(u) > 0
        from User u
        join u.roles r
        where r.roleName = :roleName
    """)
    boolean existsUserByRoleName(@Param("roleName") String roleName);

    @Query("""
        select u
        from User u
        where u.email = :email
    """)
    Optional<User> findByEmail(@Param("email") String email);


    @Query("""
        select u
        from User u
        join u.roles r
        where (u.email ilike concat('%', :search, '%') or u.fullName ilike concat('%', :search, '%'))
        and r.roleName <> 'ADMIN'
    """)
    Page<User> findAllUsers(@Param("search") String search, Pageable pageable);
}
