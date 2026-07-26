package com.interviewiq.service;

import com.interviewiq.model.InterviewReport;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Slf4j
@Service
public class PdfGeneratorService {

    public byte[] generateInterviewPDF(InterviewReport report) {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Set up fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24);
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

            // Title
            Paragraph title = new Paragraph("InterviewIQ Performance Report", titleFont);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("\n"));

            // Overall Assessment
            document.add(new Paragraph("Overall Assessment", sectionFont));
            document.add(new Paragraph("Overall Score: " + (report.getOverallScore() != null ? report.getOverallScore().intValue() : 0) + "/100", boldFont));
            document.add(new Paragraph("Technical Score: " + (report.getTechnicalScore() != null ? report.getTechnicalScore().intValue() : 0) + "/100", normalFont));
            document.add(new Paragraph("Communication Score: " + (report.getCommunicationScore() != null ? report.getCommunicationScore().intValue() : 0) + "/100", normalFont));
            document.add(new Paragraph("Confidence Score: " + (report.getConfidenceScore() != null ? report.getConfidenceScore().intValue() : 0) + "/100", normalFont));
            document.add(new Paragraph("Problem Solving Score: " + (report.getProblemSolvingScore() != null ? report.getProblemSolvingScore().intValue() : 0) + "/100", normalFont));
            document.add(new Paragraph("\n"));

            // Key Strengths
            document.add(new Paragraph("Key Strengths", sectionFont));
            if (report.getStrengths() != null) {
                for (String strength : report.getStrengths()) {
                    document.add(new Paragraph("• " + strength, normalFont));
                }
            }
            document.add(new Paragraph("\n"));

            // Areas for Improvement
            document.add(new Paragraph("Areas for Improvement", sectionFont));
            if (report.getWeaknesses() != null) {
                for (String weakness : report.getWeaknesses()) {
                    document.add(new Paragraph("• " + weakness, normalFont));
                }
            }
            document.add(new Paragraph("\n"));

            // Learning Roadmap
            document.add(new Paragraph("Recommended Learning Roadmap", sectionFont));
            if (report.getLearningRoadmap() != null) {
                for (String step : report.getLearningRoadmap()) {
                    document.add(new Paragraph("• " + step, normalFont));
                }
            }

            document.close();
        } catch (Exception e) {
            log.error("Failed to generate PDF report", e);
        }

        return out.toByteArray();
    }
}
