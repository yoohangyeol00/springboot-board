export interface Comment {
  id: number;
  boardId: number;
  memberId: number;
  parentId: number | null;
  content: string;
  writer: string;
  deleted: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CommentCreateRequest {
  content: string;
  parentId?: number | null;
}

export interface CommentUpdateRequest {
  content: string;
}
