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
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "interviewtemplates")
public class InterviewTemplate {
    @Id
    private String id;

    private String name;
    private String company;

    @Builder.Default
    private List<String> domains = new ArrayList<>();

    private String difficulty; // "Easy" | "Medium" | "Hard"
    private String experienceLevel; // "Fresher" | "Internship" | "1 Year" | "2+ Years"

    @Builder.Default
    private Integer questionCount = 5;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
