package com.board.backend.member.service;

import com.board.backend.member.dto.MemberLoginRequest;
import com.board.backend.member.dto.MemberLoginResponse;
import com.board.backend.member.dto.MemberMeResponse;
import com.board.backend.member.dto.MemberSignupRequest;

public interface MemberService {

    void signup(MemberSignupRequest request);

    MemberLoginResponse login(MemberLoginRequest request);

    MemberMeResponse getMe(Long memberId);
}
