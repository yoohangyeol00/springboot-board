import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { memberApi } from '../api/memberApi';
import { MemberMe } from '../types/member';

export default function MyPage() {
  const navigate = useNavigate();
  const [me, setMe] = useState<MemberMe | null>(null);
  const [nickname, setNickname] = useState('');
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [withdrawPassword, setWithdrawPassword] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    memberApi.me()
      .then(res => {
        setMe(res.data);
        setNickname(res.data.nickname);
      })
      .catch(() => {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        alert('로그인이 필요합니다.');
        navigate('/login');
      })
      .finally(() => setLoading(false));
  }, [navigate]);

  const handleProfileSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    try {
      await memberApi.updateMe({ nickname });
      alert('회원 정보가 수정되었습니다.');
      const response = await memberApi.me();
      setMe(response.data);
      setNickname(response.data.nickname);
    } catch (err: any) {
      alert(err?.response?.data?.message || '회원 정보 수정에 실패했습니다.');
    }
  };

  const handlePasswordSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    try {
      await memberApi.updatePassword({ currentPassword, newPassword });
      setCurrentPassword('');
      setNewPassword('');
      alert('비밀번호가 변경되었습니다. 다시 로그인해주세요.');
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      navigate('/login');
    } catch (err: any) {
      alert(err?.response?.data?.message || '비밀번호 변경에 실패했습니다.');
    }
  };

  const handleWithdraw = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!window.confirm('정말 탈퇴하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
      return;
    }

    try {
      await memberApi.withdraw({ password: withdrawPassword });
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      alert('회원 탈퇴가 완료되었습니다.');
      navigate('/');
    } catch (err: any) {
      alert(err?.response?.data?.message || '회원 탈퇴에 실패했습니다.');
    }
  };

  if (loading) {
    return (
      <div className="container">
        <div className="loading">로딩 중...</div>
      </div>
    );
  }

  if (!me) return null;

  return (
    <div className="mypage">
      <section className="container">
        <div className="page-header">
          <h1 className="page-title">마이페이지</h1>
        </div>

        <dl className="profile-summary">
          <div>
            <dt>아이디</dt>
            <dd>{me.loginId}</dd>
          </div>
          <div>
            <dt>권한</dt>
            <dd>{me.role}</dd>
          </div>
        </dl>
      </section>

      <section className="container">
        <h2 className="section-title">회원 정보 수정</h2>
        <form className="board-form" onSubmit={handleProfileSubmit}>
          <div className="form-group">
            <label className="form-label" htmlFor="nickname">닉네임</label>
            <input
              id="nickname"
              className="form-input"
              value={nickname}
              onChange={(e) => setNickname(e.target.value)}
            />
          </div>
          <div className="button-group">
            <span />
            <button className="btn btn-primary" type="submit">저장</button>
          </div>
        </form>
      </section>

      <section className="container">
        <h2 className="section-title">비밀번호 변경</h2>
        <form className="board-form" onSubmit={handlePasswordSubmit}>
          <div className="form-group">
            <label className="form-label" htmlFor="currentPassword">현재 비밀번호</label>
            <input
              id="currentPassword"
              className="form-input"
              type="password"
              value={currentPassword}
              onChange={(e) => setCurrentPassword(e.target.value)}
              autoComplete="current-password"
            />
          </div>
          <div className="form-group">
            <label className="form-label" htmlFor="newPassword">새 비밀번호</label>
            <input
              id="newPassword"
              className="form-input"
              type="password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              autoComplete="new-password"
            />
          </div>
          <div className="button-group">
            <span />
            <button className="btn btn-primary" type="submit">비밀번호 변경</button>
          </div>
        </form>
      </section>

      <section className="container danger-zone">
        <h2 className="section-title">회원 탈퇴</h2>
        <form className="board-form" onSubmit={handleWithdraw}>
          <div className="form-group">
            <label className="form-label" htmlFor="withdrawPassword">비밀번호 확인</label>
            <input
              id="withdrawPassword"
              className="form-input"
              type="password"
              value={withdrawPassword}
              onChange={(e) => setWithdrawPassword(e.target.value)}
              autoComplete="current-password"
            />
          </div>
          <div className="button-group">
            <span />
            <button className="btn btn-danger" type="submit">탈퇴하기</button>
          </div>
        </form>
      </section>
    </div>
  );
}
