package com.board.backend.member.service;

import com.board.backend.global.security.JwtTokenProvider;
import com.board.backend.member.domain.Member;
import com.board.backend.member.dto.MemberLoginRequest;
import com.board.backend.member.dto.MemberLoginResponse;
import com.board.backend.member.dto.MemberMeResponse;
import com.board.backend.member.dto.MemberPasswordUpdateRequest;
import com.board.backend.member.dto.MemberSignupRequest;
import com.board.backend.member.dto.MemberUpdateRequest;
import com.board.backend.member.dto.MemberWithdrawRequest;
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

        Member existingNickname = memberMapper.findByNickname(request.getNickname());

        if (existingNickname != null) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
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
        return new MemberMeResponse(getActiveMember(memberId));
    }

    @Override
    public void updateMe(Long memberId, MemberUpdateRequest request) {
        Member member = getActiveMember(memberId);
        Member existingNickname = memberMapper.findByNickname(request.getNickname());

        if (existingNickname != null && !existingNickname.getId().equals(member.getId())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        int result = memberMapper.updateNickname(memberId, request.getNickname());

        if (result != 1) {
            throw new IllegalStateException("회원 정보 수정에 실패했습니다.");
        }
    }

    @Override
    public void updatePassword(Long memberId, MemberPasswordUpdateRequest request) {
        Member member = getActiveMember(memberId);

        if (!passwordEncoder.matches(request.getCurrentPassword(), member.getPasswordHash())) {
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }

        String passwordHash = passwordEncoder.encode(request.getNewPassword());
        int result = memberMapper.updatePassword(memberId, passwordHash);

        if (result != 1) {
            throw new IllegalStateException("비밀번호 변경에 실패했습니다.");
        }
    }

    @Override
    public void withdraw(Long memberId, MemberWithdrawRequest request) {
        Member member = getActiveMember(memberId);

        if (!passwordEncoder.matches(request.getPassword(), member.getPasswordHash())) {
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
        }

        int result = memberMapper.withdraw(memberId);

        if (result != 1) {
            throw new IllegalStateException("회원 탈퇴에 실패했습니다.");
        }
    }

    private Member getActiveMember(Long memberId) {
        Member member = memberMapper.findById(memberId);

        if (member == null || !ACTIVE_STATUS.equals(member.getStatus())) {
            throw new IllegalArgumentException("회원 정보를 찾을 수 없습니다.");
        }

        return member;
    }
}
