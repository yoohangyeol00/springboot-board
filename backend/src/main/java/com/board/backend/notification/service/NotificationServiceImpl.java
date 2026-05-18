package com.board.backend.notification.service;

import com.board.backend.notification.dto.NotificationResponse;
import com.board.backend.notification.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;

    @Override
    public void notifyComment(Long receiverId, Long boardId, Long commentId, String commenterNickname) {
        String message = commenterNickname + "님이 회원님의 게시글에 댓글을 남겼습니다.";
        notificationMapper.save(receiverId, boardId, commentId, message);
    }

    @Override
    public void notifyReply(Long receiverId, Long boardId, Long commentId, String commenterNickname) {
        String message = commenterNickname + "님이 회원님의 댓글에 답글을 남겼습니다.";
        notificationMapper.save(receiverId, boardId, commentId, message);
    }

    @Override
    public List<NotificationResponse> getNotifications(Long receiverId) {
        return notificationMapper.findByReceiverId(receiverId)
                .stream()
                .map(NotificationResponse::new)
                .toList();
    }

    @Override
    public long countUnread(Long receiverId) {
        return notificationMapper.countUnread(receiverId);
    }

    @Override
    public void markAsRead(Long notificationId, Long receiverId) {
        notificationMapper.markAsRead(notificationId, receiverId);
    }

    @Override
    public void markAllAsRead(Long receiverId) {
        notificationMapper.markAllAsRead(receiverId);
    }
}
