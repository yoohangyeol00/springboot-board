package com.board.backend.service;

import com.board.backend.domain.Board;
import com.board.backend.dto.BoardCreateRequest;
import com.board.backend.dto.BoardResponse;
import com.board.backend.dto.BoardUpdateRequest;
import com.board.backend.dto.PageResponse;
import com.board.backend.mapper.BoardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.board.backend.exception.BoardCreateFailedException;
import com.board.backend.exception.BoardDeleteFailedException;
import com.board.backend.exception.BoardNotFoundException;
import com.board.backend.exception.BoardUpdateFailedException;



import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

    private final BoardMapper boardMapper;

    @Override
    public void create(BoardCreateRequest request) {
        int result = boardMapper.save(request);

        if (result != 1) {
            throw new BoardCreateFailedException();
        }
    }

    @Override
    public PageResponse<BoardResponse> getBoards(int page, int size) {

        int offset = (page - 1) * size;

        List<BoardResponse> boards = boardMapper.findAll(size, offset)
                .stream()
                .map(BoardResponse::new)
                .toList();

        long totalCount = boardMapper.countAll();

        return new PageResponse<>(boards, page, size, totalCount);
    }

    @Override
    public BoardResponse getBoard(Long id) {
        Board board = boardMapper.findById(id);

        if (board == null) {
            throw new BoardNotFoundException();
        }

        boardMapper.increaseViewCount(id);

        Board updatedBoard = boardMapper.findById(id);

        return new BoardResponse(updatedBoard);
    }

    @Override
    public void updateBoard(Long id, BoardUpdateRequest request) {
        Board board = boardMapper.findById(id);

        if (board == null) {
            throw new BoardNotFoundException();
        }

        int result = boardMapper.update(id, request);

        if (result != 1) {
            throw new BoardUpdateFailedException();
        }
    }

    @Override
    public void deleteBoard(Long id) {
        Board board = boardMapper.findById(id);

        if (board == null) {
            throw new BoardNotFoundException();
        }

        int result = boardMapper.delete(id);

        if (result != 1) {
            throw new BoardDeleteFailedException();
        }
    }
}