export interface Board {
  id: number;
  title: string;
  content: string;
  writer: string;
  viewCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface BoardCreateRequest {
  title: string;
  content: string;
  writer: string;
}

export interface BoardUpdateRequest {
  title: string;
  content: string;
}
