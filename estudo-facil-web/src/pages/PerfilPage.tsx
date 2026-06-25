import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';

export default function PerfilPage() {
  const { usuario, carregarPerfil } = useAuth();
  const [erro, setErro] = useState('');

  useEffect(() => {
    carregarPerfil().catch((err) => setErro(err.message));
  }, [carregarPerfil]);

  if (erro) return <div className="alert alert-error">{erro}</div>;
  if (!usuario) return <p>Carregando...</p>;

  return (
    <div>
      <h2>Meu Perfil</h2>
      <div className="profile-card">
        <div className="profile-field">
          <span className="profile-label">Nome</span>
          <span>{usuario.nome}</span>
        </div>
        <div className="profile-field">
          <span className="profile-label">Email</span>
          <span>{usuario.email}</span>
        </div>
        <div className="profile-field">
          <span className="profile-label">Membro desde</span>
          <span>{new Date(usuario.criadoEm).toLocaleDateString('pt-BR')}</span>
        </div>
      </div>
    </div>
  );
}
