package com.board.backend.board.controller;

import com.board.backend.board.dto.BoardCreateRequest;
import com.board.backend.board.dto.BoardResponse;
import com.board.backend.board.dto.BoardUpdateRequest;
import com.board.backend.board.service.BoardService;
import com.board.backend.global.common.PageResponse;
import com.board.backend.global.security.LoginMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @PostMapping
    public void create(
            @Valid @RequestBody BoardCreateRequest request,
            @AuthenticationPrincipal LoginMember loginMember) {
        boardService.create(request, loginMember.getId());
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
}
