package com.board.backend.service;

import com.board.backend.dto.BoardCreateRequest;
import com.board.backend.dto.BoardResponse;
import com.board.backend.dto.BoardUpdateRequest;
import com.board.backend.dto.PageResponse;


public interface BoardService {

    void create(BoardCreateRequest request);

    PageResponse<BoardResponse> getBoards(int page, int size);

    BoardResponse getBoard(Long id);

    void updateBoard(Long id, BoardUpdateRequest request);

    void deleteBoard(Long id);
}