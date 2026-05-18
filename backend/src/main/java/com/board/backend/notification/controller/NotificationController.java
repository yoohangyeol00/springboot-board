package com.board.backend.notification.controller;

import com.board.backend.global.security.LoginMember;
import com.board.backend.notification.dto.NotificationResponse;
import com.board.backend.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public List<NotificationResponse> getNotifications(
            @AuthenticationPrincipal LoginMember loginMember) {
        return notificationService.getNotifications(loginMember.getId());
    }

    @GetMapping("/unread-count")
    public Map<String, Long> countUnread(
            @AuthenticationPrincipal LoginMember loginMember) {
        return Map.of("count", notificationService.countUnread(loginMember.getId()));
    }

    @PatchMapping("/{notificationId}/read")
    public void markAsRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal LoginMember loginMember) {
        notificationService.markAsRead(notificationId, loginMember.getId());
    }

    @PatchMapping("/read-all")
    public void markAllAsRead(
            @AuthenticationPrincipal LoginMember loginMember) {
        notificationService.markAllAsRead(loginMember.getId());
    }
}
