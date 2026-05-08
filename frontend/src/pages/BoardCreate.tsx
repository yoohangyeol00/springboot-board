import { useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Editor } from '@toast-ui/react-editor';
import '@toast-ui/editor/dist/toastui-editor.css';
import { boardApi } from '../api/boardApi';

interface FormState {
  title: string;
  writer: string;
}

export default function BoardCreate() {
  const navigate = useNavigate();
  const editorRef = useRef<Editor>(null);
  const [form, setForm] = useState<FormState>({ title: '', writer: '' });
  const [errors, setErrors] = useState<Partial<FormState & { content: string }>>({});
  const [submitting, setSubmitting] = useState(false);

  const getContent = () => editorRef.current?.getInstance().getMarkdown() ?? '';

  const validate = (): boolean => {
    const newErrors: typeof errors = {};
    if (!form.title.trim()) newErrors.title = '제목을 입력해주세요.';
    if (!form.writer.trim()) newErrors.writer = '작성자를 입력해주세요.';
    if (!getContent().trim()) newErrors.content = '내용을 입력해주세요.';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleChange = (field: keyof FormState) => (
    e: React.ChangeEvent<HTMLInputElement>
  ) => {
    setForm(prev => ({ ...prev, [field]: e.target.value }));
    if (errors[field]) setErrors(prev => ({ ...prev, [field]: undefined }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;
    setSubmitting(true);
    try {
      await boardApi.create({ ...form, content: getContent() });
      navigate('/');
    } catch (err: any) {
      const message = err?.response?.data?.message || '작성에 실패했습니다.';
      alert(message);
    } finally {
      setSubmitting(false);
    }
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
            type="text"
            className={`form-input${errors.title ? ' error' : ''}`}
            value={form.title}
            onChange={handleChange('title')}
            placeholder="제목을 입력해주세요"
          />
          {errors.title && <span className="error-message">{errors.title}</span>}
        </div>

        <div className="form-group">
          <label className="form-label">
            작성자 <span className="required">*</span>
          </label>
          <input
            type="text"
            className={`form-input${errors.writer ? ' error' : ''}`}
            value={form.writer}
            onChange={handleChange('writer')}
            placeholder="작성자명을 입력해주세요"
          />
          {errors.writer && <span className="error-message">{errors.writer}</span>}
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
            />
          </div>
          {errors.content && <span className="error-message">{errors.content}</span>}
        </div>

        <div className="button-group">
          <button type="button" className="btn btn-secondary" onClick={() => navigate('/')}>
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
