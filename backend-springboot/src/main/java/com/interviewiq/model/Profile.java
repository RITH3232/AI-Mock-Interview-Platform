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
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "profiles")
public class Profile {
    @Id
    private String id;

    @Indexed(unique = true)
    private String user; // Reference to User.id

    private String firstName;
    private String lastName;
    private String profilePhoto;
    private String college;
    private Integer graduationYear;

    @Builder.Default
    private List<String> skills = new ArrayList<>();

    @Builder.Default
    private String experienceLevel = "Beginner"; // "Beginner" | "Intermediate" | "Advanced"

    private String preferredDomain;
    private String resumeUrl;
    private Double atsScore;

    @Builder.Default
    private Double profileCompletionPercentage = 20.0;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
