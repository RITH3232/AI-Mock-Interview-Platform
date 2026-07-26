package com.interviewiq.service;

import com.interviewiq.model.*;
import com.interviewiq.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class GamificationService {

    @Autowired
    private AnalyticsRepository analyticsRepository;

    @Autowired
    private LeaderboardRepository leaderboardRepository;

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private UserAchievementRepository userAchievementRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Transactional
    public void processSessionCompletion(
            String userId,
            String sessionId,
            double overallScore,
            String company,
            String domain
    ) {
        log.info("Processing gamification details for User ID: {}, Session: {}", userId, sessionId);

        // 1. Calculate XP based on rules
        int xpEarned = 50; // Base XP
        if (overallScore >= 90) xpEarned += 100;
        else if (overallScore >= 80) xpEarned += 75;
        else if (overallScore >= 70) xpEarned += 50;
        else if (overallScore >= 60) xpEarned += 25;

        xpEarned += 25; // Completion bonus

        List<String> targetCompanies = List.of("Google", "Amazon", "Microsoft", "TCS", "Infosys", "Accenture");
        if (company != null && targetCompanies.contains(company)) {
            xpEarned += 25;
        }

        // 2. Fetch and update Analytics
        Analytics analytics = analyticsRepository.findByUserId(userId)
                .orElseGet(() -> Analytics.builder().userId(userId).build());

        Instant now = Instant.now();
        Instant lastDate = analytics.getStreakInfo().getLastInterviewDate();
        boolean isStreakIncremented = false;

        if (lastDate != null) {
            // Days difference calculation
            long diffDays = ChronoUnit.DAYS.between(lastDate.truncatedTo(ChronoUnit.DAYS), now.truncatedTo(ChronoUnit.DAYS));

            if (diffDays == 1) {
                analytics.getStreakInfo().setCurrent(analytics.getStreakInfo().getCurrent() + 1);
                isStreakIncremented = true;
            } else if (diffDays > 1) {
                analytics.getStreakInfo().setCurrent(1); // Reset streak
            }
        } else {
            analytics.getStreakInfo().setCurrent(1);
            isStreakIncremented = true;
        }

        if (analytics.getStreakInfo().getCurrent() > analytics.getStreakInfo().getLongest()) {
            analytics.getStreakInfo().setLongest(analytics.getStreakInfo().getCurrent());
        }
        analytics.getStreakInfo().setLastInterviewDate(now);

        // Apply Streak Bonuses
        if (analytics.getStreakInfo().getCurrent() == 7 && isStreakIncremented) xpEarned += 50;
        if (analytics.getStreakInfo().getCurrent() == 30 && isStreakIncremented) xpEarned += 200;

        // Update Analytics Stats
        analytics.setTotalInterviews(analytics.getTotalInterviews() + 1);
        analytics.setTotalXP(analytics.getTotalXP() + xpEarned);
        
        if (overallScore > analytics.getBestScore()) {
            analytics.setBestScore(overallScore);
        }
        if (overallScore < analytics.getWorstScore() || analytics.getWorstScore() == 0) {
            analytics.setWorstScore(overallScore);
        }

        double oldAvg = analytics.getAverageScore();
        double newAvg = Math.round(((oldAvg * (analytics.getTotalInterviews() - 1)) + overallScore) / analytics.getTotalInterviews());
        analytics.setAverageScore(newAvg);

        // Save Analytics doc
        Analytics savedAnalytics = analyticsRepository.save(analytics);

        // 3. Update Leaderboard
        Leaderboard leaderboard = leaderboardRepository.findByUserId(userId)
                .orElseGet(() -> Leaderboard.builder().userId(userId).build());
        
        leaderboard.setXp(leaderboard.getXp() + xpEarned);
        leaderboard.setWeeklyXP(leaderboard.getWeeklyXP() + xpEarned);
        leaderboard.setMonthlyXP(leaderboard.getMonthlyXP() + xpEarned);
        leaderboard.setAverageScore(newAvg);
        leaderboard.setTotalInterviews(savedAnalytics.getTotalInterviews());

        leaderboardRepository.save(leaderboard);

        // 4. Check Achievements
        checkAndAwardAchievements(userId, savedAnalytics);

        // 5. Send Notification
        Notification notification = Notification.builder()
                .userId(userId)
                .title("Interview Completed!")
                .message("You earned " + xpEarned + " XP. Great job!")
                .type("interview")
                .isRead(false)
                .build();
        
        notificationRepository.save(notification);
    }

    private void checkAndAwardAchievements(String userId, Analytics analytics) {
        checkAward(userId, "First Interview", analytics.getTotalInterviews() >= 1);
        checkAward(userId, "5 Interviews Completed", analytics.getTotalInterviews() >= 5);
        checkAward(userId, "10 Interviews Completed", analytics.getTotalInterviews() >= 10);
        checkAward(userId, "90+ Score", analytics.getBestScore() >= 90);
        checkAward(userId, "7 Day Streak", analytics.getStreakInfo().getLongest() >= 7);
    }

    private void checkAward(String userId, String achievementName, boolean condition) {
        if (!condition) return;

        Optional<Achievement> achOpt = achievementRepository.findByName(achievementName);
        if (achOpt.isPresent()) {
            Achievement ach = achOpt.get();
            Optional<UserAchievement> userAchOpt = userAchievementRepository.findByUserIdAndAchievementId(userId, ach.getId());
            
            if (userAchOpt.isEmpty()) {
                // Unlock achievement
                UserAchievement userAchievement = UserAchievement.builder()
                        .userId(userId)
                        .achievementId(ach.getId())
                        .unlockedAt(Instant.now())
                        .build();
                userAchievementRepository.save(userAchievement);

                // Increment achievement count in user analytics
                analyticsRepository.findByUserId(userId).ifPresent(analytics -> {
                    analytics.setTotalAchievements(analytics.getTotalAchievements() + 1);
                    analyticsRepository.save(analytics);
                });

                // Send achievement unlocked notification
                Notification notification = Notification.builder()
                        .userId(userId)
                        .title("Achievement Unlocked!")
                        .message("You unlocked: " + ach.getName() + " (+ " + ach.getXpReward() + " XP)")
                        .type("achievement")
                        .isRead(false)
                        .build();
                notificationRepository.save(notification);

                // Reward XP in leaderboard
                leaderboardRepository.findByUserId(userId).ifPresent(leaderboard -> {
                    leaderboard.setXp(leaderboard.getXp() + ach.getXpReward());
                    leaderboardRepository.save(leaderboard);
                });
            }
        }
    }
}
