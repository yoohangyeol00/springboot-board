package com.board.backend.member.mapper;

import com.board.backend.member.domain.RefreshToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface RefreshTokenMapper {

    int save(@Param("memberId") Long memberId,
             @Param("tokenHash") String tokenHash,
             @Param("expiresAt") LocalDateTime expiresAt);

    RefreshToken findValidByTokenHash(@Param("tokenHash") String tokenHash,
                                      @Param("now") LocalDateTime now);

    int revokeByTokenHash(@Param("tokenHash") String tokenHash);

    int revokeAllByMemberId(@Param("memberId") Long memberId);
}
