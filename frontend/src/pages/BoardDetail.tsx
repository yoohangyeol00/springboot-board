import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { boardApi } from '../api/boardApi';
import { Board } from '../types/board';
import { formatDateTime } from '../utils/dateUtils';

export default function BoardDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [board, setBoard] = useState<Board | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!id) return;
    boardApi.getById(Number(id))
      .then(res => setBoard(res.data))
      .catch(() => {
        alert('게시글을 찾을 수 없습니다.');
        navigate('/');
      })
      .finally(() => setLoading(false));
  }, [id, navigate]);

  const handleDelete = async () => {
    if (!window.confirm('정말 삭제하시겠습니까?')) return;
    try {
      await boardApi.delete(Number(id));
      navigate('/');
    } catch {
      alert('삭제에 실패했습니다.');
    }
  };

  if (loading) {
    return (
      <div className="container">
        <div className="loading">로딩 중...</div>
      </div>
    );
  }

  if (!board) return null;

  return (
    <div className="container">
      <div className="detail-header">
        <h1 className="detail-title">{board.title}</h1>
        <div className="detail-meta">
          <span>작성자: <strong>{board.writer}</strong></span>
          <span>작성일: {formatDateTime(board.createdAt)}</span>
          <span>조회수: {board.viewCount}</span>
        </div>
      </div>

      <div className="detail-content">{board.content}</div>

      <div className="button-group">
        <button className="btn btn-secondary" onClick={() => navigate('/')}>
          목록
        </button>
        <div className="button-group-right">
          <button className="btn btn-primary" onClick={() => navigate(`/boards/${id}/edit`)}>
            수정
          </button>
          <button className="btn btn-danger" onClick={handleDelete}>
            삭제
          </button>
        </div>
      </div>
    </div>
  );
}
