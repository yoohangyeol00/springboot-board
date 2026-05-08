import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { boardApi } from '../api/boardApi';
import { Board, PageResponse } from '../types/board';
import { formatDate } from '../../../shared/utils/dateUtils';

const SIZE = 10;
const PAGE_GROUP = 10;

export default function BoardList() {
  const [pageData, setPageData] = useState<PageResponse<Board> | null>(null);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    setLoading(true);
    boardApi.getAll(page, SIZE)
      .then(res => setPageData(res.data))
      .catch(() => alert('게시글을 불러오지 못했습니다.'))
      .finally(() => setLoading(false));
  }, [page]);

  const handleCreate = () => {
    if (!localStorage.getItem('accessToken')) {
      alert('로그인이 필요합니다.');
      navigate('/login');
      return;
    }

    navigate('/boards/new');
  };

  const boards = pageData?.content ?? [];
  const totalPages = pageData?.totalPages ?? 1;
  const totalCount = pageData?.totalCount ?? 0;

  const groupStart = Math.floor((page - 1) / PAGE_GROUP) * PAGE_GROUP + 1;
  const groupEnd = Math.min(groupStart + PAGE_GROUP - 1, totalPages);
  const pageNumbers = Array.from({ length: groupEnd - groupStart + 1 }, (_, i) => groupStart + i);

  return (
    <div className="container">
      <div className="page-header">
        <h1 className="page-title">자유게시판</h1>
        <button className="btn btn-primary" onClick={handleCreate}>
          글쓰기
        </button>
      </div>

      {loading ? (
        <div className="loading">로딩 중...</div>
      ) : (
        <>
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
                    <td className="center">{totalCount - (page - 1) * SIZE - index}</td>
                    <td className="title-cell">{board.title}</td>
                    <td className="center">{board.writer}</td>
                    <td className="center">{board.viewCount}</td>
                    <td className="center">{formatDate(board.createdAt)}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>

          {totalPages > 1 && (
            <div className="pagination">
              <button className="page-btn" onClick={() => setPage(1)} disabled={page === 1}>처음</button>
              <button className="page-btn" onClick={() => setPage(p => p - 1)} disabled={page === 1}>이전</button>
              {pageNumbers.map(p => (
                <button
                  key={p}
                  className={`page-btn${p === page ? ' active' : ''}`}
                  onClick={() => setPage(p)}
                >
                  {p}
                </button>
              ))}
              <button className="page-btn" onClick={() => setPage(p => p + 1)} disabled={page === totalPages}>다음</button>
              <button className="page-btn" onClick={() => setPage(totalPages)} disabled={page === totalPages}>끝</button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
