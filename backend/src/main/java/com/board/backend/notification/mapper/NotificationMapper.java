package com.board.backend.notification.mapper;

import com.board.backend.notification.domain.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationMapper {

    int save(@Param("receiverId") Long receiverId,
             @Param("boardId") Long boardId,
             @Param("commentId") Long commentId,
             @Param("message") String message);

    List<Notification> findByReceiverId(Long receiverId);

    long countUnread(Long receiverId);

    int markAsRead(@Param("id") Long id,
                   @Param("receiverId") Long receiverId);

    int markAllAsRead(Long receiverId);
}
