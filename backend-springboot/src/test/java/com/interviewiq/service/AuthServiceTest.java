package com.interviewiq.service;

import com.interviewiq.dto.RegisterRequest;
import com.interviewiq.exception.AppException;
import com.interviewiq.model.Profile;
import com.interviewiq.model.User;
import com.interviewiq.repository.ProfileRepository;
import com.interviewiq.repository.UserRepository;
import com.interviewiq.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    public void registerUser_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("candidate@example.com");
        request.setPassword("password123");
        request.setFirstName("John");
        request.setLastName("Doe");

        when(userRepository.findByEmailAndIsDeletedFalse(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        
        User user = User.builder()
                .id("userId")
                .email(request.getEmail())
                .password("encodedPassword")
                .role("candidate")
                .build();
        
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtTokenProvider.generateAccessToken(anyString())).thenReturn("accessToken");
        when(jwtTokenProvider.generateRefreshToken(anyString())).thenReturn("refreshToken");

        Map<String, Object> result = authService.registerUser(request);

        assertNotNull(result);
        assertEquals(user, result.get("user"));
        assertEquals("accessToken", result.get("accessToken"));
        assertEquals("refreshToken", result.get("refreshToken"));
        
        verify(userRepository, times(1)).save(any(User.class));
        verify(profileRepository, times(1)).save(any(Profile.class));
    }

    @Test
    public void registerUser_EmailAlreadyRegistered() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("candidate@example.com");

        when(userRepository.findByEmailAndIsDeletedFalse(request.getEmail())).thenReturn(Optional.of(new User()));

        assertThrows(AppException.class, () -> authService.registerUser(request));
        
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void loginUser_Success() {
        String email = "candidate@example.com";
        String password = "password123";

        User user = User.builder()
                .id("userId")
                .email(email)
                .password("encodedPassword")
                .role("candidate")
                .build();

        when(userRepository.findByEmailAndIsDeletedFalse(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, user.getPassword())).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(user.getId())).thenReturn("accessToken");
        when(jwtTokenProvider.generateRefreshToken(user.getId())).thenReturn("refreshToken");

        Map<String, Object> result = authService.loginUser(email, password);

        assertNotNull(result);
        assertEquals(user, result.get("user"));
        assertEquals("accessToken", result.get("accessToken"));
        verify(jwtTokenProvider, times(1)).generateAccessToken(user.getId());
    }

    @Test
    public void loginUser_InvalidCredentials() {
        String email = "candidate@example.com";
        String password = "wrongPassword";

        User user = User.builder()
                .id("userId")
                .email(email)
                .password("encodedPassword")
                .build();

        when(userRepository.findByEmailAndIsDeletedFalse(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, user.getPassword())).thenReturn(false);

        assertThrows(AppException.class, () -> authService.loginUser(email, password));
    }
}
