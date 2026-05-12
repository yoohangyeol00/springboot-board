package com.board.backend.board.controller;

import com.board.backend.attachment.domain.BoardAttachment;
import com.board.backend.attachment.dto.AttachmentResponse;
import com.board.backend.attachment.service.BoardAttachmentService;
import com.board.backend.board.dto.BoardCreateRequest;
import com.board.backend.board.dto.BoardResponse;
import com.board.backend.board.dto.BoardUpdateRequest;
import com.board.backend.board.service.BoardService;
import com.board.backend.global.common.PageResponse;
import com.board.backend.global.security.LoginMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final BoardAttachmentService attachmentService;

    @PostMapping
    public BoardResponse create(
            @Valid @RequestBody BoardCreateRequest request,
            @AuthenticationPrincipal LoginMember loginMember) {
        return boardService.create(request, loginMember.getId());
    }

    @GetMapping
    public PageResponse<BoardResponse> getBoards(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "all") String searchType,
            @RequestParam(defaultValue = "") String keyword) {
        return boardService.getBoards(page, size, searchType, keyword);
    }

    @GetMapping("/popular")
    public List<BoardResponse> getPopularBoards(
            @RequestParam(defaultValue = "5") int limit) {
        return boardService.getPopularBoards(limit);
    }

    @GetMapping("/{id}")
    public BoardResponse getBoard(@PathVariable Long id) {
        return boardService.getBoard(id);
    }

    @PutMapping("/{id}")
    public void updateBoard(
            @PathVariable Long id,
            @Valid @RequestBody BoardUpdateRequest request,
            @AuthenticationPrincipal LoginMember loginMember) {
        boardService.updateBoard(id, request, loginMember.getId());
    }

    @DeleteMapping("/{id}")
    public void deleteBoard(
            @PathVariable Long id,
            @AuthenticationPrincipal LoginMember loginMember) {
        boardService.deleteBoard(id, loginMember.getId());
    }

    @PostMapping(value = "/{boardId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<AttachmentResponse> addAttachments(
            @PathVariable Long boardId,
            @RequestParam("files") List<MultipartFile> files,
            @AuthenticationPrincipal LoginMember loginMember) {
        return attachmentService.addAttachments(boardId, files, loginMember.getId());
    }

    @DeleteMapping("/{boardId}/attachments/{attachmentId}")
    public void deleteAttachment(
            @PathVariable Long boardId,
            @PathVariable Long attachmentId,
            @AuthenticationPrincipal LoginMember loginMember) {
        attachmentService.deleteAttachment(boardId, attachmentId, loginMember.getId());
    }

    @PutMapping(value = "/{boardId}/attachments/{attachmentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AttachmentResponse replaceAttachment(
            @PathVariable Long boardId,
            @PathVariable Long attachmentId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal LoginMember loginMember) {
        return attachmentService.replaceAttachment(boardId, attachmentId, file, loginMember.getId());
    }

    @GetMapping("/{boardId}/attachments/{attachmentId}/download")
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable Long boardId,
            @PathVariable Long attachmentId) {
        BoardAttachment attachment = attachmentService.getDownloadAttachment(boardId, attachmentId);
        Resource resource = attachmentService.getDownloadResource(attachment);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachmentService.getContentType(attachment)))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(attachment.getOriginalName(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(resource);
    }
}
