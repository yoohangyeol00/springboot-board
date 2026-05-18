export interface Board {
  id: number;
  memberId: number | null;
  title: string;
  content: string;
  writer: string;
  viewCount: number;
  commentCount: number;
  createdAt: string;
  updatedAt: string;
  attachments: BoardAttachment[];
}

export interface BoardAttachment {
  id: number;
  boardId: number;
  originalName: string;
  fileUrl: string;
  fileSize: number;
  contentType: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalCount: number;
  totalPages: number;
}

export interface BoardCreateRequest {
  title: string;
  content: string;
}

export interface BoardUpdateRequest {
  title: string;
  content: string;
}
