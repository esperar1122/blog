import request from '@/utils/request';
import type { Tag, CreateTagRequest, UpdateTagRequest } from '@blog/shared/types';

// 获取所有标签
export function getAllTags() {
  return request<Tag[]>({
    url: '/api/tags',
    method: 'get',
  });
}

// 获取热门标签
export function getPopularTags(limit = 10) {
  return request<Tag[]>({
    url: '/api/tags/popular',
    method: 'get',
    params: { limit },
  });
}

// 根据ID获取标签
export function getTagById(id: number) {
  return request<Tag>({
    url: `/api/tags/${id}`,
    method: 'get',
  });
}

// 创建标签（需要管理员权限）
export function createTag(data: CreateTagRequest) {
  return request<Tag>({
    url: '/api/tags',
    method: 'post',
    data,
  });
}

// 更新标签（需要管理员权限）
export function updateTag(id: number, data: UpdateTagRequest) {
  return request<Tag>({
    url: `/api/tags/${id}`,
    method: 'put',
    data,
  });
}

// 删除标签（需要管理员权限）
export function deleteTag(id: number) {
  return request<string>({
    url: `/api/tags/${id}`,
    method: 'delete',
  });
}

// 检查标签名称是否存在
export function checkTagNameExists(name: string, excludeId?: number) {
  return request<boolean>({
    url: '/api/tags/check/name',
    method: 'get',
    params: { name, excludeId },
  });
}

// 搜索标签
export function searchTags(name: string, limit = 20) {
  return request<Tag[]>({
    url: '/api/tags/search',
    method: 'get',
    params: { name, limit },
  });
}

// 获取标签下的文章列表
export function getArticlesByTag(id: number, page = 1, size = 10) {
  return request({
    url: `/api/tags/${id}/articles`,
    method: 'get',
    params: { page, size },
  });
}