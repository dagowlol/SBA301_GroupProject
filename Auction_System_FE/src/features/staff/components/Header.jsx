import { useLocation } from 'react-router-dom';

export default function Header() {
  const location = useLocation();

  let breadcrumb = "Dashboard";
  if (location.pathname === "/admin/categories") {
    breadcrumb = "F08 — Auction Category Management";
  } else if (location.pathname === "/admin/items") {
    breadcrumb = "F09 — Auction Item Management";
  }

  return (
    <header 
      className="d-flex align-items-center justify-content-between px-4 py-3 bg-white border-bottom shadow-xs"
      style={{ height: '70px' }}
    >
      {/* Breadcrumb Info */}
      <div className="text-muted small fw-semibold">
        {breadcrumb}
      </div>

      {/* User Information */}
      <div className="d-flex align-items-center gap-2">
        <div 
          className="rounded-circle d-flex align-items-center justify-content-center fw-bold text-white shadow-sm"
          style={{ width: '40px', height: '40px', backgroundColor: '#004e64', fontSize: '0.9rem' }}
        >
          SM
        </div>
        <div className="d-flex flex-column text-start" style={{ lineHeight: '1.2' }}>
          <span className="fw-bold text-dark" style={{ fontSize: '0.88rem' }}>Staff Maria</span>
          <span className="text-muted" style={{ fontSize: '0.75rem' }}>Staff</span>
        </div>
      </div>
    </header>
  );
}
