package com.board.backend.board.service;

import com.board.backend.board.dto.BoardCreateRequest;
import com.board.backend.board.dto.BoardResponse;
import com.board.backend.board.dto.BoardUpdateRequest;
import com.board.backend.global.common.PageResponse;

import java.util.List;

public interface BoardService {

    void create(BoardCreateRequest request, Long memberId);

    PageResponse<BoardResponse> getBoards(int page, int size, String searchType, String keyword);

    List<BoardResponse> getPopularBoards(int limit);

    BoardResponse getBoard(Long id);

    void updateBoard(Long id, BoardUpdateRequest request, Long memberId);

    void deleteBoard(Long id, Long memberId);
}
