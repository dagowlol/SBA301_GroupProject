import { Link, useLocation } from 'react-router-dom';
import { 
  LayoutDashboard, 
  Tags, 
  Package, 
  Calendar, 
  Activity, 
  Cpu, 
  Newspaper, 
  Users, 
  BarChart3, 
  LogOut 
} from 'lucide-react';

export default function Sidebar() {
  const location = useLocation();

  const menuItems = [
    { name: 'Dashboard', icon: LayoutDashboard, path: '#', badge: null },
    { name: 'Auction Categories', icon: Tags, path: '/admin/categories', badge: 'F08' },
    { name: 'Auction Items', icon: Package, path: '/admin/items', badge: 'F09' },
    { name: 'Auction Sessions', icon: Calendar, path: '#', badge: 'F10' },
    { name: 'Bid Monitoring', icon: Activity, path: '#', badge: 'F11' },
    { name: 'Auto-Bid Management', icon: Cpu, path: '#', badge: 'F12' },
    { name: 'News Management', icon: Newspaper, path: '#', badge: 'F13' },
    { name: 'User Management', icon: Users, path: '/admin/users', badge: 'F14' },
    { name: 'Auction Statistics', icon: BarChart3, path: '#', badge: 'F15' },
  ];

  return (
    <div 
      className="d-flex flex-column text-white min-vh-100 flex-shrink-0"
      style={{ width: '260px', backgroundColor: '#004e64', borderRight: '1px solid #003a4b' }}
    >
      {/* Brand Logo Header */}
      <div className="p-4" style={{ backgroundColor: '#003a4b' }}>
        <h4 className="m-0 fw-bold tracking-wide text-white">Annexe Auction</h4>
        <span className="text-white-50 small font-monospace tracking-widest uppercase">Staff Dashboard</span>
      </div>

      {/* Navigation List */}
      <div className="d-flex flex-column justify-content-between flex-grow-1 p-3">
        <ul className="nav nav-pills flex-column mb-auto gap-1">
          {menuItems.map((item, idx) => {
            const isActive = location.pathname === item.path;
            const linkProps = item.path === '#' 
              ? { onClick: (e) => e.preventDefault(), href: '#' } 
              : { as: Link, to: item.path };

            return (
              <li key={idx} className="nav-item">
                <Link 
                  to={item.path === '#' ? '#' : item.path}
                  onClick={item.path === '#' ? (e) => e.preventDefault() : undefined}
                  className={`nav-link text-white d-flex align-items-center justify-content-between py-2 px-3 rounded ${
                    isActive ? 'active-sidebar' : 'hover-sidebar'
                  }`}
                  style={{
                    backgroundColor: isActive ? '#00607a' : 'transparent',
                    transition: 'all 0.2s ease',
                    fontSize: '0.92rem'
                  }}
                >
                  <div className="d-flex align-items-center gap-2">
                    <item.icon size={16} className={isActive ? 'text-white' : 'text-white-50'} />
                    <span>{item.name}</span>
                  </div>
                  {item.badge && (
                    <span 
                      className="badge font-monospace text-white-50 px-1 py-0.5 rounded border border-secondary"
                      style={{ fontSize: '0.65rem', backgroundColor: 'rgba(255,255,255,0.08)' }}
                    >
                      {item.badge}
                    </span>
                  )}
                </Link>
              </li>
            );
          })}
        </ul>

        {/* Sign Out Action */}
        <div className="pt-3 border-top border-white-10">
          <Link 
            to="/" 
            className="nav-link text-white d-flex align-items-center gap-2 py-2 px-3 rounded hover-sidebar"
            style={{ fontSize: '0.92rem', transition: 'all 0.2s' }}
          >
            <LogOut size={16} className="text-white-50" />
            <span>Sign Out</span>
          </Link>
        </div>
      </div>
    </div>
  );
}
