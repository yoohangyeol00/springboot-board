import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { memberApi } from '../api/memberApi';

export default function Signup() {
  const navigate = useNavigate();
  const [loginId, setLoginId] = useState('');
  const [password, setPassword] = useState('');
  const [nickname, setNickname] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);

    try {
      await memberApi.signup({ loginId, password, nickname });
      alert('회원가입이 완료되었습니다. 로그인해주세요.');
      navigate('/login');
    } catch (err: any) {
      alert(err?.response?.data?.message || '회원가입에 실패했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-container">
      <form className="auth-form" onSubmit={handleSubmit}>
        <h1 className="auth-title">회원가입</h1>

        <label className="form-label" htmlFor="signup-loginId">아이디</label>
        <input
          id="signup-loginId"
          className="form-input"
          value={loginId}
          onChange={(e) => setLoginId(e.target.value)}
          autoComplete="username"
        />

        <label className="form-label" htmlFor="signup-password">비밀번호</label>
        <input
          id="signup-password"
          className="form-input"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          autoComplete="new-password"
        />

        <label className="form-label" htmlFor="nickname">닉네임</label>
        <input
          id="nickname"
          className="form-input"
          value={nickname}
          onChange={(e) => setNickname(e.target.value)}
          autoComplete="nickname"
        />

        <button className="btn btn-primary auth-submit" type="submit" disabled={submitting}>
          {submitting ? '가입 중...' : '가입하기'}
        </button>

        <Link className="auth-link" to="/login">이미 계정이 있어요</Link>
      </form>
    </div>
  );
}
