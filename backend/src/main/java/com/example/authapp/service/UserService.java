package com.example.authapp.service;

import com.example.authapp.dto.response.UserProfileResponse;
import com.example.authapp.dto.response.UserSummaryResponse;
import com.example.authapp.entity.User;
import com.example.authapp.exception.AppException;
import com.example.authapp.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "user not found"));

        return new UserProfileResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole().name());
    }

    public List<UserSummaryResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserSummaryResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getRole().name(),
                        user.getCreatedAt()
                ))
                .toList();
    }
}
