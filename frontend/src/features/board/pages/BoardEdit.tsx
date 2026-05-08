import { useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Editor } from '@toast-ui/react-editor';
import '@toast-ui/editor/dist/toastui-editor.css';
import { boardApi } from '../api/boardApi';

export default function BoardEdit() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const editorRef = useRef<Editor>(null);
  const pendingBlobs = useRef<Map<string, Blob>>(new Map());
  const [title, setTitle] = useState('');
  const [initialContent, setInitialContent] = useState('');
  const [errors, setErrors] = useState<{ title?: string; content?: string }>({});
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (!id) return;
    boardApi.getById(Number(id))
      .then(res => {
        setTitle(res.data.title);
        setInitialContent(res.data.content);
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

      await boardApi.update(Number(id), { title, content });
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
