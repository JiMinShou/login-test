package com.example.authapp.repository;

import com.example.authapp.entity.RefreshToken;
import com.example.authapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    long deleteByUser(User user);
}
