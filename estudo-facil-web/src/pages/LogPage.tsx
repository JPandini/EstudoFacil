import { useEffect, useState } from 'react';
import { apiFetch, buildApiUrl, buildQuery } from '../api/client';
import type { AuditAction, AuditEntity, AuditLog, PageResponse } from '../api/types';

type ViewMode = 'tabela' | 'json';

const ACTION_BADGE: Record<AuditAction, string> = {
  CREATE: 'badge-create',
  UPDATE: 'badge-update',
  DELETE: 'badge-delete',
};

function formatTimestamp(ts: string): string {
  return new Date(ts).toLocaleString('pt-BR');
}

function truncateToken(token: string | null): string {
  if (!token) return '(não autenticado)';
  if (token.length <= 12) return token;
  return `${token.slice(0, 8)}...${token.slice(-4)}`;
}

export default function LogPage() {
  const [logs, setLogs] = useState<AuditLog[]>([]);
  const [rawResponse, setRawResponse] = useState<PageResponse<AuditLog> | null>(null);
  const [requestPath, setRequestPath] = useState('/log');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [entity, setEntity] = useState<AuditEntity | ''>('');
  const [action, setAction] = useState<AuditAction | ''>('');
  const [entityId, setEntityId] = useState('');
  const [viewMode, setViewMode] = useState<ViewMode>('tabela');
  const [erro, setErro] = useState('');

  async function carregar() {
    setErro('');
    try {
      const qs = buildQuery({
        entity: entity || undefined,
        action: action || undefined,
        entityId: entityId ? Number(entityId) : undefined,
        page,
        size: 20,
      });
      const path = `/log${qs}`;
      const data = await apiFetch<PageResponse<AuditLog>>(path);
      setRequestPath(path);
      setRawResponse(data);
      setLogs(data.content);
      setTotalPages(data.totalPages);
    } catch (err) {
      setErro(err instanceof Error ? err.message : 'Erro ao carregar');
    }
  }

  useEffect(() => { carregar(); }, [entity, action, entityId, page]);

  const token = localStorage.getItem('token');

  return (
    <div>
      <div className="page-header">
        <h2>Auditoria</h2>
        <div className="view-toggle">
          <button
            type="button"
            className={`btn btn-sm ${viewMode === 'tabela' ? 'btn-primary' : 'btn-secondary'}`}
            onClick={() => setViewMode('tabela')}
          >
            Tabela
          </button>
          <button
            type="button"
            className={`btn btn-sm ${viewMode === 'json' ? 'btn-primary' : 'btn-secondary'}`}
            onClick={() => setViewMode('json')}
          >
            JSON
          </button>
        </div>
      </div>

      {erro && <div className="alert alert-error">{erro}</div>}

      <div className="filters">
        <select
          value={entity}
          onChange={(e) => { setEntity(e.target.value as AuditEntity | ''); setPage(0); }}
        >
          <option value="">Todas entidades</option>
          <option value="MATERIA">MATERIA</option>
        </select>
        <select
          value={action}
          onChange={(e) => { setAction(e.target.value as AuditAction | ''); setPage(0); }}
        >
          <option value="">Todas ações</option>
          <option value="CREATE">CREATE</option>
          <option value="UPDATE">UPDATE</option>
          <option value="DELETE">DELETE</option>
        </select>
        <input
          type="number"
          placeholder="ID da entidade"
          value={entityId}
          onChange={(e) => { setEntityId(e.target.value); setPage(0); }}
          min={1}
        />
      </div>

      {viewMode === 'tabela' ? (
        <table className="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Entidade</th>
              <th>Ação</th>
              <th>ID Entidade</th>
              <th>Usuário</th>
              <th>Data/Hora</th>
            </tr>
          </thead>
          <tbody>
            {logs.map((log) => (
              <tr key={log.id}>
                <td>{log.id}</td>
                <td>{log.entity}</td>
                <td>
                  <span className={`badge ${ACTION_BADGE[log.action]}`}>{log.action}</span>
                </td>
                <td>{log.entityId}</td>
                <td>{log.userEmail}</td>
                <td>{formatTimestamp(log.timestamp)}</td>
              </tr>
            ))}
            {logs.length === 0 && (
              <tr><td colSpan={6} className="empty">Nenhum registro de auditoria encontrado</td></tr>
            )}
          </tbody>
        </table>
      ) : (
        <div className="json-view">
          <div className="endpoint-box">
            <div><strong>GET</strong> {buildApiUrl(requestPath)}</div>
            <div><strong>Authorization:</strong> Bearer {truncateToken(token)}</div>
          </div>
          <pre className="json-panel">
            {rawResponse ? JSON.stringify(rawResponse, null, 2) : '{}'}
          </pre>
        </div>
      )}

      <div className="pagination">
        <button disabled={page === 0} onClick={() => setPage(page - 1)}>Anterior</button>
        <span>Página {page + 1} de {totalPages || 1}</span>
        <button disabled={page + 1 >= totalPages} onClick={() => setPage(page + 1)}>Próxima</button>
      </div>
    </div>
  );
}
