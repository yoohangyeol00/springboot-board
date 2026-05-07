import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { boardApi } from '../api/boardApi';
import { Board } from '../types/board';
import { formatDate } from '../utils/dateUtils';

export default function BoardList() {
  const [boards, setBoards] = useState<Board[]>([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    boardApi.getAll()
      .then(res => setBoards(res.data))
      .catch(() => alert('게시글을 불러오는데 실패했습니다.'))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="container">
      <div className="page-header">
        <h1 className="page-title">자유게시판</h1>
        <button className="btn btn-primary" onClick={() => navigate('/boards/new')}>
          글쓰기
        </button>
      </div>

      {loading ? (
        <div className="loading">로딩 중...</div>
      ) : (
        <table className="board-table">
          <thead>
            <tr>
              <th className="col-no">번호</th>
              <th className="col-title">제목</th>
              <th className="col-writer">작성자</th>
              <th className="col-views">조회수</th>
              <th className="col-date">작성일</th>
            </tr>
          </thead>
          <tbody>
            {boards.length === 0 ? (
              <tr>
                <td colSpan={5} className="empty-message">
                  등록된 게시글이 없습니다.
                </td>
              </tr>
            ) : (
              boards.map((board, index) => (
                <tr key={board.id} onClick={() => navigate(`/boards/${board.id}`)}>
                  <td className="center">{boards.length - index}</td>
                  <td className="title-cell">{board.title}</td>
                  <td className="center">{board.writer}</td>
                  <td className="center">{board.viewCount}</td>
                  <td className="center">{formatDate(board.createdAt)}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      )}
    </div>
  );
}
