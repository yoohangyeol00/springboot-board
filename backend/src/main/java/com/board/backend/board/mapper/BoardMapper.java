package com.board.backend.board.mapper;

import com.board.backend.board.domain.Board;
import com.board.backend.board.dto.BoardCreateRequest;
import com.board.backend.board.dto.BoardUpdateRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BoardMapper {

    int save(@Param("request") BoardCreateRequest request,
            @Param("memberId") Long memberId);

    List<Board> findAll(@Param("size") int size,
                    @Param("offset") int offset);

    long countAll();

    Board findById(Long id);

    int update(@Param("id") Long id,
            @Param("request") BoardUpdateRequest request);

    int delete(Long id);

    int increaseViewCount(Long id);
}
