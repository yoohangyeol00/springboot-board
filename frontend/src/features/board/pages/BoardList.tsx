import { FormEvent, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { boardApi, BoardSearchType } from '../api/boardApi';
import { Board, PageResponse } from '../types/board';
import { formatDate } from '../../../shared/utils/dateUtils';

const SIZE = 10;
const PAGE_GROUP = 10;

export default function BoardList() {
  const [pageData, setPageData] = useState<PageResponse<Board> | null>(null);
  const [popularBoards, setPopularBoards] = useState<Board[]>([]);
  const [page, setPage] = useState(1);
  const [searchType, setSearchType] = useState<BoardSearchType>('all');
  const [keyword, setKeyword] = useState('');
  const [appliedSearchType, setAppliedSearchType] = useState<BoardSearchType>('all');
  const [appliedKeyword, setAppliedKeyword] = useState('');
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    boardApi.getPopular(5)
      .then(res => setPopularBoards(res.data))
      .catch(() => setPopularBoards([]));
  }, []);

  useEffect(() => {
    setLoading(true);
    boardApi.getAll(page, SIZE, appliedSearchType, appliedKeyword)
      .then(res => setPageData(res.data))
      .catch(() => alert('게시글을 불러오지 못했습니다.'))
      .finally(() => setLoading(false));
  }, [page, appliedSearchType, appliedKeyword]);

  const handleCreate = () => {
    if (!localStorage.getItem('accessToken')) {
      alert('로그인이 필요합니다.');
      navigate('/login');
      return;
    }

    navigate('/boards/new');
  };

  const handleSearch = (event: FormEvent) => {
    event.preventDefault();
    setPage(1);
    setAppliedSearchType(searchType);
    setAppliedKeyword(keyword.trim());
  };

  const handleResetSearch = () => {
    setSearchType('all');
    setKeyword('');
    setAppliedSearchType('all');
    setAppliedKeyword('');
    setPage(1);
  };

  const boards = pageData?.content ?? [];
  const totalPages = pageData?.totalPages ?? 1;
  const totalCount = pageData?.totalCount ?? 0;

  const groupStart = Math.floor((page - 1) / PAGE_GROUP) * PAGE_GROUP + 1;
  const groupEnd = Math.min(groupStart + PAGE_GROUP - 1, totalPages);
  const pageNumbers = Array.from({ length: groupEnd - groupStart + 1 }, (_, i) => groupStart + i);
  const isSearching = appliedKeyword.length > 0;

  const renderBoardRows = (rows: Board[], emptyMessage: string, showRank = false) => (
    <tbody>
      {rows.length === 0 ? (
        <tr>
          <td colSpan={6} className="empty-message">
            {emptyMessage}
          </td>
        </tr>
      ) : (
        rows.map((board, index) => (
          <tr key={board.id} onClick={() => navigate(`/boards/${board.id}`)}>
            <td className="center">{showRank ? index + 1 : totalCount - (page - 1) * SIZE - index}</td>
            <td className="title-cell">{board.title}</td>
            <td className="center">{board.writer}</td>
            <td className="center">{board.commentCount}</td>
            <td className="center">{board.viewCount}</td>
            <td className="center">{formatDate(board.createdAt)}</td>
          </tr>
        ))
      )}
    </tbody>
  );

  return (
    <div className="board-list">
      <section className="container popular-section">
        <div className="section-header">
          <h2 className="section-title">인기글</h2>
          <span className="section-subtitle">조회수 높은 글 5개</span>
        </div>
        <table className="board-table popular-table">
          <thead>
            <tr>
              <th className="col-no">순위</th>
              <th className="col-title">제목</th>
              <th className="col-writer">작성자</th>
              <th className="col-comments">댓글</th>
              <th className="col-views">조회수</th>
              <th className="col-date">작성일</th>
            </tr>
          </thead>
          {renderBoardRows(popularBoards, '아직 인기글이 없습니다.', true)}
        </table>
      </section>

      <section className="container">
        <div className="page-header">
          <h1 className="page-title">자유게시판</h1>
          <button className="btn btn-primary" onClick={handleCreate}>
            글쓰기
          </button>
        </div>

        <form className="board-search" onSubmit={handleSearch}>
          <select
            className="search-select"
            value={searchType}
            onChange={event => setSearchType(event.target.value as BoardSearchType)}
          >
            <option value="all">전체</option>
            <option value="title">제목</option>
            <option value="content">내용</option>
            <option value="writer">작성자</option>
          </select>
          <input
            className="search-input"
            value={keyword}
            onChange={event => setKeyword(event.target.value)}
            placeholder="검색어를 입력하세요."
          />
          <button className="btn btn-primary search-button" type="submit">
            검색
          </button>
          {isSearching && (
            <button className="btn btn-secondary search-button" type="button" onClick={handleResetSearch}>
              초기화
            </button>
          )}
        </form>

        {isSearching && (
          <div className="search-summary">
            "{appliedKeyword}" 검색 결과 {totalCount}건
          </div>
        )}

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
                  <th className="col-comments">댓글</th>
                  <th className="col-views">조회수</th>
                  <th className="col-date">작성일</th>
                </tr>
              </thead>
              {renderBoardRows(boards, isSearching ? '검색 결과가 없습니다.' : '등록된 게시글이 없습니다.')}
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
      </section>
    </div>
  );
}
