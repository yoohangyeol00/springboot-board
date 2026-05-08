import { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Editor } from '@toast-ui/react-editor';
import '@toast-ui/editor/dist/toastui-editor.css';
import { boardApi } from '../api/boardApi';

interface FormState {
  title: string;
}

export default function BoardCreate() {
  const navigate = useNavigate();
  const titleInputRef = useRef<HTMLInputElement>(null);
  const editorRef = useRef<Editor>(null);
  const pendingBlobs = useRef<Map<string, Blob>>(new Map());
  const [form, setForm] = useState<FormState>({ title: '' });
  const [errors, setErrors] = useState<Partial<FormState & { content: string }>>({});
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    const timer = window.setTimeout(() => {
      titleInputRef.current?.focus();
    }, 0);

    return () => window.clearTimeout(timer);
  }, []);

  const getContent = () => editorRef.current?.getInstance().getMarkdown() ?? '';

  const validate = (): boolean => {
    const newErrors: typeof errors = {};
    if (!form.title.trim()) newErrors.title = '제목을 입력해주세요.';
    if (!getContent().trim()) newErrors.content = '내용을 입력해주세요.';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm(prev => ({ ...prev, title: e.target.value }));
    if (errors.title) setErrors(prev => ({ ...prev, title: undefined }));
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

      await boardApi.create({ title: form.title, content });
      navigate('/');
    } catch (err: any) {
      const status = err?.response?.status;
      const message = err?.response?.data?.message || '게시글 작성에 실패했습니다.';

      if (status === 401 || status === 403) {
        alert('로그인이 필요합니다.');
        navigate('/login');
        return;
      }

      alert(message);
    } finally {
      setSubmitting(false);
    }
  };

  const handleCancel = () => {
    pendingBlobs.current.forEach((_, url) => URL.revokeObjectURL(url));
    pendingBlobs.current.clear();
    navigate('/');
  };

  return (
    <div className="container">
      <div className="page-header">
        <h1 className="page-title">글쓰기</h1>
      </div>

      <form onSubmit={handleSubmit} className="board-form">
        <div className="form-group">
          <label className="form-label">
            제목 <span className="required">*</span>
          </label>
          <input
            ref={titleInputRef}
            type="text"
            className={`form-input${errors.title ? ' error' : ''}`}
            value={form.title}
            onChange={handleChange}
            placeholder="제목을 입력해주세요"
            autoFocus
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
