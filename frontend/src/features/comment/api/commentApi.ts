import { apiClient } from '../../../shared/api/apiClient';
import { Comment, CommentCreateRequest, CommentUpdateRequest } from '../types/comment';

export const commentApi = {
  getByBoardId: (boardId: number) => apiClient.get<Comment[]>(`/boards/${boardId}/comments`),
  create: (boardId: number, data: CommentCreateRequest) => apiClient.post(`/boards/${boardId}/comments`, data),
  update: (id: number, data: CommentUpdateRequest) => apiClient.put(`/comments/${id}`, data),
  delete: (id: number) => apiClient.delete(`/comments/${id}`),
};
