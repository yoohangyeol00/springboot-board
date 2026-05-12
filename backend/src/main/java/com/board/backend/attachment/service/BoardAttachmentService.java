package com.board.backend.attachment.service;

import com.board.backend.attachment.domain.BoardAttachment;
import com.board.backend.attachment.dto.AttachmentResponse;
import com.board.backend.attachment.dto.StoredAttachmentFile;
import com.board.backend.attachment.exception.AttachmentDeleteFailedException;
import com.board.backend.attachment.exception.AttachmentNotFoundException;
import com.board.backend.attachment.exception.AttachmentSaveFailedException;
import com.board.backend.attachment.exception.AttachmentUpdateFailedException;
import com.board.backend.attachment.mapper.BoardAttachmentMapper;
import com.board.backend.board.domain.Board;
import com.board.backend.board.exception.BoardNotFoundException;
import com.board.backend.board.mapper.BoardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardAttachmentService {

    private static final int MAX_ATTACHMENTS_PER_BOARD = 10;
    private static final long MAX_FILE_SIZE = 10L * 1024L * 1024L;

    private final BoardAttachmentMapper attachmentMapper;
    private final BoardMapper boardMapper;
    private final AttachmentStorageService storageService;

    public List<AttachmentResponse> getAttachments(Long boardId) {
        return attachmentMapper.findByBoardId(boardId)
                .stream()
                .map(AttachmentResponse::new)
                .toList();
    }

    @Transactional
    public List<AttachmentResponse> addAttachments(Long boardId, List<MultipartFile> files, Long memberId) {
        validateOwner(boardId, memberId);
        validateFiles(files);
        validateAttachmentLimit(boardId, files.size());

        List<String> savedStoredNames = new ArrayList<>();

        try {
            for (MultipartFile file : files) {
                StoredAttachmentFile storedFile = storageService.save(file);
                savedStoredNames.add(storedFile.getStoredName());

                BoardAttachment attachment = toAttachment(boardId, storedFile);
                int result = attachmentMapper.save(attachment);

                if (result != 1) {
                    throw new AttachmentSaveFailedException();
                }
            }
        } catch (IOException | RuntimeException e) {
            deleteFiles(savedStoredNames);

            if (e instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }

            throw new IllegalStateException("Failed to store attachment file.", e);
        }

        return getAttachments(boardId);
    }

    @Transactional
    public void deleteAttachment(Long boardId, Long attachmentId, Long memberId) {
        validateOwner(boardId, memberId);

        BoardAttachment attachment = findAttachment(boardId, attachmentId);
        int result = attachmentMapper.delete(attachmentId, boardId);

        if (result != 1) {
            throw new AttachmentDeleteFailedException();
        }

        storageService.deleteQuietly(attachment.getStoredName());
    }

    @Transactional
    public AttachmentResponse replaceAttachment(
            Long boardId,
            Long attachmentId,
            MultipartFile file,
            Long memberId) {
        validateOwner(boardId, memberId);
        validateFile(file);

        BoardAttachment oldAttachment = findAttachment(boardId, attachmentId);
        StoredAttachmentFile storedFile = null;

        try {
            storedFile = storageService.save(file);
            BoardAttachment newAttachment = toAttachment(boardId, storedFile);
            newAttachment.setId(attachmentId);

            int result = attachmentMapper.updateFile(newAttachment);
            if (result != 1) {
                throw new AttachmentUpdateFailedException();
            }

            storageService.deleteQuietly(oldAttachment.getStoredName());

            return new AttachmentResponse(findAttachment(boardId, attachmentId));
        } catch (IOException | RuntimeException e) {
            if (storedFile != null) {
                storageService.deleteQuietly(storedFile.getStoredName());
            }

            if (e instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }

            throw new IllegalStateException("Failed to replace attachment file.", e);
        }
    }

    public BoardAttachment getDownloadAttachment(Long boardId, Long attachmentId) {
        validateBoardExists(boardId);
        return findAttachment(boardId, attachmentId);
    }

    public Resource getDownloadResource(BoardAttachment attachment) {
        PathResource resource = new PathResource(storageService.resolve(attachment.getStoredName()));

        if (!resource.exists() || !resource.isReadable()) {
            throw new AttachmentNotFoundException();
        }

        return resource;
    }

    public String getContentType(BoardAttachment attachment) {
        if (attachment.getContentType() != null && !attachment.getContentType().isBlank()) {
            return attachment.getContentType();
        }

        try {
            String probedContentType = Files.probeContentType(storageService.resolve(attachment.getStoredName()));
            return probedContentType != null ? probedContentType : "application/octet-stream";
        } catch (IOException e) {
            return "application/octet-stream";
        }
    }

    public void deleteFilesByBoardId(Long boardId) {
        attachmentMapper.findByBoardId(boardId)
                .forEach(attachment -> storageService.deleteQuietly(attachment.getStoredName()));
    }

    public List<String> getStoredNamesByBoardId(Long boardId) {
        return attachmentMapper.findByBoardId(boardId)
                .stream()
                .map(BoardAttachment::getStoredName)
                .toList();
    }

    public void deleteFiles(List<String> storedNames) {
        storedNames.forEach(storageService::deleteQuietly);
    }

    private BoardAttachment findAttachment(Long boardId, Long attachmentId) {
        BoardAttachment attachment = attachmentMapper.findByIdAndBoardId(attachmentId, boardId);

        if (attachment == null) {
            throw new AttachmentNotFoundException();
        }

        return attachment;
    }

    private void validateBoardExists(Long boardId) {
        if (boardMapper.findById(boardId) == null) {
            throw new BoardNotFoundException();
        }
    }

    private void validateOwner(Long boardId, Long memberId) {
        Board board = boardMapper.findById(boardId);

        if (board == null) {
            throw new BoardNotFoundException();
        }

        if (board.getMemberId() == null || !board.getMemberId().equals(memberId)) {
            throw new AccessDeniedException("You do not have permission to manage attachments.");
        }
    }

    private void validateAttachmentLimit(Long boardId, int newFileCount) {
        int currentCount = attachmentMapper.countByBoardId(boardId);

        if (currentCount + newFileCount > MAX_ATTACHMENTS_PER_BOARD) {
            throw new IllegalArgumentException("A board can have up to 10 attachments.");
        }
    }

    private void validateFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("Please select at least one file.");
        }

        files.forEach(this::validateFile);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Empty files cannot be uploaded.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Each attachment must be 10MB or smaller.");
        }
    }

    private BoardAttachment toAttachment(Long boardId, StoredAttachmentFile storedFile) {
        BoardAttachment attachment = new BoardAttachment();
        attachment.setBoardId(boardId);
        attachment.setOriginalName(storedFile.getOriginalName());
        attachment.setStoredName(storedFile.getStoredName());
        attachment.setFileUrl(storedFile.getFileUrl());
        attachment.setFileSize(storedFile.getFileSize());
        attachment.setContentType(storedFile.getContentType());
        return attachment;
    }
}
