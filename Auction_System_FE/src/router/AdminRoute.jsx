import { useContext } from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';

const ALLOWED_ROLES = ['ADMIN', 'AUCTION_MANAGER'];

export default function AdminRoute() {
  const { isAuthenticated, user } = useContext(AuthContext);

  if (!isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  if (!ALLOWED_ROLES.includes(user?.role)) {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
}
