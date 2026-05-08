import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { memberApi } from '../api/memberApi';

export default function Login() {
  const navigate = useNavigate();
  const [loginId, setLoginId] = useState('');
  const [password, setPassword] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);

    try {
      const response = await memberApi.login({ loginId, password });
      localStorage.setItem('accessToken', response.data.accessToken);
      navigate('/');
    } catch (err: any) {
      alert(err?.response?.data?.message || '아이디 또는 비밀번호가 올바르지 않습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-container">
      <form className="auth-form" onSubmit={handleSubmit}>
        <h1 className="auth-title">로그인</h1>

        <label className="form-label" htmlFor="loginId">아이디</label>
        <input
          id="loginId"
          className="form-input"
          value={loginId}
          onChange={(e) => setLoginId(e.target.value)}
          autoComplete="username"
        />

        <label className="form-label" htmlFor="password">비밀번호</label>
        <input
          id="password"
          className="form-input"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          autoComplete="current-password"
        />

        <button className="btn btn-primary auth-submit" type="submit" disabled={submitting}>
          {submitting ? '로그인 중...' : '로그인'}
        </button>

        <Link className="auth-link" to="/signup">회원가입</Link>
      </form>
    </div>
  );
}
