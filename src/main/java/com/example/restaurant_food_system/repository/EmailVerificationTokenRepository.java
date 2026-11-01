package com.example.restaurant_food_system.repository;

import com.example.restaurant_food_system.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, String> {

    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);

    Optional<EmailVerificationToken> findTopByUserUserIdOrderByCreatedAtDesc(String userId);

    @Modifying
    @Query("""
        update EmailVerificationToken t
        set t.used = true
        where t.user.userId = :userId
        and t.used = false
    """)
    void invalidateAllByUserId(@Param("userId") String userId);
}
