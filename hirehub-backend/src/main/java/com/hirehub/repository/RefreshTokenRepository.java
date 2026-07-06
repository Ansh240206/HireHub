package com.hirehub.repository;

import com.hirehub.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("update RefreshToken token set token.revoked = true where token.user.id = :userId and token.revoked = false")
    void revokeAllActiveTokensForUser(@Param("userId") Long userId);
}
