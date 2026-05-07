package com.board.backend.mapper;

import com.board.backend.domain.Board;
import com.board.backend.dto.BoardCreateRequest;
import com.board.backend.dto.BoardUpdateRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BoardMapper {

    int save(BoardCreateRequest request);

    List<Board> findAll();

    Board findById(Long id);


    int update(@Param("id") Long id,
            @Param("request") BoardUpdateRequest request);

    int delete(Long id);

    int increaseViewCount(Long id);
}