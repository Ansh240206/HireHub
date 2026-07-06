package com.hirehub.dto.profile;

public record ResumeResponse(
        Long id,
        String originalFileName,
        String filePath,
        long sizeBytes
) {
}
