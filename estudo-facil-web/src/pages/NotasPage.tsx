import { useEffect, useState } from 'react';
import { apiFetch, buildQuery } from '../api/client';
import type { Materia, Nota, NotaRequest, PageResponse } from '../api/types';
import FormModal from '../components/FormModal';

const emptyForm: NotaRequest = { valor: 0, peso: 1, observacao: '', materiaId: 0 };

export default function NotasPage() {
  const [notas, setNotas] = useState<Nota[]>([]);
  const [materias, setMaterias] = useState<Materia[]>([]);
  const [materiaId, setMateriaId] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [erro, setErro] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  const [editId, setEditId] = useState<number | null>(null);
  const [form, setForm] = useState<NotaRequest>(emptyForm);

  async function carregarMaterias() {
    const data = await apiFetch<PageResponse<Materia>>('/materias?size=100');
    setMaterias(data.content);
  }

  async function carregar() {
    setErro('');
    try {
      const qs = buildQuery({ materiaId: materiaId || undefined, page, size: 10 });
      const data = await apiFetch<PageResponse<Nota>>(`/notas${qs}`);
      setNotas(data.content);
      setTotalPages(data.totalPages);
    } catch (err) {
      setErro(err instanceof Error ? err.message : 'Erro ao carregar');
    }
  }

  useEffect(() => { carregarMaterias(); }, []);
  useEffect(() => { carregar(); }, [materiaId, page]);

  function abrirCriar() {
    setEditId(null);
    setForm({ ...emptyForm, materiaId: materias[0]?.id ?? 0 });
    setModalOpen(true);
  }

  function abrirEditar(n: Nota) {
    setEditId(n.id);
    setForm({ valor: n.valor, peso: n.peso, observacao: n.observacao, materiaId: n.materiaId });
    setModalOpen(true);
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    try {
      if (editId) {
        await apiFetch(`/notas/${editId}`, { method: 'PUT', body: JSON.stringify(form) });
      } else {
        await apiFetch('/notas', { method: 'POST', body: JSON.stringify(form) });
      }
      setModalOpen(false);
      carregar();
    } catch (err) {
      setErro(err instanceof Error ? err.message : 'Erro ao salvar');
    }
  }

  async function handleDelete(id: number) {
    if (!confirm('Excluir esta nota?')) return;
    try {
      await apiFetch(`/notas/${id}`, { method: 'DELETE' });
      carregar();
    } catch (err) {
      setErro(err instanceof Error ? err.message : 'Erro ao excluir');
    }
  }

  return (
    <div>
      <div className="page-header">
        <h2>Notas</h2>
        <button className="btn btn-primary" onClick={abrirCriar} disabled={materias.length === 0}>+ Nova Nota</button>
      </div>

      {erro && <div className="alert alert-error">{erro}</div>}

      <div className="filters">
        <select value={materiaId} onChange={(e) => { setMateriaId(e.target.value); setPage(0); }}>
          <option value="">Todas as matérias</option>
          {materias.map((m) => <option key={m.id} value={m.id}>{m.nome}</option>)}
        </select>
      </div>

      <table className="data-table">
        <thead>
          <tr>
            <th>Matéria</th>
            <th>Valor</th>
            <th>Peso</th>
            <th>Observação</th>
            <th>Ações</th>
          </tr>
        </thead>
        <tbody>
          {notas.map((n) => (
            <tr key={n.id}>
              <td>{n.nomeMateria}</td>
              <td>{n.valor}</td>
              <td>{n.peso}</td>
              <td>{n.observacao || '—'}</td>
              <td className="actions">
                <button className="btn btn-sm btn-secondary" onClick={() => abrirEditar(n)}>Editar</button>
                <button className="btn btn-sm btn-danger" onClick={() => handleDelete(n.id)}>Excluir</button>
              </td>
            </tr>
          ))}
          {notas.length === 0 && (
            <tr><td colSpan={5} className="empty">Nenhuma nota encontrada</td></tr>
          )}
        </tbody>
      </table>

      <div className="pagination">
        <button disabled={page === 0} onClick={() => setPage(page - 1)}>Anterior</button>
        <span>Página {page + 1} de {totalPages || 1}</span>
        <button disabled={page + 1 >= totalPages} onClick={() => setPage(page + 1)}>Próxima</button>
      </div>

      <FormModal title={editId ? 'Editar Nota' : 'Nova Nota'} open={modalOpen} onClose={() => setModalOpen(false)} onSubmit={handleSubmit}>
        <label>Valor (0-10) *<input type="number" step="0.1" min="0" max="10" value={form.valor} onChange={(e) => setForm({ ...form, valor: Number(e.target.value) })} required /></label>
        <label>Peso *<input type="number" step="0.1" min="0.1" value={form.peso} onChange={(e) => setForm({ ...form, peso: Number(e.target.value) })} required /></label>
        <label>Observação<input value={form.observacao} onChange={(e) => setForm({ ...form, observacao: e.target.value })} /></label>
        <label>Matéria *
          <select value={form.materiaId} onChange={(e) => setForm({ ...form, materiaId: Number(e.target.value) })} required>
            {materias.map((m) => <option key={m.id} value={m.id}>{m.nome}</option>)}
          </select>
        </label>
      </FormModal>
    </div>
  );
}
