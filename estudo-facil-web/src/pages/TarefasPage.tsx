import { useEffect, useState } from 'react';
import { apiFetch, buildQuery } from '../api/client';
import type { Materia, Tarefa, TarefaRequest, PageResponse, TaskStatus } from '../api/types';
import FormModal from '../components/FormModal';

const STATUS_OPTIONS: TaskStatus[] = ['PENDENTE', 'EM_ANDAMENTO', 'CONCLUIDA'];

const emptyForm: TarefaRequest = { titulo: '', descricao: '', dataEntrega: '', status: 'PENDENTE', materiaId: 0 };

export default function TarefasPage() {
  const [tarefas, setTarefas] = useState<Tarefa[]>([]);
  const [materias, setMaterias] = useState<Materia[]>([]);
  const [status, setStatus] = useState('');
  const [materiaId, setMateriaId] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [erro, setErro] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  const [editId, setEditId] = useState<number | null>(null);
  const [form, setForm] = useState<TarefaRequest>(emptyForm);

  async function carregarMaterias() {
    const data = await apiFetch<PageResponse<Materia>>('/materias?size=100');
    setMaterias(data.content);
  }

  async function carregar() {
    setErro('');
    try {
      const qs = buildQuery({ status, materiaId: materiaId || undefined, page, size: 10 });
      const data = await apiFetch<PageResponse<Tarefa>>(`/tarefas${qs}`);
      setTarefas(data.content);
      setTotalPages(data.totalPages);
    } catch (err) {
      setErro(err instanceof Error ? err.message : 'Erro ao carregar');
    }
  }

  useEffect(() => { carregarMaterias(); }, []);
  useEffect(() => { carregar(); }, [status, materiaId, page]);

  function abrirCriar() {
    setEditId(null);
    setForm({ ...emptyForm, materiaId: materias[0]?.id ?? 0 });
    setModalOpen(true);
  }

  function abrirEditar(t: Tarefa) {
    setEditId(t.id);
    setForm({ titulo: t.titulo, descricao: t.descricao, dataEntrega: t.dataEntrega, status: t.status, materiaId: t.materiaId });
    setModalOpen(true);
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    try {
      if (editId) {
        await apiFetch(`/tarefas/${editId}`, { method: 'PUT', body: JSON.stringify(form) });
      } else {
        await apiFetch('/tarefas', { method: 'POST', body: JSON.stringify(form) });
      }
      setModalOpen(false);
      carregar();
    } catch (err) {
      setErro(err instanceof Error ? err.message : 'Erro ao salvar');
    }
  }

  async function handleDelete(id: number) {
    if (!confirm('Excluir esta tarefa?')) return;
    try {
      await apiFetch(`/tarefas/${id}`, { method: 'DELETE' });
      carregar();
    } catch (err) {
      setErro(err instanceof Error ? err.message : 'Erro ao excluir');
    }
  }

  return (
    <div>
      <div className="page-header">
        <h2>Tarefas</h2>
        <button className="btn btn-primary" onClick={abrirCriar} disabled={materias.length === 0}>+ Nova Tarefa</button>
      </div>

      {erro && <div className="alert alert-error">{erro}</div>}

      <div className="filters">
        <select value={status} onChange={(e) => { setStatus(e.target.value); setPage(0); }}>
          <option value="">Todos os status</option>
          {STATUS_OPTIONS.map((s) => <option key={s} value={s}>{s}</option>)}
        </select>
        <select value={materiaId} onChange={(e) => { setMateriaId(e.target.value); setPage(0); }}>
          <option value="">Todas as matérias</option>
          {materias.map((m) => <option key={m.id} value={m.id}>{m.nome}</option>)}
        </select>
      </div>

      <table className="data-table">
        <thead>
          <tr>
            <th>Título</th>
            <th>Matéria</th>
            <th>Status</th>
            <th>Entrega</th>
            <th>Ações</th>
          </tr>
        </thead>
        <tbody>
          {tarefas.map((t) => (
            <tr key={t.id}>
              <td>{t.titulo}</td>
              <td>{t.nomeMateria}</td>
              <td><span className={`badge badge-${t.status.toLowerCase()}`}>{t.status}</span></td>
              <td>{t.dataEntrega || '—'}</td>
              <td className="actions">
                <button className="btn btn-sm btn-secondary" onClick={() => abrirEditar(t)}>Editar</button>
                <button className="btn btn-sm btn-danger" onClick={() => handleDelete(t.id)}>Excluir</button>
              </td>
            </tr>
          ))}
          {tarefas.length === 0 && (
            <tr><td colSpan={5} className="empty">Nenhuma tarefa encontrada</td></tr>
          )}
        </tbody>
      </table>

      <div className="pagination">
        <button disabled={page === 0} onClick={() => setPage(page - 1)}>Anterior</button>
        <span>Página {page + 1} de {totalPages || 1}</span>
        <button disabled={page + 1 >= totalPages} onClick={() => setPage(page + 1)}>Próxima</button>
      </div>

      <FormModal title={editId ? 'Editar Tarefa' : 'Nova Tarefa'} open={modalOpen} onClose={() => setModalOpen(false)} onSubmit={handleSubmit}>
        <label>Título *<input value={form.titulo} onChange={(e) => setForm({ ...form, titulo: e.target.value })} required /></label>
        <label>Descrição<textarea value={form.descricao} onChange={(e) => setForm({ ...form, descricao: e.target.value })} /></label>
        <label>Data de Entrega<input type="date" value={form.dataEntrega} onChange={(e) => setForm({ ...form, dataEntrega: e.target.value })} /></label>
        <label>Status
          <select value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value as TaskStatus })}>
            {STATUS_OPTIONS.map((s) => <option key={s} value={s}>{s}</option>)}
          </select>
        </label>
        <label>Matéria *
          <select value={form.materiaId} onChange={(e) => setForm({ ...form, materiaId: Number(e.target.value) })} required>
            {materias.map((m) => <option key={m.id} value={m.id}>{m.nome}</option>)}
          </select>
        </label>
      </FormModal>
    </div>
  );
}
