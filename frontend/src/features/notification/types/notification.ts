export interface Notification {
  id: number;
  boardId: number;
  commentId: number;
  message: string;
  read: boolean;
  createdAt: string;
}

export interface UnreadCountResponse {
  count: number;
}
