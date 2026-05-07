import axios from 'axios';
import { Board, BoardCreateRequest, BoardUpdateRequest } from '../types/board';

const api = axios.create({
  baseURL: '/api',
});

export const boardApi = {
  getAll: () => api.get<Board[]>('/boards'),
  getById: (id: number) => api.get<Board>(`/boards/${id}`),
  create: (data: BoardCreateRequest) => api.post('/boards', data),
  update: (id: number, data: BoardUpdateRequest) => api.put(`/boards/${id}`, data),
  delete: (id: number) => api.delete(`/boards/${id}`),
};
