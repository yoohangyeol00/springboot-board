import { useEffect, useState } from 'react';
import { Routes, Route, Link, useLocation, useNavigate } from 'react-router-dom';
import BoardList from './features/board/pages/BoardList';
import BoardDetail from './features/board/pages/BoardDetail';
import BoardCreate from './features/board/pages/BoardCreate';
import BoardEdit from './features/board/pages/BoardEdit';
import Login from './features/member/pages/Login';
import Signup from './features/member/pages/Signup';
import MyPage from './features/member/pages/MyPage';
import { memberApi } from './features/member/api/memberApi';
import { MemberMe } from './features/member/types/member';
import { notificationApi } from './features/notification/api/notificationApi';
import { Notification } from './features/notification/types/notification';
import { formatDate } from './shared/utils/dateUtils';

function NotFoundPage() {
  const title = '\uc694\uccad\ud558\uc2e0 \uc8fc\uc18c\ub97c \ucc3e\uc744 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4.';
  const description = '\uc785\ub825\ud558\uc2e0 URL\uc774 \uc874\uc7ac\ud558\uc9c0 \uc54a\uac70\ub098 \uc774\ub3d9\ub41c \ud398\uc774\uc9c0\uc785\ub2c8\ub2e4.';

  return (
    <section className="not-found" aria-labelledby="not-found-title">
      <p className="not-found-code">404</p>
      <h1 id="not-found-title" className="not-found-title">{title}</h1>
      <p className="not-found-description">{description}</p>
      <Link to="/" className="btn btn-primary">Home</Link>
    </section>
  );
}

function App() {
  const navigate = useNavigate();
  const location = useLocation();
  const [me, setMe] = useState<MemberMe | null>(null);
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [notificationOpen, setNotificationOpen] = useState(false);

  const loadNotifications = async () => {
    if (!localStorage.getItem('accessToken')) {
      setNotifications([]);
      setUnreadCount(0);
      return;
    }

    const [notificationRes, countRes] = await Promise.all([
      notificationApi.getAll(),
      notificationApi.getUnreadCount(),
    ]);

    setNotifications(notificationRes.data);
    setUnreadCount(countRes.data.count);
  };

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
        localStorage.removeItem('refreshToken');
        setMe(null);
      });
  }, [location.pathname]);

  useEffect(() => {
    if (!me) {
      setNotifications([]);
      setUnreadCount(0);
      setNotificationOpen(false);
      return;
    }

    loadNotifications().catch(() => {
      setNotifications([]);
      setUnreadCount(0);
    });

    const timerId = window.setInterval(() => {
      loadNotifications().catch(() => undefined);
    }, 30000);

    return () => window.clearInterval(timerId);
  }, [me?.id]);

  const handleToggleNotifications = async () => {
    const nextOpen = !notificationOpen;
    setNotificationOpen(nextOpen);

    if (nextOpen) {
      await loadNotifications().catch(() => undefined);
    }
  };

  const handleNotificationClick = async (notification: Notification) => {
    if (!notification.read) {
      await notificationApi.markAsRead(notification.id).catch(() => undefined);
    }

    setNotificationOpen(false);
    await loadNotifications().catch(() => undefined);
    navigate(`/boards/${notification.boardId}`);
  };

  const handleMarkAllAsRead = async () => {
    await notificationApi.markAllAsRead();
    await loadNotifications();
  };

  const handleLogout = async () => {
    const refreshToken = localStorage.getItem('refreshToken');

    if (refreshToken) {
      try {
        await memberApi.logout({ refreshToken });
      } catch {
        // Always clear the client session, even if the refresh token was already invalid.
      }
    }

    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    setMe(null);
    setNotifications([]);
    setUnreadCount(0);
    setNotificationOpen(false);
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
                <div className="notification-menu">
                  <button
                    type="button"
                    className="notification-button"
                    onClick={handleToggleNotifications}
                    aria-label="알림"
                  >
                    알림
                    {unreadCount > 0 && (
                      <span className="notification-badge">{unreadCount > 99 ? '99+' : unreadCount}</span>
                    )}
                  </button>
                  {notificationOpen && (
                    <div className="notification-panel">
                      <div className="notification-panel-header">
                        <strong>알림</strong>
                        {unreadCount > 0 && (
                          <button type="button" className="notification-read-all" onClick={handleMarkAllAsRead}>
                            모두 읽음
                          </button>
                        )}
                      </div>
                      <div className="notification-list">
                        {notifications.length === 0 ? (
                          <div className="notification-empty">새 알림이 없습니다.</div>
                        ) : (
                          notifications.map(notification => (
                            <button
                              key={notification.id}
                              type="button"
                              className={`notification-item${notification.read ? '' : ' notification-item-unread'}`}
                              onClick={() => handleNotificationClick(notification)}
                            >
                              <span>{notification.message}</span>
                              <small>{formatDate(notification.createdAt)}</small>
                            </button>
                          ))
                        )}
                      </div>
                    </div>
                  )}
                </div>
                <span className="header-user">{me.nickname}</span>
                <Link to="/mypage" className="nav-link">마이페이지</Link>
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
          <Route path="/mypage" element={<MyPage />} />
          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </main>
      <footer className="footer">
        <p>자유게시판</p>
      </footer>
    </div>
  );
}

export default App;
