package com.hirehub.service;

import com.hirehub.constants.FileConstants;
import com.hirehub.entity.ApplicantProfile;
import com.hirehub.entity.Resume;
import com.hirehub.exception.ValidationException;
import com.hirehub.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final ApplicantProfileService applicantProfileService;
    private final ResumeRepository resumeRepository;

    @Value("${app.files.resume-dir}")
    private String resumeDir;

    @Transactional
    public Resume uploadResume(MultipartFile file) {
        validateResume(file);
        ApplicantProfile profile = applicantProfileService.findOwnProfile();
        Path directory = Path.of(resumeDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(directory);
            String storedName = profile.getId() + "-" + UUID.randomUUID() + ".pdf";
            Path target = directory.resolve(storedName).normalize();
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            Resume resume = resumeRepository.findByApplicantProfileId(profile.getId()).orElseGet(Resume::new);
            resume.setApplicantProfile(profile);
            resume.setOriginalFileName(file.getOriginalFilename() == null ? "resume.pdf" : file.getOriginalFilename());
            resume.setFilePath(target.toString());
            resume.setSizeBytes(file.getSize());
            return resumeRepository.save(resume);
        } catch (IOException ex) {
            throw new ValidationException("Could not store resume");
        }
    }

    private void validateResume(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ValidationException("Resume file is required");
        }
        if (file.getSize() > FileConstants.MAX_RESUME_SIZE_BYTES) {
            throw new ValidationException("Resume maximum size is 5MB");
        }
        if (!FileConstants.PDF_CONTENT_TYPE.equalsIgnoreCase(file.getContentType())) {
            throw new ValidationException("Resume must be a PDF");
        }
    }
}
