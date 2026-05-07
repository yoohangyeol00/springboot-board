package com.board.backend.service;

import com.board.backend.dto.BoardCreateRequest;
import com.board.backend.dto.BoardResponse;
import com.board.backend.dto.BoardUpdateRequest;

import java.util.List;

public interface BoardService {

    void create(BoardCreateRequest request);

    List<BoardResponse> getBoards();

    BoardResponse getBoard(Long id);

    void updateBoard(Long id, BoardUpdateRequest request);

    void deleteBoard(Long id);
}