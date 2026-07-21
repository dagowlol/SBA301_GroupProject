import { Outlet } from 'react-router-dom';
import Sidebar from '../features/staff/components/Sidebar';
import Header from '../features/staff/components/Header';
import './AdminLayout.css';

export default function AdminLayout() {
  return (
    <div className="admin-layout d-flex min-vh-100 bg-light" style={{ overflow: 'hidden' }}>
      {/* Dashboard Sidebar Navigation */}
      <Sidebar />

      {/* Main Panel Content Area */}
      <div className="d-flex flex-column flex-grow-1" style={{ height: '100vh', overflowY: 'auto' }}>
        <Header />
        
        <div className="admin-main-content flex-grow-1 p-4 bg-light">
          <Outlet />
        </div>
      </div>
    </div>
  );
}
