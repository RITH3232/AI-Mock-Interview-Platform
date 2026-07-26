package com.interviewiq.config;

import com.interviewiq.model.Achievement;
import com.interviewiq.repository.AchievementRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private AchievementRepository achievementRepository;

    @Override
    public void run(String... args) throws Exception {
        if (achievementRepository.count() == 0) {
            log.info("Achievements catalog is empty. Seeding default achievements.");
            List<Achievement> achievements = List.of(
                    Achievement.builder()
                            .name("First Interview")
                            .description("Complete your first mock interview")
                            .icon("award")
                            .xpReward(50)
                            .category("Interviews")
                            .build(),
                    Achievement.builder()
                            .name("5 Interviews Completed")
                            .description("Complete 5 mock interviews")
                            .icon("award")
                            .xpReward(100)
                            .category("Interviews")
                            .build(),
                    Achievement.builder()
                            .name("10 Interviews Completed")
                            .description("Complete 10 mock interviews")
                            .icon("award")
                            .xpReward(250)
                            .category("Interviews")
                            .build(),
                    Achievement.builder()
                            .name("90+ Score")
                            .description("Achieve a score of 90 or above in any mock interview")
                            .icon("star")
                            .xpReward(150)
                            .category("Performance")
                            .build(),
                    Achievement.builder()
                            .name("7 Day Streak")
                            .description("Maintain a mock interview streak for 7 consecutive days")
                            .icon("zap")
                            .xpReward(200)
                            .category("Consistency")
                            .build()
            );
            achievementRepository.saveAll(achievements);
            log.info("Successfully seeded {} achievements.", achievements.size());
        }
    }
}
