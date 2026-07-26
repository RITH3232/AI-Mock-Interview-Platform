package com.interviewiq.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "resumes")
public class Resume {
    @Id
    private String id;

    private String userId; // Reference to User.id
    private String originalFileName;
    private String cloudinaryUrl;
    private String cloudinaryPublicId;

    @Builder.Default
    private Instant uploadedAt = Instant.now();

    private Long fileSize;

    @Builder.Default
    private String status = "uploaded"; // "uploaded" | "analyzing" | "completed" | "failed"

    private String rawText;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
