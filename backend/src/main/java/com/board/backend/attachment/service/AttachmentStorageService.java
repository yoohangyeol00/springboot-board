package com.board.backend.attachment.service;

import com.board.backend.attachment.dto.StoredAttachmentFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class AttachmentStorageService {

    private static final String ATTACHMENT_DIR = "attachments";
    private static final Set<String> BLOCKED_EXTENSIONS = Set.of(
            ".exe", ".bat", ".cmd", ".com", ".sh", ".js", ".jsp", ".php", ".msi"
    );

    @Value("${file.upload-dir}")
    private String uploadDir;

    public StoredAttachmentFile save(MultipartFile file) throws IOException {
        String originalName = cleanOriginalName(file.getOriginalFilename());
        String extension = extractExtension(originalName);

        if (BLOCKED_EXTENSIONS.contains(extension.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("This file type is not allowed.");
        }

        Path attachmentPath = getAttachmentPath();
        Files.createDirectories(attachmentPath);

        String storedName = UUID.randomUUID() + extension;
        Path target = attachmentPath.resolve(storedName).normalize();

        if (!target.startsWith(attachmentPath)) {
            throw new IllegalArgumentException("Invalid file path.");
        }

        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        return new StoredAttachmentFile(
                originalName,
                storedName,
                "/uploads/attachments/" + storedName,
                file.getSize(),
                file.getContentType()
        );
    }

    public Path resolve(String storedName) {
        Path attachmentPath = getAttachmentPath();
        Path filePath = attachmentPath.resolve(storedName).normalize();

        if (!filePath.startsWith(attachmentPath)) {
            throw new IllegalArgumentException("Invalid file path.");
        }

        return filePath;
    }

    public void deleteQuietly(String storedName) {
        if (storedName == null || storedName.isBlank()) {
            return;
        }

        try {
            Files.deleteIfExists(resolve(storedName));
        } catch (IOException e) {
            log.warn("[AttachmentDelete] Failed to delete file: {}", storedName, e);
        }
    }

    private Path getAttachmentPath() {
        return Paths.get(uploadDir).toAbsolutePath().normalize().resolve(ATTACHMENT_DIR);
    }

    private String cleanOriginalName(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "attachment";
        }

        String normalizedName = Paths.get(originalFilename).getFileName().toString();
        if (normalizedName.contains("..")) {
            throw new IllegalArgumentException("Invalid file name.");
        }

        return normalizedName;
    }

    private String extractExtension(String filename) {
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex < 0) {
            return "";
        }

        return filename.substring(dotIndex);
    }
}
