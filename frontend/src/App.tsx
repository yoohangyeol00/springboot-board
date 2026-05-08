import { useEffect, useState } from 'react';
import { Routes, Route, Link, useLocation, useNavigate } from 'react-router-dom';
import BoardList from './features/board/pages/BoardList';
import BoardDetail from './features/board/pages/BoardDetail';
import BoardCreate from './features/board/pages/BoardCreate';
import BoardEdit from './features/board/pages/BoardEdit';
import Login from './features/member/pages/Login';
import Signup from './features/member/pages/Signup';
import { memberApi } from './features/member/api/memberApi';
import { MemberMe } from './features/member/types/member';

function App() {
  const navigate = useNavigate();
  const location = useLocation();
  const [me, setMe] = useState<MemberMe | null>(null);

  useEffect(() => {
    const token = localStorage.getItem('accessToken');

    if (!token) {
      setMe(null);
      return;
    }

    memberApi.me()
      .then(res => setMe(res.data))
      .catch(() => {
        localStorage.removeItem('accessToken');
        setMe(null);
      });
  }, [location.pathname]);

  const handleLogout = () => {
    localStorage.removeItem('accessToken');
    setMe(null);
    navigate('/');
  };

  return (
    <div className="app">
      <header className="header">
        <div className="header-inner">
          <Link to="/" className="logo">자유게시판</Link>
          <nav className="header-nav">
            {me ? (
              <>
                <span className="header-user">{me.nickname}</span>
                <button type="button" className="nav-button" onClick={handleLogout}>
                  로그아웃
                </button>
              </>
            ) : (
              <>
                <Link to="/login" className="nav-link">로그인</Link>
                <Link to="/signup" className="nav-link">회원가입</Link>
              </>
            )}
          </nav>
        </div>
      </header>
      <main className="main">
        <Routes>
          <Route path="/" element={<BoardList />} />
          <Route path="/boards/new" element={<BoardCreate />} />
          <Route path="/boards/:id" element={<BoardDetail />} />
          <Route path="/boards/:id/edit" element={<BoardEdit />} />
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<Signup />} />
        </Routes>
      </main>
      <footer className="footer">
        <p>자유게시판</p>
      </footer>
    </div>
  );
}

export default App;
