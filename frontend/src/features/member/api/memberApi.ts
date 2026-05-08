import { apiClient } from '../../../shared/api/apiClient';
import {
  MemberLoginRequest,
  MemberLoginResponse,
  MemberMe,
  MemberPasswordUpdateRequest,
  MemberSignupRequest,
  MemberUpdateRequest,
  MemberWithdrawRequest,
} from '../types/member';

export const memberApi = {
  signup: (data: MemberSignupRequest) => apiClient.post('/members/signup', data),
  login: (data: MemberLoginRequest) => apiClient.post<MemberLoginResponse>('/members/login', data),
  me: () => apiClient.get<MemberMe>('/members/me'),
  updateMe: (data: MemberUpdateRequest) => apiClient.patch('/members/me', data),
  updatePassword: (data: MemberPasswordUpdateRequest) => apiClient.patch('/members/me/password', data),
  withdraw: (data: MemberWithdrawRequest) => apiClient.delete('/members/me', { data }),
};
