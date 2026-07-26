package com.interviewiq.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "leaderboards")
public class Leaderboard {
    @Id
    private String id;

    @Indexed(unique = true)
    private String userId; // Reference to User.id

    @Indexed
    @Builder.Default
    private Integer xp = 0;

    @Indexed
    @Builder.Default
    private Integer weeklyXP = 0;

    @Indexed
    @Builder.Default
    private Integer monthlyXP = 0;

    @Builder.Default
    private Integer rankChange = 0;

    @Indexed
    @Builder.Default
    private Integer totalInterviews = 0;

    @Indexed
    @Builder.Default
    private Double averageScore = 0.0;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
