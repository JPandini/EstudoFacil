import { createContext, useContext, useState, useCallback, type ReactNode } from 'react';
import type { Usuario, AuthResponse, RegisterRequest, LoginRequest } from '../api/types';
import { apiFetch, clearToken } from '../api/client';

interface AuthContextType {
  usuario: Usuario | null;
  isAuthenticated: boolean;
  login: (data: LoginRequest) => Promise<void>;
  registrar: (data: RegisterRequest) => Promise<void>;
  logout: () => void;
  carregarPerfil: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<Usuario | null>(() => {
    const stored = localStorage.getItem('usuario');
    return stored ? JSON.parse(stored) : null;
  });

  const isAuthenticated = !!localStorage.getItem('token');

  const login = useCallback(async (data: LoginRequest) => {
    const response = await apiFetch<AuthResponse>('/auth/login', {
      method: 'POST',
      body: JSON.stringify(data),
    }, false);

    localStorage.setItem('token', response.token);
    localStorage.setItem('usuario', JSON.stringify(response.usuario));
    setUsuario(response.usuario);
  }, []);

  const registrar = useCallback(async (data: RegisterRequest) => {
    await apiFetch<Usuario>('/auth/registrar', {
      method: 'POST',
      body: JSON.stringify(data),
    }, false);
  }, []);

  const logout = useCallback(() => {
    clearToken();
    setUsuario(null);
  }, []);

  const carregarPerfil = useCallback(async () => {
    const perfil = await apiFetch<Usuario>('/auth/perfil');
    localStorage.setItem('usuario', JSON.stringify(perfil));
    setUsuario(perfil);
  }, []);

  return (
    <AuthContext.Provider value={{ usuario, isAuthenticated, login, registrar, logout, carregarPerfil }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth deve ser usado dentro de AuthProvider');
  return ctx;
}
