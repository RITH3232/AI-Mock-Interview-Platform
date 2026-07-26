package com.interviewiq.controller;

import com.interviewiq.dto.ApiResponse;
import com.interviewiq.security.UserPrincipal;
import com.interviewiq.service.AdminAnalyticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    @Autowired
    private AdminAnalyticsService adminAnalyticsService;

    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPlatformHealth(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        log.info("Fetching admin platform analytics.");
        
        // Final fallback role security validation
        if (userPrincipal == null || !"admin".equalsIgnoreCase(userPrincipal.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .findFirst().orElse(""))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Map<String, Object> health = adminAnalyticsService.getPlatformHealth();
        return ResponseEntity.ok(ApiResponse.success(health));
    }
}
