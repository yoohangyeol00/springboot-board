import { Routes, Route, Link } from 'react-router-dom';
import BoardList from './features/board/pages/BoardList';
import BoardDetail from './features/board/pages/BoardDetail';
import BoardCreate from './features/board/pages/BoardCreate';
import BoardEdit from './features/board/pages/BoardEdit';

function App() {
  return (
    <div className="app">
      <header className="header">
        <div className="header-inner">
          <Link to="/" className="logo">자유게시판</Link>
        </div>
      </header>
      <main className="main">
        <Routes>
          <Route path="/" element={<BoardList />} />
          <Route path="/boards/new" element={<BoardCreate />} />
          <Route path="/boards/:id" element={<BoardDetail />} />
          <Route path="/boards/:id/edit" element={<BoardEdit />} />
        </Routes>
      </main>
      <footer className="footer">
        <p>© 자유게시판</p>
      </footer>
    </div>
  );
}

export default App;
