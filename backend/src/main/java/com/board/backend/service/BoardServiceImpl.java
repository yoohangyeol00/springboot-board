package com.board.backend.service;

import com.board.backend.domain.Board;
import com.board.backend.dto.BoardCreateRequest;
import com.board.backend.dto.BoardResponse;
import com.board.backend.dto.BoardUpdateRequest;
import com.board.backend.mapper.BoardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

    private final BoardMapper boardMapper;

    @Override
    public void create(BoardCreateRequest request) {
        boardMapper.save(request);
    }

    @Override
    public List<BoardResponse> getBoards() {
        return boardMapper.findAll()
                .stream()
                .map(BoardResponse::new)
                .toList();
    }

    @Override
    public BoardResponse getBoard(Long id) {

        boardMapper.increaseViewCount(id);

        Board board = boardMapper.findById(id);

        return new BoardResponse(board);
    }

    @Override
    public void updateBoard(Long id, BoardUpdateRequest request) {
        boardMapper.update(id, request);
    }

    @Override
    public void deleteBoard(Long id) {
        boardMapper.delete(id);
    }
}