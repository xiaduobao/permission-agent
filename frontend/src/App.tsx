import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import Login from './pages/Login';
import MainLayout from './layouts/MainLayout';
import Dashboard from './pages/Dashboard';
import Users from './pages/system/Users';
import Roles from './pages/system/Roles';
import Menus from './pages/system/Menus';
import Depts from './pages/system/Depts';
import Apis from './pages/system/Apis';
import Tenants from './pages/system/Tenants';
import AiChat from './pages/ai/Chat';
import Risks from './pages/ai/Risks';
import Templates from './pages/ai/Templates';
import Logs from './pages/audit/Logs';
import { useAuthStore } from './store/authStore';

function PrivateRoute({ children }: { children: React.ReactNode }) {
  const token = useAuthStore((s) => s.token);
  return token ? <>{children}</> : <Navigate to="/login" replace />;
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route
          path="/"
          element={
            <PrivateRoute>
              <MainLayout />
            </PrivateRoute>
          }
        >
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route path="dashboard" element={<Dashboard />} />
          <Route path="system/users" element={<Users />} />
          <Route path="system/roles" element={<Roles />} />
          <Route path="system/menus" element={<Menus />} />
          <Route path="system/depts" element={<Depts />} />
          <Route path="system/apis" element={<Apis />} />
          <Route path="system/tenants" element={<Tenants />} />
          <Route path="ai/chat" element={<AiChat />} />
          <Route path="ai/risks" element={<Risks />} />
          <Route path="ai/templates" element={<Templates />} />
          <Route path="audit/logs" element={<Logs />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
