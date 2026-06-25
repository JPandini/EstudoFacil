import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Layout() {
  const { usuario, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate('/login');
  }

  return (
    <div className="layout">
      <aside className="sidebar">
        <h1 className="logo">Estudo Fácil</h1>
        <nav>
          <NavLink to="/painel">Painel</NavLink>
          <NavLink to="/materias">Matérias</NavLink>
          <NavLink to="/auditoria">Auditoria</NavLink>
          <NavLink to="/tarefas">Tarefas</NavLink>
          <NavLink to="/notas">Notas</NavLink>
          <NavLink to="/perfil">Perfil</NavLink>
        </nav>
        <div className="sidebar-footer">
          <span>{usuario?.nome}</span>
          <button onClick={handleLogout} className="btn btn-secondary btn-sm">Sair</button>
        </div>
      </aside>
      <main className="content">
        <Outlet />
      </main>
    </div>
  );
}
