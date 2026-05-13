import { useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Editor } from '@toast-ui/react-editor';
import '@toast-ui/editor/dist/toastui-editor.css';
import { boardApi } from '../api/boardApi';
import { BoardAttachment } from '../types/board';

export default function BoardEdit() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const editorRef = useRef<Editor>(null);
  const pendingBlobs = useRef<Map<string, Blob>>(new Map());
  const [title, setTitle] = useState('');
  const [initialContent, setInitialContent] = useState('');
  const [attachments, setAttachments] = useState<BoardAttachment[]>([]);
  const [deletedAttachmentIds, setDeletedAttachmentIds] = useState<number[]>([]);
  const [newAttachments, setNewAttachments] = useState<File[]>([]);
  const [errors, setErrors] = useState<{ title?: string; content?: string }>({});
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [attachmentSubmitting, setAttachmentSubmitting] = useState(false);

  useEffect(() => {
    if (!id) return;
    boardApi.getById(Number(id))
      .then(res => {
        setTitle(res.data.title);
        setInitialContent(res.data.content);
        setAttachments(res.data.attachments ?? []);
      })
      .catch(() => {
        alert('게시글을 찾을 수 없습니다.');
        navigate('/');
      })
      .finally(() => setLoading(false));
  }, [id, navigate]);

  const getContent = () => editorRef.current?.getInstance().getMarkdown() ?? '';

  const validate = (): boolean => {
    const newErrors: typeof errors = {};
    if (!title.trim()) newErrors.title = '제목을 입력해주세요.';
    if (!getContent().trim()) newErrors.content = '내용을 입력해주세요.';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;
    setSubmitting(true);
    try {
      let content = getContent();

      for (const [tempUrl, blob] of pendingBlobs.current.entries()) {
        if (content.includes(tempUrl)) {
          const res = await boardApi.uploadImage(blob);
          content = content.split(tempUrl).join(res.data.url);
        }
        URL.revokeObjectURL(tempUrl);
      }
      pendingBlobs.current.clear();

      const boardId = Number(id);

      await boardApi.update(boardId, { title, content });

      for (const attachmentId of deletedAttachmentIds) {
        await boardApi.deleteAttachment(boardId, attachmentId);
      }

      if (newAttachments.length > 0) {
        await boardApi.addAttachments(boardId, newAttachments);
      }

      navigate(`/boards/${id}`);
    } catch (err: any) {
      const message = err?.response?.data?.message || '수정에 실패했습니다.';
      alert(message);
    } finally {
      setSubmitting(false);
    }
  };

  const handleCancel = () => {
    pendingBlobs.current.forEach((_, url) => URL.revokeObjectURL(url));
    pendingBlobs.current.clear();
    navigate(`/boards/${id}`);
  };

  const handleNewAttachmentChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const selectedFiles = Array.from(e.target.files ?? []);
    setNewAttachments(prev => [...prev, ...selectedFiles]);
    e.target.value = '';
  };

  const handleRemoveNewAttachment = (index: number) => {
    setNewAttachments(prev => prev.filter((_, fileIndex) => fileIndex !== index));
  };

  const handleDeleteAttachment = async (attachmentId: number) => {
    setDeletedAttachmentIds(prev =>
      prev.includes(attachmentId) ? prev : [...prev, attachmentId],
    );
    setAttachments(prev => prev.filter(attachment => attachment.id !== attachmentId));
  };

  const handleReplaceAttachment = async (
    attachmentId: number,
    e: React.ChangeEvent<HTMLInputElement>,
  ) => {
    if (!id) return;

    const file = e.target.files?.[0];
    e.target.value = '';

    if (!file) return;

    try {
      setAttachmentSubmitting(true);
      const res = await boardApi.replaceAttachment(Number(id), attachmentId, file);
      setAttachments(prev =>
        prev.map(attachment => (attachment.id === attachmentId ? res.data : attachment)),
      );
    } catch (err: any) {
      alert(err?.response?.data?.message || 'Failed to replace attachment.');
    } finally {
      setAttachmentSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="container">
        <div className="loading">로딩 중...</div>
      </div>
    );
  }

  return (
    <div className="container">
      <div className="page-header">
        <h1 className="page-title">글수정</h1>
      </div>

      <form onSubmit={handleSubmit} className="board-form">
        <div className="form-group">
          <label className="form-label">
            제목 <span className="required">*</span>
          </label>
          <input
            type="text"
            className={`form-input${errors.title ? ' error' : ''}`}
            value={title}
            onChange={e => {
              setTitle(e.target.value);
              if (errors.title) setErrors(prev => ({ ...prev, title: undefined }));
            }}
            placeholder="제목을 입력해주세요"
          />
          {errors.title && <span className="error-message">{errors.title}</span>}
        </div>

        <div className="form-group">
          <label className="form-label">
            내용 <span className="required">*</span>
          </label>
          <div className={errors.content ? 'editor-wrap editor-wrap--error' : 'editor-wrap'}>
            <Editor
              ref={editorRef}
              initialValue={initialContent}
              previewStyle="vertical"
              height="400px"
              initialEditType="wysiwyg"
              hideModeSwitch
              useCommandShortcut
              hooks={{
                addImageBlobHook: (blob: Blob | File, callback: (url: string, alt: string) => void) => {
                  const tempUrl = URL.createObjectURL(blob);
                  pendingBlobs.current.set(tempUrl, blob);
                  callback(tempUrl, '이미지');
                },
              }}
            />
          </div>
          {errors.content && <span className="error-message">{errors.content}</span>}
        </div>

        <div className="form-group">
          <label className="form-label">Attachments</label>

          {attachments.length > 0 ? (
            <ul className="attachment-list attachment-list-edit">
              {attachments.map(attachment => (
                <li key={attachment.id} className="attachment-item attachment-item-edit">
                  <a
                    className="attachment-link"
                    href={boardApi.getAttachmentDownloadUrl(Number(id), attachment.id)}
                  >
                    {attachment.originalName}
                  </a>
                  <div className="attachment-actions">
                    <label className="comment-action attachment-replace">
                      Replace
                      <input
                        type="file"
                        disabled={attachmentSubmitting}
                        onChange={event => handleReplaceAttachment(attachment.id, event)}
                      />
                    </label>
                    <button
                      type="button"
                      className="comment-action comment-action-danger"
                      disabled={attachmentSubmitting}
                      onClick={() => handleDeleteAttachment(attachment.id)}
                    >
                      Delete
                    </button>
                  </div>
                </li>
              ))}
            </ul>
          ) : (
            <div className="attachment-empty">No attachments.</div>
          )}

          <input type="file" className="form-input" multiple onChange={handleNewAttachmentChange} />

          {newAttachments.length > 0 && (
            <ul className="attachment-list attachment-list-edit">
              {newAttachments.map((file, index) => (
                <li key={`${file.name}-${index}`} className="attachment-item">
                  <span className="attachment-name">{file.name}</span>
                  <button
                    type="button"
                    className="comment-action comment-action-danger"
                    onClick={() => handleRemoveNewAttachment(index)}
                  >
                    Remove
                  </button>
                </li>
              ))}
            </ul>
          )}

        </div>

        <div className="button-group">
          <button type="button" className="btn btn-secondary" onClick={handleCancel}>
            취소
          </button>
          <div className="button-group-right">
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? '저장 중...' : '저장'}
            </button>
          </div>
        </div>
      </form>
    </div>
  );
}
