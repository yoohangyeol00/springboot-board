package com.board.backend.member.service;

import com.board.backend.global.security.JwtTokenProvider;
import com.board.backend.member.domain.Member;
import com.board.backend.member.dto.MemberLoginRequest;
import com.board.backend.member.dto.MemberLoginResponse;
import com.board.backend.member.dto.MemberMeResponse;
import com.board.backend.member.dto.MemberSignupRequest;
import com.board.backend.member.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private static final String ACTIVE_STATUS = "ACTIVE";

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void signup(MemberSignupRequest request) {
        Member existingMember = memberMapper.findByLoginId(request.getLoginId());

        if (existingMember != null) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        String passwordHash = passwordEncoder.encode(request.getPassword());
        int result = memberMapper.save(request.getLoginId(), passwordHash, request.getNickname());

        if (result != 1) {
            throw new IllegalStateException("회원가입에 실패했습니다.");
        }
    }

    @Override
    public MemberLoginResponse login(MemberLoginRequest request) {
        Member member = memberMapper.findByLoginId(request.getLoginId());

        if (member == null) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        if (!ACTIVE_STATUS.equals(member.getStatus())) {
            throw new IllegalArgumentException("사용할 수 없는 계정입니다.");
        }

        if (!passwordEncoder.matches(request.getPassword(), member.getPasswordHash())) {
            memberMapper.increaseFailedLoginCount(member.getId());
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        memberMapper.resetLoginFailure(member.getId());

        String accessToken = jwtTokenProvider.createAccessToken(
                member.getId(),
                member.getLoginId(),
                member.getRole());

        return new MemberLoginResponse(accessToken);
    }

    @Override
    public MemberMeResponse getMe(Long memberId) {
        Member member = memberMapper.findById(memberId);

        if (member == null) {
            throw new IllegalArgumentException("회원 정보를 찾을 수 없습니다.");
        }

        return new MemberMeResponse(member);
    }
}
