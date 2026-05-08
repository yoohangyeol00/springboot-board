package com.board.backend.board.service;

import com.board.backend.board.domain.Board;
import com.board.backend.board.dto.BoardCreateRequest;
import com.board.backend.board.dto.BoardResponse;
import com.board.backend.board.dto.BoardUpdateRequest;
import com.board.backend.board.exception.BoardCreateFailedException;
import com.board.backend.board.exception.BoardDeleteFailedException;
import com.board.backend.board.exception.BoardNotFoundException;
import com.board.backend.board.exception.BoardUpdateFailedException;
import com.board.backend.board.mapper.BoardMapper;
import com.board.backend.global.common.PageResponse;
import com.board.backend.image.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

    private final BoardMapper boardMapper;
    private final ImageService imageService;

    @Override
    public void create(BoardCreateRequest request, Long memberId) {
        int result = boardMapper.save(request, memberId);

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
    public void updateBoard(Long id, BoardUpdateRequest request, Long memberId) {
        Board board = boardMapper.findById(id);

        if (board == null) {
            throw new BoardNotFoundException();
        }

        validateOwner(board, memberId);

        int result = boardMapper.update(id, request);

        if (result != 1) {
            throw new BoardUpdateFailedException();
        }
    }

    @Override
    public void deleteBoard(Long id, Long memberId) {
        Board board = boardMapper.findById(id);

        if (board == null) {
            throw new BoardNotFoundException();
        }

        validateOwner(board, memberId);

        imageService.deleteImages(board.getContent());

        int result = boardMapper.delete(id);

        if (result != 1) {
            throw new BoardDeleteFailedException();
        }
    }

    private void validateOwner(Board board, Long memberId) {
        if (board.getMemberId() == null || !board.getMemberId().equals(memberId)) {
            throw new AccessDeniedException("본인이 작성한 글만 수정/삭제할 수 있습니다.");
        }
    }
}
