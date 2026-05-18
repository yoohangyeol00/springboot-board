package com.board.backend.image.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class ImageService {

    private static final Pattern UPLOAD_IMAGE_PATTERN = Pattern.compile("/uploads/(?!attachments/)[^\\s\"')\\]]+");

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String save(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String extension = extractExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + extension;

        Files.copy(file.getInputStream(), uploadPath.resolve(filename));

        return "/uploads/" + filename;
    }

    public void deleteImages(String content) {
        extractImageUrls(content).forEach(this::deleteImage);
    }

    public void deleteRemovedImages(String oldContent, String newContent) {
        Set<String> oldImageUrls = extractImageUrls(oldContent);
        Set<String> newImageUrls = extractImageUrls(newContent);

        oldImageUrls.removeAll(newImageUrls);
        oldImageUrls.forEach(this::deleteImage);
    }

    private Set<String> extractImageUrls(String content) {
        Set<String> imageUrls = new LinkedHashSet<>();

        if (content == null || !content.contains("/uploads/")) {
            return imageUrls;
        }

        Matcher matcher = UPLOAD_IMAGE_PATTERN.matcher(content);
        while (matcher.find()) {
            imageUrls.add(matcher.group());
        }

        return imageUrls;
    }

    private void deleteImage(String imageUrl) {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
        String filename = imageUrl.substring("/uploads/".length());
        Path filePath = uploadPath.resolve(filename).normalize();

        if (!filePath.startsWith(uploadPath)) {
            log.warn("[ImageDelete] Invalid image path: {}", filePath);
            return;
        }

        log.info("[ImageDelete] Deleting image: {}", filePath);
        try {
            boolean deleted = Files.deleteIfExists(filePath);
            log.info("[ImageDelete] Result: {}", deleted ? "deleted" : "not found");
        } catch (IOException e) {
            log.warn("[ImageDelete] Failed to delete image: {}", filePath, e);
        }
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf("."));
    }
}
