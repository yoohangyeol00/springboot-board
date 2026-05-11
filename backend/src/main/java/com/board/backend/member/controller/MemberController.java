package com.board.backend.member.controller;

import com.board.backend.global.security.LoginMember;
import com.board.backend.member.dto.MemberLoginRequest;
import com.board.backend.member.dto.MemberLoginResponse;
import com.board.backend.member.dto.MemberMeResponse;
import com.board.backend.member.dto.MemberPasswordUpdateRequest;
import com.board.backend.member.dto.RefreshTokenRequest;
import com.board.backend.member.dto.MemberSignupRequest;
import com.board.backend.member.dto.MemberUpdateRequest;
import com.board.backend.member.dto.MemberWithdrawRequest;
import com.board.backend.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public void signup(@Valid @RequestBody MemberSignupRequest request) {
        memberService.signup(request);
    }

    @PostMapping("/login")
    public MemberLoginResponse login(@Valid @RequestBody MemberLoginRequest request) {
        return memberService.login(request);
    }

    @PostMapping("/refresh")
    public MemberLoginResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return memberService.refresh(request);
    }

    @PostMapping("/logout")
    public void logout(@Valid @RequestBody RefreshTokenRequest request) {
        memberService.logout(request);
    }

    @GetMapping("/me")
    public MemberMeResponse me(@AuthenticationPrincipal LoginMember loginMember) {
        return memberService.getMe(loginMember.getId());
    }

    @PatchMapping("/me")
    public void updateMe(
            @AuthenticationPrincipal LoginMember loginMember,
            @Valid @RequestBody MemberUpdateRequest request) {
        memberService.updateMe(loginMember.getId(), request);
    }

    @PatchMapping("/me/password")
    public void updatePassword(
            @AuthenticationPrincipal LoginMember loginMember,
            @Valid @RequestBody MemberPasswordUpdateRequest request) {
        memberService.updatePassword(loginMember.getId(), request);
    }

    @DeleteMapping("/me")
    public void withdraw(
            @AuthenticationPrincipal LoginMember loginMember,
            @Valid @RequestBody MemberWithdrawRequest request) {
        memberService.withdraw(loginMember.getId(), request);
    }
}
