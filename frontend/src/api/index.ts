import request, { ApiResult, PageResult } from '../utils/request';
import { MenuItem, useAuthStore } from '../store/authStore';

export interface LoginResponse {
  token: string;
  userId: number;
  username: string;
  nickname: string;
  tenantId: number;
  menus: MenuItem[];
  permissions: string[];
}

export const authApi = {
  login: (username: string, password: string) =>
    request.post<any, ApiResult<LoginResponse>>('/auth/login', { username, password }),
};

export const userApi = {
  page: (params: { pageNum: number; pageSize: number; keyword?: string }) =>
    request.get<any, ApiResult<PageResult<any>>>('/users', { params }),
  save: (data: any) => request.post<any, ApiResult<void>>('/users', data),
  delete: (id: number) => request.delete<any, ApiResult<void>>(`/users/${id}`),
  listAll: () => request.get<any, ApiResult<any[]>>('/users/all'),
};

export const roleApi = {
  page: (params: { pageNum: number; pageSize: number }) =>
    request.get<any, ApiResult<PageResult<any>>>('/roles', { params }),
  listAll: () => request.get<any, ApiResult<any[]>>('/roles/all'),
  save: (data: any) => request.post<any, ApiResult<void>>('/roles', data),
  delete: (id: number) => request.delete<any, ApiResult<void>>(`/roles/${id}`),
};

export const deptApi = {
  tree: () => request.get<any, ApiResult<any[]>>('/depts/tree'),
  save: (data: any) => request.post<any, ApiResult<void>>('/depts', data),
  delete: (id: number) => request.delete<any, ApiResult<void>>(`/depts/${id}`),
};

export const menuApi = {
  tree: () => request.get<any, ApiResult<any[]>>('/menus/tree'),
  save: (data: any) => request.post<any, ApiResult<void>>('/menus', data),
  delete: (id: number) => request.delete<any, ApiResult<void>>(`/menus/${id}`),
};

export const apiPermApi = {
  page: (params: { pageNum: number; pageSize: number }) =>
    request.get<any, ApiResult<PageResult<any>>>('/apis', { params }),
  save: (data: any) => request.post<any, ApiResult<void>>('/apis', data),
  delete: (id: number) => request.delete<any, ApiResult<void>>(`/apis/${id}`),
};

export const tenantApi = {
  page: (params: { pageNum: number; pageSize: number }) =>
    request.get<any, ApiResult<PageResult<any>>>('/tenants', { params }),
  save: (data: any) => request.post<any, ApiResult<void>>('/tenants', data),
  delete: (id: number) => request.delete<any, ApiResult<void>>(`/tenants/${id}`),
};

export const dashboardApi = {
  stats: () => request.get<any, ApiResult<Record<string, number>>>('/dashboard/stats'),
  risks: (params: { pageNum: number; pageSize: number; status?: number }) =>
    request.get<any, ApiResult<PageResult<any>>>('/risks', { params }),
  handleRisk: (id: number, status: number) =>
    request.put<any, ApiResult<void>>(`/risks/${id}/handle`, null, { params: { status } }),
  triggerCheck: () => request.post<any, ApiResult<void>>('/risks/check'),
  logs: (params: { pageNum: number; pageSize: number }) =>
    request.get<any, ApiResult<PageResult<any>>>('/logs', { params }),
  templates: () => request.get<any, ApiResult<any[]>>('/templates'),
};

export const aiApi = {
  listSessions: () => request.get<any, ApiResult<any[]>>('/ai/sessions'),
  getSession: (sessionId: string) => request.get<any, ApiResult<any[]>>(`/ai/sessions/${sessionId}`),
  deleteSession: (sessionId: string) => request.delete<any, ApiResult<void>>(`/ai/sessions/${sessionId}`),
  confirmAction: (id: number) => request.post<any, ApiResult<string>>(`/ai/actions/${id}/confirm`),
  rejectAction: (id: number) => request.post<any, ApiResult<void>>(`/ai/actions/${id}/reject`),
};


export async function streamAiChat(
  message: string,
  sessionId: string | null,
  onChunk: (text: string) => void,
  onDone: (sessionId: string) => void,
  onToolStart?: (tool: string) => void,
  onConfirmRequired?: (data: { actionId: number; tool: string; preview: string }) => void,
) {
  const authToken = useAuthStore.getState().token || '';

  const res = await fetch('/api/ai/chat/stream', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${authToken}`,
    },
    body: JSON.stringify({ message, sessionId }),
  });

  if (!res.ok) throw new Error('AI对话请求失败');

  const reader = res.body?.getReader();
  const decoder = new TextDecoder();
  if (!reader) return;

  let buffer = '';
  let newSessionId = sessionId || '';
  while (true) {
    const { done, value } = await reader.read();
    if (done) break;
    buffer += decoder.decode(value, { stream: true });
    const events = buffer.split('\n\n');
    buffer = events.pop() || '';
    for (const event of events) {
      const lines = event.split('\n');
      let eventName = 'message';
      let data = '';
      for (const line of lines) {
        if (line.startsWith('event:')) eventName = line.slice(6).trim();
        if (line.startsWith('data:')) data = line.slice(5).trim();
      }
      if (eventName === 'message' && data) onChunk(data);
      if (eventName === 'tool_start' && data && onToolStart) onToolStart(data);
      if (eventName === 'confirm_required' && data && onConfirmRequired) {
        try { onConfirmRequired(JSON.parse(data)); } catch { /* ignore */ }
      }
      if (eventName === 'done' && data) newSessionId = data;
    }
  }
  onDone(newSessionId);
}
