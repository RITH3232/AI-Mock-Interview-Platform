package com.interviewiq.service;

import com.interviewiq.exception.AppException;
import com.interviewiq.model.Resume;
import com.interviewiq.repository.ResumeAnalysisRepository;
import com.interviewiq.repository.ResumeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
public class ResumeService {

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private ResumeAnalysisRepository resumeAnalysisRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private ResumeAnalysisPipelineService pipelineService;

    @Transactional
    public Resume uploadAndAnalyze(String userId, String originalFilename, byte[] fileBytes) {
        log.info("Uploading resume for User ID: {}", userId);
        
        // 1. Upload to Cloudinary
        Map uploadResult;
        try {
            uploadResult = cloudinaryService.upload(fileBytes, "interview-iq/resumes");
        } catch (IOException e) {
            log.error("Cloudinary upload failed", e);
            throw new AppException("Failed to upload file to Cloudinary", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        String secureUrl = (String) uploadResult.get("secure_url");
        String publicId = (String) uploadResult.get("public_id");

        // 2. Create DB record
        Resume resume = Resume.builder()
                .userId(userId)
                .originalFileName(originalFilename)
                .cloudinaryUrl(secureUrl)
                .cloudinaryPublicId(publicId)
                .fileSize((long) fileBytes.length)
                .status("uploaded")
                .uploadedAt(Instant.now())
                .build();

        Resume savedResume = resumeRepository.save(resume);

        // 3. Trigger Async background parsing pipeline
        pipelineService.runPipeline(savedResume.getId(), fileBytes);

        return savedResume;
    }

    @Transactional
    public void deleteResume(String resumeId, String userId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new AppException("Resume not found", HttpStatus.NOT_FOUND));

        if (!resume.getUserId().equals(userId)) {
            throw new AppException("Not authorized to delete this resume", HttpStatus.FORBIDDEN);
        }

        // Delete from Cloudinary
        try {
            cloudinaryService.delete(resume.getCloudinaryPublicId());
        } catch (IOException e) {
            log.warn("Failed to delete file from Cloudinary: {}", resume.getCloudinaryPublicId(), e);
        }

        // Delete analysis and resume record from DB
        resumeAnalysisRepository.findByResumeId(resumeId).ifPresent(analysis -> 
            resumeAnalysisRepository.delete(analysis)
        );
        
        resumeRepository.delete(resume);
        log.info("Successfully deleted Resume ID: {}", resumeId);
    }
}
