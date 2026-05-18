package com.board.backend.notification.service;

import com.board.backend.notification.dto.NotificationResponse;

import java.util.List;

public interface NotificationService {

    void notifyComment(Long receiverId, Long boardId, Long commentId, String commenterNickname);

    void notifyReply(Long receiverId, Long boardId, Long commentId, String commenterNickname);

    List<NotificationResponse> getNotifications(Long receiverId);

    long countUnread(Long receiverId);

    void markAsRead(Long notificationId, Long receiverId);

    void markAllAsRead(Long receiverId);
}
