package com.interviewiq.repository;

import com.interviewiq.model.UserAchievement;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAchievementRepository extends MongoRepository<UserAchievement, String> {
    Optional<UserAchievement> findByUserIdAndAchievementId(String userId, String achievementId);
    List<UserAchievement> findByUserId(String userId);
}
