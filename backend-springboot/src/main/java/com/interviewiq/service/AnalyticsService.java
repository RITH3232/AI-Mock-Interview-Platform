package com.interviewiq.service;

import com.interviewiq.exception.AppException;
import com.interviewiq.model.*;
import com.interviewiq.repository.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    @Autowired
    private AnalyticsRepository analyticsRepository;

    @Autowired
    private LeaderboardRepository leaderboardRepository;

    @Autowired
    private InterviewSessionRepository sessionRepository;

    @Autowired
    private InterviewReportRepository reportRepository;

    @Autowired
    private UserAchievementRepository userAchievementRepository;

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private ProfileRepository profileRepository;

    public Map<String, Object> getDashboardMetrics(String userId) {
        Analytics analytics = analyticsRepository.findByUserId(userId)
                .orElse(null);

        Leaderboard leaderboard = leaderboardRepository.findByUserId(userId)
                .orElse(null);

        // Calculate global rank
        long globalRank = 0;
        if (leaderboard != null) {
            globalRank = leaderboardRepository.countByXpGreaterThan(leaderboard.getXp()) + 1;
        }

        // Fetch Recent Completed Sessions
        List<InterviewSession> recentSessions = sessionRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, "completed");
        List<InterviewSession> limitedSessions = recentSessions.stream().limit(5).collect(Collectors.toList());

        // Attach scores to recent history
        List<Map<String, Object>> recentHistory = new ArrayList<>();
        for (InterviewSession session : limitedSessions) {
            double score = 0.0;
            Optional<InterviewReport> rep = reportRepository.findByInterviewSessionId(session.getId());
            if (rep.isPresent()) {
                score = rep.get().getOverallScore();
            }

            Map<String, Object> map = new HashMap<>();
            map.put("_id", session.getId());
            map.put("interviewType", session.getInterviewType());
            map.put("difficulty", session.getDifficulty());
            map.put("domain", session.getDomain());
            map.put("experienceLevel", session.getExperienceLevel());
            map.put("company", session.getCompany());
            map.put("status", session.getStatus());
            map.put("score", score);
            map.put("createdAt", session.getCreatedAt());
            recentHistory.add(map);
        }

        // Fetch Unlocked Achievements
        List<UserAchievement> unlocked = userAchievementRepository.findByUserId(userId);
        List<Achievement> achievements = new ArrayList<>();
        for (UserAchievement ua : unlocked) {
            achievementRepository.findById(ua.getAchievementId()).ifPresent(achievements::add);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("analytics", analytics != null ? analytics : Map.of("totalInterviews", 0, "averageScore", 0, "bestScore", 0, "totalXP", 0));
        result.put("leaderboard", leaderboard != null ? leaderboard : Map.of("xp", 0, "weeklyXP", 0));
        result.put("rank", globalRank);
        result.put("recentHistory", recentHistory);
        result.put("achievements", achievements);

        return result;
    }

    public List<LeaderboardEntryResponse> getLeaderboard(int limit) {
        List<Leaderboard> topEntries = leaderboardRepository.findAllByOrderByXpDesc(PageRequest.of(0, limit));
        List<LeaderboardEntryResponse> responseList = new ArrayList<>();

        for (Leaderboard entry : topEntries) {
            LeaderboardEntryResponse.UserDetails userDetails = new LeaderboardEntryResponse.UserDetails();
            userDetails.setId(entry.getUserId());
            
            profileRepository.findByUser(entry.getUserId()).ifPresent(profile -> {
                userDetails.setFirstName(profile.getFirstName());
                userDetails.setLastName(profile.getLastName());
                userDetails.setAvatarUrl(profile.getProfilePhoto());
            });

            LeaderboardEntryResponse res = LeaderboardEntryResponse.builder()
                    .id(entry.getId())
                    .xp(entry.getXp())
                    .weeklyXP(entry.getWeeklyXP())
                    .monthlyXP(entry.getMonthlyXP())
                    .totalInterviews(entry.getTotalInterviews())
                    .averageScore(entry.getAverageScore())
                    .userId(userDetails)
                    .build();
            responseList.add(res);
        }

        return responseList;
    }

    public List<Achievement> getAchievementsCatalog() {
        return achievementRepository.findAllByOrderByXpRewardAsc();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeaderboardEntryResponse {
        private String id;
        private Integer xp;
        private Integer weeklyXP;
        private Integer monthlyXP;
        private Integer totalInterviews;
        private Double averageScore;
        private UserDetails userId;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class UserDetails {
            private String id;
            private String firstName;
            private String lastName;
            private String avatarUrl;
        }
    }
}
