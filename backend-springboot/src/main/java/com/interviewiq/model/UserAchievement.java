package com.interviewiq.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "userachievements")
@CompoundIndex(name = "user_achievement_uniq", def = "{'userId': 1, 'achievementId': 1}", unique = true)
public class UserAchievement {
    @Id
    private String id;

    private String userId; // Reference to User.id
    private String achievementId; // Reference to Achievement.id

    @Builder.Default
    private Instant unlockedAt = Instant.now();

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
