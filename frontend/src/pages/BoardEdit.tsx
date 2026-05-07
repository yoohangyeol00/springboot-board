import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { boardApi } from '../api/boardApi';

interface FormState {
  title: string;
  content: string;
}

export default function BoardEdit() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [form, setForm] = useState<FormState>({ title: '', content: '' });
  const [errors, setErrors] = useState<Partial<FormState>>({});
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (!id) return;
    boardApi.getById(Number(id))
      .then(res => setForm({ title: res.data.title, content: res.data.content }))
      .catch(() => {
        alert('게시글을 찾을 수 없습니다.');
        navigate('/');
      })
      .finally(() => setLoading(false));
  }, [id, navigate]);

  const validate = (): boolean => {
    const newErrors: Partial<FormState> = {};
    if (!form.title.trim()) newErrors.title = '제목을 입력해주세요.';
    if (!form.content.trim()) newErrors.content = '내용을 입력해주세요.';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleChange = (field: keyof FormState) => (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    setForm(prev => ({ ...prev, [field]: e.target.value }));
    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: undefined }));
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;
    setSubmitting(true);
    try {
      await boardApi.update(Number(id), form);
      navigate(`/boards/${id}`);
    } catch (err: any) {
      const message = err?.response?.data?.message || '수정에 실패했습니다.';
      alert(message);
    } finally {
      setSubmitting(false);
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
            value={form.title}
            onChange={handleChange('title')}
            placeholder="제목을 입력해주세요"
          />
          {errors.title && <span className="error-message">{errors.title}</span>}
        </div>

        <div className="form-group">
          <label className="form-label">
            내용 <span className="required">*</span>
          </label>
          <textarea
            className={`form-textarea${errors.content ? ' error' : ''}`}
            value={form.content}
            onChange={handleChange('content')}
            placeholder="내용을 입력해주세요"
          />
          {errors.content && <span className="error-message">{errors.content}</span>}
        </div>

        <div className="button-group">
          <button
            type="button"
            className="btn btn-secondary"
            onClick={() => navigate(`/boards/${id}`)}
          >
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
