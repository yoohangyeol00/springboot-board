package com.board.backend.board.service;

import com.board.backend.attachment.service.BoardAttachmentService;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

    private final BoardMapper boardMapper;
    private final ImageService imageService;
    private final BoardAttachmentService attachmentService;

    @Override
    @Transactional
    public BoardResponse create(BoardCreateRequest request, Long memberId) {
        int result = boardMapper.save(request, memberId);

        if (result != 1 || request.getId() == null) {
            throw new BoardCreateFailedException();
        }

        return getBoardWithoutIncreasingViewCount(request.getId());
    }

    @Override
    public PageResponse<BoardResponse> getBoards(int page, int size, String searchType, String keyword) {
        int offset = (page - 1) * size;
        String normalizedSearchType = normalizeSearchType(searchType);
        String normalizedKeyword = normalizeKeyword(keyword);

        List<BoardResponse> boards = boardMapper.findAll(size, offset, normalizedSearchType, normalizedKeyword)
                .stream()
                .map(BoardResponse::new)
                .toList();

        long totalCount = boardMapper.countAll(normalizedSearchType, normalizedKeyword);

        return new PageResponse<>(boards, page, size, totalCount);
    }

    @Override
    public List<BoardResponse> getPopularBoards(int limit) {
        int normalizedLimit = Math.max(1, Math.min(limit, 10));

        return boardMapper.findPopular(normalizedLimit)
                .stream()
                .map(BoardResponse::new)
                .toList();
    }

    @Override
    public BoardResponse getBoard(Long id) {
        Board board = boardMapper.findById(id);

        if (board == null) {
            throw new BoardNotFoundException();
        }

        boardMapper.increaseViewCount(id);

        Board updatedBoard = boardMapper.findById(id);

        return new BoardResponse(updatedBoard, attachmentService.getAttachments(id));
    }

    @Override
    @Transactional
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
    @Transactional
    public void deleteBoard(Long id, Long memberId) {
        Board board = boardMapper.findById(id);

        if (board == null) {
            throw new BoardNotFoundException();
        }

        validateOwner(board, memberId);

        List<String> attachmentStoredNames = attachmentService.getStoredNamesByBoardId(id);

        imageService.deleteImages(board.getContent());

        int result = boardMapper.delete(id);

        if (result != 1) {
            throw new BoardDeleteFailedException();
        }

        attachmentService.deleteFiles(attachmentStoredNames);
    }

    private BoardResponse getBoardWithoutIncreasingViewCount(Long id) {
        Board board = boardMapper.findById(id);

        if (board == null) {
            throw new BoardNotFoundException();
        }

        return new BoardResponse(board, attachmentService.getAttachments(id));
    }

    private void validateOwner(Board board, Long memberId) {
        if (board.getMemberId() == null || !board.getMemberId().equals(memberId)) {
            throw new AccessDeniedException("You can edit or delete only your own board.");
        }
    }

    private String normalizeSearchType(String searchType) {
        if (searchType == null) {
            return "all";
        }

        return switch (searchType) {
            case "title", "content", "writer" -> searchType;
            default -> "all";
        };
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }

        return keyword.trim();
    }
}
