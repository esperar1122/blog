export interface Tag {
  id: number;
  name: string;
  color: string;
  articleCount: number;
  createTime: string;
  updateTime?: string;
}

export interface ArticleTag {
  articleId: number;
  tagId: number;
  createTime: string;
}

export interface CreateTagRequest {
  name: string;
  color?: string;
}

export interface UpdateTagRequest {
  name: string;
  color?: string;
}