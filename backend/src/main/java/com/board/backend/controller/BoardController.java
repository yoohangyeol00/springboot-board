package com.board.backend.controller;

import com.board.backend.dto.BoardCreateRequest;
import com.board.backend.dto.BoardResponse;
import com.board.backend.dto.BoardUpdateRequest;
import com.board.backend.service.BoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @PostMapping
    public void create(@Valid @RequestBody BoardCreateRequest request) {
        boardService.create(request);
    }

    @GetMapping
    public List<BoardResponse> getBoards() {
        return boardService.getBoards();
    }

    @GetMapping("/{id}")
    public BoardResponse getBoard(@PathVariable Long id) {
        return boardService.getBoard(id);
    }

    @PutMapping("/{id}")
    public void updateBoard(
            @PathVariable Long id,
            @Valid @RequestBody BoardUpdateRequest request
    ) {
        boardService.updateBoard(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteBoard(@PathVariable Long id) {
        boardService.deleteBoard(id);
    }
}