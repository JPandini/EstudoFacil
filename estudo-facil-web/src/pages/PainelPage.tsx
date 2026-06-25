import { useEffect, useState } from 'react';
import { apiFetch } from '../api/client';
import type { Dashboard } from '../api/types';

export default function PainelPage() {
  const [dashboard, setDashboard] = useState<Dashboard | null>(null);
  const [erro, setErro] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    apiFetch<Dashboard>('/painel')
      .then(setDashboard)
      .catch((err) => setErro(err.message))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <p>Carregando...</p>;
  if (erro) return <div className="alert alert-error">{erro}</div>;
  if (!dashboard) return null;

  const cards = [
    { label: 'Matérias', value: dashboard.totalMaterias, color: '#3b82f6' },
    { label: 'Tarefas Pendentes', value: dashboard.tarefasPendentes, color: '#f59e0b' },
    { label: 'Em Andamento', value: dashboard.tarefasEmAndamento, color: '#8b5cf6' },
    { label: 'Concluídas', value: dashboard.tarefasConcluidas, color: '#22c55e' },
    { label: 'Média Geral', value: dashboard.mediaGeral ?? '—', color: '#ef4444' },
  ];

  return (
    <div>
      <h2>Painel Acadêmico</h2>
      <div className="cards-grid">
        {cards.map((card) => (
          <div key={card.label} className="card" style={{ borderTopColor: card.color }}>
            <span className="card-label">{card.label}</span>
            <span className="card-value">{card.value}</span>
          </div>
        ))}
      </div>
    </div>
  );
}
