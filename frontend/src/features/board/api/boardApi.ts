import { Board, BoardCreateRequest, BoardUpdateRequest, PageResponse } from '../types/board';
import { apiClient } from '../../../shared/api/apiClient';

export type BoardSearchType = 'all' | 'title' | 'content' | 'writer';

export const boardApi = {
  getAll: (page = 1, size = 10, searchType: BoardSearchType = 'all', keyword = '') =>
    apiClient.get<PageResponse<Board>>('/boards', {
      params: {
        page,
        size,
        searchType,
        keyword,
      },
    }),
  getPopular: (limit = 5) => apiClient.get<Board[]>('/boards/popular', { params: { limit } }),
  getById: (id: number) => apiClient.get<Board>(`/boards/${id}`),
  create: (data: BoardCreateRequest) => apiClient.post('/boards', data),
  update: (id: number, data: BoardUpdateRequest) => apiClient.put(`/boards/${id}`, data),
  delete: (id: number) => apiClient.delete(`/boards/${id}`),
  uploadImage: (blob: Blob) => {
    const formData = new FormData();
    formData.append('image', blob);
    return apiClient.post<{ url: string }>('/images', formData);
  },
};
