import { apiClient } from '../../../shared/api/apiClient';
import { Notification, UnreadCountResponse } from '../types/notification';

export const notificationApi = {
  getAll: () => apiClient.get<Notification[]>('/notifications'),
  getUnreadCount: () => apiClient.get<UnreadCountResponse>('/notifications/unread-count'),
  markAsRead: (id: number) => apiClient.patch(`/notifications/${id}/read`),
  markAllAsRead: () => apiClient.patch('/notifications/read-all'),
};
