package com.board.backend.member.service;

import com.board.backend.member.dto.MemberLoginRequest;
import com.board.backend.member.dto.MemberLoginResponse;
import com.board.backend.member.dto.MemberMeResponse;
import com.board.backend.member.dto.MemberPasswordUpdateRequest;
import com.board.backend.member.dto.RefreshTokenRequest;
import com.board.backend.member.dto.MemberSignupRequest;
import com.board.backend.member.dto.MemberUpdateRequest;
import com.board.backend.member.dto.MemberWithdrawRequest;

public interface MemberService {

    void signup(MemberSignupRequest request);

    MemberLoginResponse login(MemberLoginRequest request);

    MemberLoginResponse refresh(RefreshTokenRequest request);

    void logout(RefreshTokenRequest request);

    MemberMeResponse getMe(Long memberId);

    void updateMe(Long memberId, MemberUpdateRequest request);

    void updatePassword(Long memberId, MemberPasswordUpdateRequest request);

    void withdraw(Long memberId, MemberWithdrawRequest request);
}
