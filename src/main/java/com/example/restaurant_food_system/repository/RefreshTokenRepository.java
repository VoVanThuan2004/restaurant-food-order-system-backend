package com.example.restaurant_food_system.repository;

import com.example.restaurant_food_system.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    @Query("""
        select r
        from RefreshToken r
        where r.refreshToken = :refreshToken
    """)
    Optional<RefreshToken> findByRefreshToken(@Param("refreshToken") String refreshToken);


    @Modifying
    @Query("""
    delete
    from RefreshToken r
    where r.user.userId = :userId
""")
    void deleteAllByUserId(@Param("userId") String userId);
}
