import { apiClient } from '../../../shared/api/apiClient';
import {
  MemberLoginRequest,
  MemberLoginResponse,
  MemberMe,
  MemberSignupRequest,
} from '../types/member';

export const memberApi = {
  signup: (data: MemberSignupRequest) => apiClient.post('/members/signup', data),
  login: (data: MemberLoginRequest) => apiClient.post<MemberLoginResponse>('/members/login', data),
  me: () => apiClient.get<MemberMe>('/members/me'),
};
