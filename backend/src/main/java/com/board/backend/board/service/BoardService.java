package com.board.backend.board.service;

import com.board.backend.board.dto.BoardCreateRequest;
import com.board.backend.board.dto.BoardResponse;
import com.board.backend.board.dto.BoardUpdateRequest;
import com.board.backend.global.common.PageResponse;

public interface BoardService {

    void create(BoardCreateRequest request);

    PageResponse<BoardResponse> getBoards(int page, int size);

    BoardResponse getBoard(Long id);

    void updateBoard(Long id, BoardUpdateRequest request);

    void deleteBoard(Long id);
}
