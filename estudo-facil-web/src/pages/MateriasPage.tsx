import { useEffect, useState } from 'react';
import { apiFetch, buildQuery } from '../api/client';
import type { Materia, MateriaRequest, PageResponse } from '../api/types';
import FormModal from '../components/FormModal';

const emptyForm: MateriaRequest = { nome: '', professor: '', semestre: '', descricao: '' };

export default function MateriasPage() {
  const [materias, setMaterias] = useState<Materia[]>([]);
  const [semestre, setSemestre] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [erro, setErro] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  const [editId, setEditId] = useState<number | null>(null);
  const [form, setForm] = useState<MateriaRequest>(emptyForm);

  async function carregar() {
    setErro('');
    try {
      const qs = buildQuery({ semestre, page, size: 10 });
      const data = await apiFetch<PageResponse<Materia>>(`/materias${qs}`);
      setMaterias(data.content);
      setTotalPages(data.totalPages);
    } catch (err) {
      setErro(err instanceof Error ? err.message : 'Erro ao carregar');
    }
  }

  useEffect(() => { carregar(); }, [semestre, page]);

  function abrirCriar() {
    setEditId(null);
    setForm(emptyForm);
    setModalOpen(true);
  }

  function abrirEditar(m: Materia) {
    setEditId(m.id);
    setForm({ nome: m.nome, professor: m.professor, semestre: m.semestre, descricao: m.descricao });
    setModalOpen(true);
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    try {
      if (editId) {
        await apiFetch(`/materias/${editId}`, { method: 'PUT', body: JSON.stringify(form) });
      } else {
        await apiFetch('/materias', { method: 'POST', body: JSON.stringify(form) });
      }
      setModalOpen(false);
      carregar();
    } catch (err) {
      setErro(err instanceof Error ? err.message : 'Erro ao salvar');
    }
  }

  async function handleDelete(id: number) {
    if (!confirm('Excluir esta matéria?')) return;
    try {
      await apiFetch(`/materias/${id}`, { method: 'DELETE' });
      carregar();
    } catch (err) {
      setErro(err instanceof Error ? err.message : 'Erro ao excluir');
    }
  }

  return (
    <div>
      <div className="page-header">
        <h2>Matérias</h2>
        <button className="btn btn-primary" onClick={abrirCriar}>+ Nova Matéria</button>
      </div>

      {erro && <div className="alert alert-error">{erro}</div>}

      <div className="filters">
        <input
          placeholder="Filtrar por semestre (ex: 2026-1)"
          value={semestre}
          onChange={(e) => { setSemestre(e.target.value); setPage(0); }}
        />
      </div>

      <table className="data-table">
        <thead>
          <tr>
            <th>Nome</th>
            <th>Professor</th>
            <th>Semestre</th>
            <th>Descrição</th>
            <th>Ações</th>
          </tr>
        </thead>
        <tbody>
          {materias.map((m) => (
            <tr key={m.id}>
              <td>{m.nome}</td>
              <td>{m.professor || '—'}</td>
              <td>{m.semestre || '—'}</td>
              <td>{m.descricao || '—'}</td>
              <td className="actions">
                <button className="btn btn-sm btn-secondary" onClick={() => abrirEditar(m)}>Editar</button>
                <button className="btn btn-sm btn-danger" onClick={() => handleDelete(m.id)}>Excluir</button>
              </td>
            </tr>
          ))}
          {materias.length === 0 && (
            <tr><td colSpan={5} className="empty">Nenhuma matéria encontrada</td></tr>
          )}
        </tbody>
      </table>

      <div className="pagination">
        <button disabled={page === 0} onClick={() => setPage(page - 1)}>Anterior</button>
        <span>Página {page + 1} de {totalPages || 1}</span>
        <button disabled={page + 1 >= totalPages} onClick={() => setPage(page + 1)}>Próxima</button>
      </div>

      <FormModal
        title={editId ? 'Editar Matéria' : 'Nova Matéria'}
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        onSubmit={handleSubmit}
      >
        <label>Nome *<input value={form.nome} onChange={(e) => setForm({ ...form, nome: e.target.value })} required /></label>
        <label>Professor<input value={form.professor} onChange={(e) => setForm({ ...form, professor: e.target.value })} /></label>
        <label>Semestre<input value={form.semestre} onChange={(e) => setForm({ ...form, semestre: e.target.value })} placeholder="2026-1 ou 2026-01" /></label>
        <label>Descrição<textarea value={form.descricao} onChange={(e) => setForm({ ...form, descricao: e.target.value })} /></label>
      </FormModal>
    </div>
  );
}
