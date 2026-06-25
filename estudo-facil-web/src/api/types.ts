export interface Usuario {
  id: number;
  nome: string;
  email: string;
  criadoEm: string;
}

export interface AuthResponse {
  token: string;
  tipo: string;
  usuario: Usuario;
}

export interface Materia {
  id: number;
  nome: string;
  professor: string;
  semestre: string;
  descricao: string;
  usuarioId: number;
  criadoEm: string;
  atualizadoEm: string;
}

export interface Tarefa {
  id: number;
  titulo: string;
  descricao: string;
  dataEntrega: string;
  status: TaskStatus;
  materiaId: number;
  nomeMateria: string;
  criadoEm: string;
  atualizadoEm: string;
}

export interface Nota {
  id: number;
  valor: number;
  peso: number;
  observacao: string;
  materiaId: number;
  nomeMateria: string;
  criadoEm: string;
  atualizadoEm: string;
}

export interface Dashboard {
  totalMaterias: number;
  tarefasPendentes: number;
  tarefasConcluidas: number;
  tarefasEmAndamento: number;
  mediaGeral: number | null;
}

export type TaskStatus = 'PENDENTE' | 'EM_ANDAMENTO' | 'CONCLUIDA';

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface ApiError {
  status: number;
  message: string;
  timestamp?: string;
  errors?: Record<string, string>;
}

export interface RegisterRequest {
  nome: string;
  email: string;
  senha: string;
}

export interface LoginRequest {
  email: string;
  senha: string;
}

export interface MateriaRequest {
  nome: string;
  professor?: string;
  semestre?: string;
  descricao?: string;
}

export interface TarefaRequest {
  titulo: string;
  descricao?: string;
  dataEntrega?: string;
  status?: TaskStatus;
  materiaId: number;
}

export interface NotaRequest {
  valor: number;
  peso: number;
  observacao?: string;
  materiaId: number;
}

export type AuditAction = 'CREATE' | 'UPDATE' | 'DELETE';
export type AuditEntity = 'MATERIA';

export interface AuditLog {
  id: number;
  entity: AuditEntity;
  action: AuditAction;
  entityId: number;
  userEmail: string;
  timestamp: string;
}
