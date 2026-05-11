import { apiClient } from '../../../shared/api/apiClient';
import { Comment, CommentCreateRequest, CommentUpdateRequest } from '../types/comment';

export const commentApi = {
  getByBoardId: (boardId: number) => apiClient.get<Comment[]>(`/boards/${boardId}/comments`),
  create: (boardId: number, data: CommentCreateRequest) => apiClient.post(`/boards/${boardId}/comments`, data),
  update: (boardId: number, commentId: number, data: CommentUpdateRequest) =>
    apiClient.put(`/boards/${boardId}/comments/${commentId}`, data),
  delete: (boardId: number, commentId: number) =>
    apiClient.delete(`/boards/${boardId}/comments/${commentId}`),
};
