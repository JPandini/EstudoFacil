import type { ApiError } from './types';

export const API_BASE = 'http://localhost:8080';

export function buildApiUrl(path: string): string {
  return `${API_BASE}${path}`;
}

function getToken(): string | null {
  return localStorage.getItem('token');
}

export function clearToken(): void {
  localStorage.removeItem('token');
  localStorage.removeItem('usuario');
}

export async function apiFetch<T>(
  path: string,
  options: RequestInit = {},
  auth = true
): Promise<T> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string>),
  };

  if (auth) {
    const token = getToken();
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
  }

  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers,
  });

  if (response.status === 401 && auth) {
    clearToken();
    window.location.href = '/login';
    throw new Error('Não autorizado');
  }

  if (response.status === 204) {
    return undefined as T;
  }

  const data = await response.json().catch(() => null);

  if (!response.ok) {
    const error = data as ApiError;
    if (error?.errors && Object.keys(error.errors).length > 0) {
      const details = Object.values(error.errors).join(' · ');
      throw new Error(details);
    }
    throw new Error(error?.message || `Erro ${response.status}`);
  }

  return data as T;
}

export function buildQuery(params: Record<string, string | number | undefined>): string {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== '') {
      search.set(key, String(value));
    }
  });
  const qs = search.toString();
  return qs ? `?${qs}` : '';
}
