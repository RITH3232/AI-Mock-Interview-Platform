package com.interviewiq.controller;

import com.interviewiq.dto.ApiResponse;
import com.interviewiq.dto.LoginRequest;
import com.interviewiq.dto.RegisterRequest;
import com.interviewiq.dto.UserDto;
import com.interviewiq.exception.AppException;
import com.interviewiq.model.User;
import com.interviewiq.repository.UserRepository;
import com.interviewiq.security.JwtTokenProvider;
import com.interviewiq.security.UserPrincipal;
import com.interviewiq.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, UserDto>>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response
    ) {
        log.info("Received registration request for email: {}", request.getEmail());
        Map<String, Object> result = authService.registerUser(request);

        User user = (User) result.get("user");
        String accessToken = (String) result.get("accessToken");
        String refreshToken = (String) result.get("refreshToken");

        jwtTokenProvider.setTokenCookies(response, accessToken, refreshToken);

        UserDto userDto = UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();

        return new ResponseEntity<>(ApiResponse.success(Map.of("user", userDto)), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, UserDto>>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        log.info("Received login request for email: {}", request.getEmail());
        Map<String, Object> result = authService.loginUser(request.getEmail(), request.getPassword());

        User user = (User) result.get("user");
        String accessToken = (String) result.get("accessToken");
        String refreshToken = (String) result.get("refreshToken");

        jwtTokenProvider.setTokenCookies(response, accessToken, refreshToken);

        UserDto userDto = UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();

        return ResponseEntity.ok(ApiResponse.success(Map.of("user", userDto)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
        log.info("Received logout request.");
        jwtTokenProvider.clearTokenCookies(response);
        return ResponseEntity.ok(ApiResponse.successMessage("Logged out successfully"));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<Void>> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        log.info("Received token refresh request.");
        String rToken = null;

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    rToken = cookie.getValue();
                    break;
                }
            }
        }

        if (rToken == null || rToken.isBlank()) {
            throw new AppException("No refresh token provided", HttpStatus.UNAUTHORIZED);
        }

        if (!jwtTokenProvider.validateRefreshToken(rToken)) {
            jwtTokenProvider.clearTokenCookies(response);
            throw new AppException("Invalid or expired refresh token", HttpStatus.UNAUTHORIZED);
        }

        String userId = jwtTokenProvider.getUserIdFromRefreshToken(rToken);
        Map<String, String> tokens = authService.refreshUserToken(userId);

        jwtTokenProvider.setTokenCookies(response, tokens.get("accessToken"), tokens.get("refreshToken"));

        return ResponseEntity.ok(ApiResponse.successMessage("Token refreshed"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, UserDto>>> getMe(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            throw new AppException("Not authorized to access this route", HttpStatus.UNAUTHORIZED);
        }

        UserDto userDto = UserDto.builder()
                .id(userPrincipal.getId())
                .email(userPrincipal.getEmail())
                .role(userPrincipal.getAuthorities().stream()
                        .map(a -> a.getAuthority().replace("ROLE_", "").toLowerCase())
                        .findFirst().orElse("candidate"))
                .build();

        return ResponseEntity.ok(ApiResponse.success(Map.of("user", userDto)));
    }
}
