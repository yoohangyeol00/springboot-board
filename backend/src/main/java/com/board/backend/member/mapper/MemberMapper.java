package com.board.backend.member.mapper;

import com.board.backend.member.domain.Member;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MemberMapper {

    int save(@Param("loginId") String loginId,
            @Param("passwordHash") String passwordHash,
            @Param("nickname") String nickname);

    Member findById(Long id);

    Member findByLoginId(String loginId);

    Member findByNickname(String nickname);

    int updateNickname(@Param("id") Long id,
            @Param("nickname") String nickname);

    int updatePassword(@Param("id") Long id,
            @Param("passwordHash") String passwordHash);

    int withdraw(Long id);

    int increaseFailedLoginCount(Long id);

    int resetLoginFailure(Long id);
}
