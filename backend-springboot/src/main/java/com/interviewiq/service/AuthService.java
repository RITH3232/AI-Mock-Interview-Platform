package com.interviewiq.service;

import com.interviewiq.dto.RegisterRequest;
import com.interviewiq.exception.AppException;
import com.interviewiq.model.Profile;
import com.interviewiq.model.User;
import com.interviewiq.repository.ProfileRepository;
import com.interviewiq.repository.UserRepository;
import com.interviewiq.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Transactional
    public Map<String, Object> registerUser(RegisterRequest data) {
        if (userRepository.findByEmailAndIsDeletedFalse(data.getEmail()).isPresent()) {
            throw new AppException("Email is already registered", HttpStatus.BAD_REQUEST);
        }

        String verificationToken = UUID.randomUUID().toString();

        User user = User.builder()
                .email(data.getEmail())
                .password(passwordEncoder.encode(data.getPassword()))
                .verificationToken(verificationToken)
                .role("candidate")
                .isVerified(false)
                .build();

        User savedUser = userRepository.save(user);

        Profile profile = Profile.builder()
                .user(savedUser.getId())
                .firstName(data.getFirstName())
                .lastName(data.getLastName())
                .profileCompletionPercentage(20.0)
                .build();

        profileRepository.save(profile);

        String accessToken = jwtTokenProvider.generateAccessToken(savedUser.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(savedUser.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("user", savedUser);
        result.put("accessToken", accessToken);
        result.put("refreshToken", refreshToken);
        return result;
    }

    public Map<String, Object> loginUser(String email, String password) {
        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new AppException("Invalid credentials", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AppException("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("accessToken", accessToken);
        result.put("refreshToken", refreshToken);
        return result;
    }

    public Map<String, String> refreshUserToken(String userId) {
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        return Map.of("accessToken", accessToken, "refreshToken", refreshToken);
    }
}
