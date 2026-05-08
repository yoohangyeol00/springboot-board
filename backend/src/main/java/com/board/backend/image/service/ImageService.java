package com.board.backend.image.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class ImageService {

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
        if (content == null || !content.contains("/uploads/")) return;

        Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
        Pattern pattern = Pattern.compile("/uploads/[^\\s\"')\\]]+");
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String filename = matcher.group().substring("/uploads/".length());
            Path filePath = uploadPath.resolve(filename);
            log.info("[ImageDelete] 삭제 시도: {}", filePath);
            try {
                boolean deleted = Files.deleteIfExists(filePath);
                log.info("[ImageDelete] 결과: {}", deleted ? "삭제 성공" : "파일 없음");
            } catch (IOException e) {
                log.error("[ImageDelete] 삭제 실패: {}", filePath, e);
            }
        }
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf("."));
    }
}
