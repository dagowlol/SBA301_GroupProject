import { useContext, useState, useRef, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { User, Settings, Home, LogOut, ChevronDown, ChevronUp } from 'lucide-react';
import { AuthContext } from '../../../context/AuthContext';
import { auctionSocketService } from '../../../features/auction/services/auctionSocketService';

const ROLE_LABELS = {
  ADMIN: 'Admin',
  AUCTION_MANAGER: 'Auction Manager',
  USER: 'User',
};

export default function Header() {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, logout } = useContext(AuthContext);
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const dropdownRef = useRef(null);

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setDropdownOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleLogout = async () => {
    auctionSocketService.disconnect();
    await logout();
    setDropdownOpen(false);
    navigate('/');
  };

  const firstName = user?.firstName || '';
  const lastName = user?.lastName || '';
  const fullName = [firstName, lastName].filter(Boolean).join(' ') || 'Staff';
  const initials = [firstName?.[0], lastName?.[0]].filter(Boolean).join('').toUpperCase() || '??';
  const roleLabel = ROLE_LABELS[user?.role] || user?.role || 'Staff';

  let breadcrumb = 'Dashboard';
  if (location.pathname === '/admin/categories') {
    breadcrumb = 'F08 — Auction Category Management';
  } else if (location.pathname === '/admin/items') {
    breadcrumb = 'F09 — Auction Item Management';
  } else if (location.pathname === '/admin/sessions') {
    breadcrumb = 'F10 — Auction Session Management';
  } else if (location.pathname === '/admin/bid-monitoring') {
    breadcrumb = 'F11 — Bid Monitoring';
  } else if (location.pathname === '/admin/users') {
    breadcrumb = 'F14 — User Management';
  }

  return (
    <header
      className="d-flex align-items-center justify-content-between px-4 py-3 bg-white border-bottom shadow-xs"
      style={{ height: '70px' }}
    >
      <div className="text-muted small fw-semibold">{breadcrumb}</div>

      {/* User Dropdown */}
      <div className="position-relative" ref={dropdownRef}>
        <button
          className="d-flex align-items-center gap-2 border-0 bg-transparent p-1 rounded"
          onClick={() => setDropdownOpen(!dropdownOpen)}
          style={{ cursor: 'pointer' }}
        >
          <div
            className="rounded-circle d-flex align-items-center justify-content-center fw-bold text-white shadow-sm"
            style={{ width: '40px', height: '40px', backgroundColor: '#004e64', fontSize: '0.9rem' }}
          >
            {initials}
          </div>
          <div className="d-flex flex-column text-start" style={{ lineHeight: '1.2' }}>
            <span className="fw-bold text-dark" style={{ fontSize: '0.88rem' }}>{fullName}</span>
            <span className="text-muted" style={{ fontSize: '0.75rem' }}>{roleLabel}</span>
          </div>
          {dropdownOpen ? <ChevronUp size={16} className="text-muted" /> : <ChevronDown size={16} className="text-muted" />}
        </button>

        {dropdownOpen && (
          <div
            className="position-absolute bg-white rounded-3 shadow-lg border mt-2"
            style={{ right: 0, width: '240px', zIndex: 1050 }}
          >
            {/* User Info Header */}
            <div className="px-3 py-3 border-bottom">
              <div className="fw-bold text-dark" style={{ fontSize: '0.95rem' }}>{fullName}</div>
              <div className="text-muted" style={{ fontSize: '0.8rem' }}>{user?.email || ''}</div>
              <span
                className="badge mt-1"
                style={{ backgroundColor: '#004e64', fontSize: '0.7rem' }}
              >
                {roleLabel}
              </span>
            </div>

            {/* Menu Items */}
            <div className="py-1">
              <button
                className="dropdown-item d-flex align-items-center gap-2 px-3 py-2"
                onClick={() => { navigate('/my-account'); setDropdownOpen(false); }}
              >
                <User size={16} />
                <span>My Account</span>
              </button>
              <button
                className="dropdown-item d-flex align-items-center gap-2 px-3 py-2"
                onClick={() => { navigate('/'); setDropdownOpen(false); }}
              >
                <Home size={16} />
                <span>Back to Store</span>
              </button>
            </div>

            {/* Logout */}
            <div className="border-top py-1">
              <button
                className="dropdown-item d-flex align-items-center gap-2 px-3 py-2 text-danger"
                onClick={handleLogout}
              >
                <LogOut size={16} />
                <span>Sign Out</span>
              </button>
            </div>
          </div>
        )}
      </div>
    </header>
  );
}
