export interface MemberSignupRequest {
  loginId: string;
  password: string;
  nickname: string;
}

export interface MemberLoginRequest {
  loginId: string;
  password: string;
}

export interface MemberLoginResponse {
  accessToken: string;
  refreshToken: string;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface MemberMe {
  id: number;
  loginId: string;
  nickname: string;
  role: string;
}

export interface MemberUpdateRequest {
  nickname: string;
}

export interface MemberPasswordUpdateRequest {
  currentPassword: string;
  newPassword: string;
}

export interface MemberWithdrawRequest {
  password: string;
}
