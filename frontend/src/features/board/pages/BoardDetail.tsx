import { FormEvent, useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Viewer } from '@toast-ui/react-editor';
import '@toast-ui/editor/dist/toastui-editor-viewer.css';
import { boardApi } from '../api/boardApi';
import { Board } from '../types/board';
import { formatDateTime } from '../../../shared/utils/dateUtils';
import { memberApi } from '../../member/api/memberApi';
import { MemberMe } from '../../member/types/member';
import { commentApi } from '../../comment/api/commentApi';
import { Comment } from '../../comment/types/comment';

export default function BoardDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const boardId = Number(id);
  const [board, setBoard] = useState<Board | null>(null);
  const [me, setMe] = useState<MemberMe | null>(null);
  const [comments, setComments] = useState<Comment[]>([]);
  const [commentContent, setCommentContent] = useState('');
  const [replyContent, setReplyContent] = useState('');
  const [editingContent, setEditingContent] = useState('');
  const [replyTargetId, setReplyTargetId] = useState<number | null>(null);
  const [editingCommentId, setEditingCommentId] = useState<number | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [loading, setLoading] = useState(true);

  const rootComments = useMemo(
    () => comments.filter(comment => comment.parentId == null),
    [comments],
  );

  const repliesByParentId = useMemo(() => {
    return comments.reduce<Record<number, Comment[]>>((acc, comment) => {
      if (comment.parentId != null) {
        acc[comment.parentId] = [...(acc[comment.parentId] ?? []), comment];
      }

      return acc;
    }, {});
  }, [comments]);

  const loadComments = async () => {
    if (!boardId) return;

    const res = await commentApi.getByBoardId(boardId);
    setComments(res.data);
  };

  useEffect(() => {
    if (!id) return;

    boardApi.getById(boardId)
      .then(res => setBoard(res.data))
      .catch(() => {
        alert('게시글을 찾을 수 없습니다.');
        navigate('/');
      })
      .finally(() => setLoading(false));

    loadComments().catch(() => {
      alert('댓글을 불러오지 못했습니다.');
    });

    if (localStorage.getItem('accessToken')) {
      memberApi.me()
        .then(res => setMe(res.data))
        .catch(() => {
          localStorage.removeItem('accessToken');
          setMe(null);
        });
    }
  }, [id, boardId, navigate]);

  const handleDelete = async () => {
    if (!window.confirm('정말 삭제하시겠습니까?')) return;

    try {
      await boardApi.delete(boardId);
      navigate('/');
    } catch (err: any) {
      alert(err?.response?.data?.message || '삭제에 실패했습니다.');
    }
  };

  const handleCreateComment = async (event: FormEvent) => {
    event.preventDefault();

    if (!me) {
      alert('로그인이 필요합니다.');
      navigate('/login');
      return;
    }

    if (!commentContent.trim()) {
      alert('댓글 내용을 입력해주세요.');
      return;
    }

    try {
      setSubmitting(true);
      await commentApi.create(boardId, { content: commentContent.trim() });
      setCommentContent('');
      await loadComments();
    } catch (err: any) {
      alert(err?.response?.data?.message || '댓글 등록에 실패했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleCreateReply = async (parentId: number) => {
    if (!me) {
      alert('로그인이 필요합니다.');
      navigate('/login');
      return;
    }

    if (!replyContent.trim()) {
      alert('답글 내용을 입력해주세요.');
      return;
    }

    try {
      setSubmitting(true);
      await commentApi.create(boardId, {
        content: replyContent.trim(),
        parentId,
      });
      setReplyTargetId(null);
      setReplyContent('');
      await loadComments();
    } catch (err: any) {
      alert(err?.response?.data?.message || '답글 등록에 실패했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  const startEdit = (comment: Comment) => {
    setReplyTargetId(null);
    setReplyContent('');
    setEditingCommentId(comment.id);
    setEditingContent(comment.content);
  };

  const cancelEdit = () => {
    setEditingCommentId(null);
    setEditingContent('');
  };

  const handleUpdateComment = async (commentId: number) => {
    if (!editingContent.trim()) {
      alert('댓글 내용을 입력해주세요.');
      return;
    }

    try {
      setSubmitting(true);
      await commentApi.update(commentId, { content: editingContent.trim() });
      cancelEdit();
      await loadComments();
    } catch (err: any) {
      alert(err?.response?.data?.message || '댓글 수정에 실패했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeleteComment = async (commentId: number) => {
    if (!window.confirm('댓글을 삭제하시겠습니까?')) return;

    try {
      await commentApi.delete(commentId);
      await loadComments();
    } catch (err: any) {
      alert(err?.response?.data?.message || '댓글 삭제에 실패했습니다.');
    }
  };

  const startReply = (commentId: number) => {
    setEditingCommentId(null);
    setEditingContent('');
    setReplyTargetId(commentId);
    setReplyContent('');
  };

  const cancelReply = () => {
    setReplyTargetId(null);
    setReplyContent('');
  };

  const renderComment = (comment: Comment, isReply = false) => {
    const isCommentOwner = me != null && comment.memberId === me.id;
    const isEditing = editingCommentId === comment.id;
    const replies = repliesByParentId[comment.id] ?? [];

    return (
      <div key={comment.id} className={`comment-item${isReply ? ' comment-item-reply' : ''}`}>
        <div className="comment-main">
          <div className="comment-writer">{comment.writer}</div>

          {isEditing ? (
            <div className="comment-edit">
              <textarea
                className="comment-textarea"
                value={editingContent}
                onChange={event => setEditingContent(event.target.value)}
                rows={3}
              />
              <div className="comment-actions">
                <button
                  type="button"
                  className="btn btn-primary btn-sm"
                  disabled={submitting}
                  onClick={() => handleUpdateComment(comment.id)}
                >
                  저장
                </button>
                <button type="button" className="btn btn-secondary btn-sm" onClick={cancelEdit}>
                  취소
                </button>
              </div>
            </div>
          ) : (
            <>
              <p className={`comment-content${comment.deleted ? ' comment-content-deleted' : ''}`}>
                {comment.content}
              </p>
              <div className="comment-date">{formatDateTime(comment.createdAt)}</div>
            </>
          )}

          {!comment.deleted && !isEditing && (
            <div className="comment-actions">
              {!isReply && (
                <button type="button" className="comment-action" onClick={() => startReply(comment.id)}>
                  답글쓰기
                </button>
              )}
              {isCommentOwner && (
                <>
                  <button type="button" className="comment-action" onClick={() => startEdit(comment)}>
                    수정
                  </button>
                  <button
                    type="button"
                    className="comment-action comment-action-danger"
                    onClick={() => handleDeleteComment(comment.id)}
                  >
                    삭제
                  </button>
                </>
              )}
            </div>
          )}
        </div>

        {replyTargetId === comment.id && (
          <div className="reply-form">
            <textarea
              className="comment-textarea"
              value={replyContent}
              onChange={event => setReplyContent(event.target.value)}
              rows={3}
              placeholder="답글을 입력하세요."
            />
            <div className="comment-actions">
              <button
                type="button"
                className="btn btn-primary btn-sm"
                disabled={submitting}
                onClick={() => handleCreateReply(comment.id)}
              >
                등록
              </button>
              <button type="button" className="btn btn-secondary btn-sm" onClick={cancelReply}>
                취소
              </button>
            </div>
          </div>
        )}

        {replies.map(reply => renderComment(reply, true))}
      </div>
    );
  };

  if (loading) {
    return (
      <div className="container">
        <div className="loading">로딩 중...</div>
      </div>
    );
  }

  if (!board) return null;

  const isOwner = me != null && board.memberId === me.id;

  return (
    <div className="container">
      <div className="detail-header">
        <h1 className="detail-title">{board.title}</h1>
        <div className="detail-meta">
          <span>작성자 <strong>{board.writer}</strong></span>
          <span>작성일 {formatDateTime(board.createdAt)}</span>
          <span>조회수 {board.viewCount}</span>
        </div>
      </div>

      <div className="detail-content">
        <Viewer initialValue={board.content} />
      </div>

      <section className="comment-section">
        <div className="comment-section-header">
          <h2>댓글</h2>
        </div>

        <form className="comment-form" onSubmit={handleCreateComment}>
          <textarea
            className="comment-textarea"
            value={commentContent}
            onChange={event => setCommentContent(event.target.value)}
            rows={4}
            placeholder={me ? '댓글을 입력하세요.' : '로그인 후 댓글을 작성할 수 있습니다.'}
            disabled={!me || submitting}
          />
          <div className="comment-form-actions">
            <button type="submit" className="btn btn-primary" disabled={!me || submitting}>
              댓글 등록
            </button>
          </div>
        </form>

        <div className="comment-list">
          {rootComments.length === 0 ? (
            <div className="comment-empty">아직 댓글이 없습니다.</div>
          ) : (
            rootComments.map(comment => renderComment(comment))
          )}
        </div>
      </section>

      <div className="button-group">
        <button className="btn btn-secondary" onClick={() => navigate('/')}>
          목록
        </button>
        {isOwner && (
          <div className="button-group-right">
            <button className="btn btn-primary" onClick={() => navigate(`/boards/${id}/edit`)}>
              수정
            </button>
            <button className="btn btn-danger" onClick={handleDelete}>
              삭제
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
