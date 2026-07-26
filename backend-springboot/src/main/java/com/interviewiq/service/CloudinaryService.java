package com.interviewiq.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class CloudinaryService {
    private Cloudinary cloudinary;
    private final boolean isConfigured;

    public CloudinaryService(
            @Value("${app.cloudinary.cloud-name}") String cloudName,
            @Value("${app.cloudinary.api-key}") String apiKey,
            @Value("${app.cloudinary.api-secret}") String apiSecret) {

        if (cloudName != null && !cloudName.isBlank() && 
            apiKey != null && !apiKey.isBlank() && 
            apiSecret != null && !apiSecret.isBlank()) {
            
            this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName,
                    "api_key", apiKey,
                    "api_secret", apiSecret,
                    "secure", true
            ));
            this.isConfigured = true;
            log.info("Cloudinary is successfully configured.");
        } else {
            this.isConfigured = false;
            log.warn("Cloudinary not configured. Running in Mock Upload mode.");
        }
    }

    public Map upload(byte[] fileBytes, String folder) throws IOException {
        if (!isConfigured) {
            log.info("Cloudinary not configured. Mocking upload.");
            Map<String, Object> mockResult = new HashMap<>();
            mockResult.put("secure_url", "https://example.com/mock-resume.pdf");
            mockResult.put("public_id", "mock-id-" + System.currentTimeMillis());
            return mockResult;
        }

        try {
            return cloudinary.uploader().upload(fileBytes, ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", "auto"
            ));
        } catch (Exception e) {
            log.error("Cloudinary upload failed", e);
            throw new IOException("Failed to upload file to Cloudinary", e);
        }
    }

    public void delete(String publicId) throws IOException {
        if (!isConfigured) {
            log.info("Cloudinary not configured. Mocking deletion of {}", publicId);
            return;
        }

        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Successfully deleted {} from Cloudinary", publicId);
        } catch (Exception e) {
            log.error("Cloudinary deletion failed", e);
            throw new IOException("Failed to delete file from Cloudinary", e);
        }
    }
}
