import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Layout from './components/Layout';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import PainelPage from './pages/PainelPage';
import MateriasPage from './pages/MateriasPage';
import TarefasPage from './pages/TarefasPage';
import NotasPage from './pages/NotasPage';
import PerfilPage from './pages/PerfilPage';
import LogPage from './pages/LogPage';

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/registrar" element={<RegisterPage />} />
          <Route element={<ProtectedRoute />}>
            <Route element={<Layout />}>
              <Route path="/painel" element={<PainelPage />} />
              <Route path="/materias" element={<MateriasPage />} />
              <Route path="/auditoria" element={<LogPage />} />
              <Route path="/tarefas" element={<TarefasPage />} />
              <Route path="/notas" element={<NotasPage />} />
              <Route path="/perfil" element={<PerfilPage />} />
            </Route>
          </Route>
          <Route path="*" element={<Navigate to="/painel" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
