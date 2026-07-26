package com.interviewiq.controller;

import com.interviewiq.dto.ApiResponse;
import com.interviewiq.model.Achievement;
import com.interviewiq.security.UserPrincipal;
import com.interviewiq.service.AnalyticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        log.info("Fetching dashboard metrics for user: {}", userPrincipal.getId());
        Map<String, Object> metrics = analyticsService.getDashboardMetrics(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(metrics));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<ApiResponse<List<AnalyticsService.LeaderboardEntryResponse>>> getLeaderboard() {
        log.info("Fetching global leaderboard.");
        List<AnalyticsService.LeaderboardEntryResponse> leaderboard = analyticsService.getLeaderboard(50);
        return ResponseEntity.ok(ApiResponse.success(leaderboard));
    }

    @GetMapping("/achievements")
    public ResponseEntity<ApiResponse<List<Achievement>>> getAchievements() {
        log.info("Fetching achievements catalog.");
        List<Achievement> achievements = analyticsService.getAchievementsCatalog();
        return ResponseEntity.ok(ApiResponse.success(achievements));
    }
}
