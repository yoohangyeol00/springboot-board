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
}

export interface MemberMe {
  id: number;
  loginId: string;
  nickname: string;
  role: string;
}
