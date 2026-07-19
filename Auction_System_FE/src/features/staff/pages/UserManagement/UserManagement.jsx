import React, { useState, useEffect, useMemo } from 'react';
import { Plus, Search, Pencil, UserX, UserCheck, ShieldPlus, ShieldMinus, RefreshCw, X } from 'lucide-react';
import { message } from 'antd';
import { userApi } from '../../../../api/userApi';
import './UserManagement.css';

// ─── Role helpers ───────────────────────────────────────────────────────────
const ROLES = ['USER', 'AUCTION_MANAGER', 'ADMIN'];

const ROLE_META = {
  USER:             { label: 'User',  sub: 'Seller / Buyer',   badge: 'badge-buyer',  dot: 'dot-buyer'  },
  AUCTION_MANAGER:  { label: 'Staff', sub: 'Auction Manager',  badge: 'badge-seller', dot: 'dot-seller' },
  ADMIN:            { label: 'Admin', sub: 'Administrator',    badge: 'badge-admin',  dot: 'dot-admin'  },
};

function getRoleLabel(role) {
  return ROLE_META[role]?.label ?? (role || 'N/A');
}

function getRoleSub(role) {
  return ROLE_META[role]?.sub ?? '';
}

function getRoleBadgeClass(role) {
  return ROLE_META[role]?.badge ?? 'badge-default';
}

function getStatusClass(status) {
  const map = { ACTIVE: 'status-active', SUSPENDED: 'status-suspended', PENDING: 'status-pending' };
  return map[status] ?? 'status-active';
}

// ─── Small summary card ──────────────────────────────────────────────────────
function RoleSummaryCards({ users }) {
  const counts = useMemo(() => ({
    USER:            users.filter(u => u.role === 'USER').length,
    AUCTION_MANAGER: users.filter(u => u.role === 'AUCTION_MANAGER').length,
    ADMIN:           users.filter(u => u.role === 'ADMIN').length,
  }), [users]);

  return (
    <div className="um-role-summary">
      {ROLES.map(r => (
        <div key={r} className="um-role-card">
          <div className="um-role-card-label">
            <span className={`um-role-dot ${ROLE_META[r].dot}`} />
            <div>
              <span className="um-role-card-title">{ROLE_META[r].label}</span>
              <span className="um-role-card-sub">{ROLE_META[r].sub}</span>
            </div>
          </div>
          <div className="um-role-card-count">{counts[r]}</div>
        </div>
      ))}
    </div>
  );
}

// ─── Create User Modal ───────────────────────────────────────────────────────
function CreateUserModal({ onClose, onSave }) {
  const [form, setForm] = useState({ firstName: '', lastName: '', email: '', phoneNumber: '', password: '', address: '' });
  const [loading, setLoading] = useState(false);

  const handleChange = e => setForm(p => ({ ...p, [e.target.name]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await userApi.createUser(form);
      message.success('User created successfully');
      onSave();
    } catch (err) {
      message.error(err.message || 'Error creating user');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="um-modal-overlay">
      <div className="um-modal">
        <div className="um-modal-header">
          <h2>Add New User</h2>
          <button className="um-modal-close" onClick={onClose}><X size={20} /></button>
        </div>
        <form onSubmit={handleSubmit}>
          <div className="um-form-grid-2">
            <div className="um-form-group">
              <label className="um-form-label">First Name *</label>
              <input className="um-form-control" name="firstName" required value={form.firstName} onChange={handleChange} placeholder="John" />
            </div>
            <div className="um-form-group">
              <label className="um-form-label">Last Name *</label>
              <input className="um-form-control" name="lastName" required value={form.lastName} onChange={handleChange} placeholder="Doe" />
            </div>
          </div>
          <div className="um-form-group">
            <label className="um-form-label">Email *</label>
            <input className="um-form-control" type="email" name="email" required value={form.email} onChange={handleChange} placeholder="john@example.com" />
          </div>
          <div className="um-form-group">
            <label className="um-form-label">Password *</label>
            <input className="um-form-control" type="password" name="password" required value={form.password} onChange={handleChange} placeholder="Min. 6 characters" />
          </div>
          <div className="um-form-grid-2">
            <div className="um-form-group">
              <label className="um-form-label">Phone</label>
              <input className="um-form-control" name="phoneNumber" value={form.phoneNumber} onChange={handleChange} placeholder="0901234567" />
            </div>
            <div className="um-form-group">
              <label className="um-form-label">Address</label>
              <input className="um-form-control" name="address" value={form.address} onChange={handleChange} placeholder="123 Main St" />
            </div>
          </div>
          <div className="um-modal-footer">
            <button type="button" className="um-btn-cancel" onClick={onClose}>Cancel</button>
            <button type="submit" className="um-btn-submit" disabled={loading}>{loading ? 'Creating…' : 'Create User'}</button>
          </div>
        </form>
      </div>
    </div>
  );
}

// ─── Edit User Modal ─────────────────────────────────────────────────────────
function EditUserModal({ user, onClose, onSave }) {
  const [form, setForm] = useState({ firstName: user.firstName || '', lastName: user.lastName || '', phoneNumber: user.phoneNumber || '', address: user.address || '' });
  const [loading, setLoading] = useState(false);

  const handleChange = e => setForm(p => ({ ...p, [e.target.name]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await userApi.updateUser(user.id, form);
      message.success('User updated successfully');
      onSave();
    } catch (err) {
      message.error(err.message || 'Error updating user');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="um-modal-overlay">
      <div className="um-modal">
        <div className="um-modal-header">
          <h2>Edit User</h2>
          <button className="um-modal-close" onClick={onClose}><X size={20} /></button>
        </div>
        <form onSubmit={handleSubmit}>
          <div className="um-form-grid-2">
            <div className="um-form-group">
              <label className="um-form-label">First Name</label>
              <input className="um-form-control" name="firstName" required value={form.firstName} onChange={handleChange} />
            </div>
            <div className="um-form-group">
              <label className="um-form-label">Last Name</label>
              <input className="um-form-control" name="lastName" required value={form.lastName} onChange={handleChange} />
            </div>
          </div>
          <div className="um-form-grid-2">
            <div className="um-form-group">
              <label className="um-form-label">Phone</label>
              <input className="um-form-control" name="phoneNumber" value={form.phoneNumber} onChange={handleChange} />
            </div>
            <div className="um-form-group">
              <label className="um-form-label">Address</label>
              <input className="um-form-control" name="address" value={form.address} onChange={handleChange} />
            </div>
          </div>
          <div className="um-modal-footer">
            <button type="button" className="um-btn-cancel" onClick={onClose}>Cancel</button>
            <button type="submit" className="um-btn-submit" disabled={loading}>{loading ? 'Saving…' : 'Save Changes'}</button>
          </div>
        </form>
      </div>
    </div>
  );
}

// ─── Assign Role Modal ───────────────────────────────────────────────────────
function AssignRoleModal({ user, onClose, onSave }) {
  const [role, setRole] = useState(user.role || 'USER');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await userApi.assignRole(user.id, { role });
      message.success('Role updated successfully');
      onSave();
    } catch (err) {
      message.error(err.message || 'Error assigning role');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="um-modal-overlay">
      <div className="um-modal um-modal-sm">
        <div className="um-modal-header">
          <h2>Assign Role</h2>
          <button className="um-modal-close" onClick={onClose}><X size={20} /></button>
        </div>
        <form onSubmit={handleSubmit}>
          <p style={{ fontSize: '0.85rem', color: '#64748b', marginBottom: '1rem' }}>
            Select role for <strong>{user.firstName} {user.lastName}</strong>
          </p>
          <div className="um-role-options">
            {ROLES.map(r => (
              <button
                key={r}
                type="button"
                className={`um-role-option${role === r ? ' selected' : ''}`}
                onClick={() => setRole(r)}
              >
                <span className="um-role-option-label">{getRoleLabel(r)}</span>
                <span className="um-role-option-sub">{getRoleSub(r)}</span>
              </button>
            ))}
          </div>
          <div className="um-modal-footer">
            <button type="button" className="um-btn-cancel" onClick={onClose}>Cancel</button>
            <button type="submit" className="um-btn-submit" disabled={loading}>{loading ? 'Saving…' : 'Update Role'}</button>
          </div>
        </form>
      </div>
    </div>
  );
}

// ─── Status Modal ────────────────────────────────────────────────────────────
function StatusModal({ user, onClose, onSave }) {
  const nextStatus = user.status === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE';
  const [loading, setLoading] = useState(false);

  const handleConfirm = async () => {
    setLoading(true);
    try {
      await userApi.updateStatus(user.id, { status: nextStatus });
      message.success(`User ${nextStatus === 'SUSPENDED' ? 'suspended' : 'reactivated'} successfully`);
      onSave();
    } catch (err) {
      message.error(err.message || 'Error updating status');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="um-modal-overlay">
      <div className="um-modal um-modal-sm">
        <div className="um-modal-header">
          <h2>{nextStatus === 'SUSPENDED' ? 'Suspend User' : 'Reactivate User'}</h2>
          <button className="um-modal-close" onClick={onClose}><X size={20} /></button>
        </div>
        <p style={{ fontSize: '0.88rem', color: '#475569', marginBottom: '1rem' }}>
          Are you sure you want to <strong>{nextStatus === 'SUSPENDED' ? 'suspend' : 'reactivate'}</strong>{' '}
          <strong>{user.firstName} {user.lastName}</strong>?
        </p>
        <div className="um-modal-footer">
          <button className="um-btn-cancel" onClick={onClose}>Cancel</button>
          <button
            className="um-btn-submit"
            style={{ backgroundColor: nextStatus === 'SUSPENDED' ? '#dc2626' : '#16a34a' }}
            disabled={loading}
            onClick={handleConfirm}
          >
            {loading ? 'Saving…' : nextStatus === 'SUSPENDED' ? 'Suspend' : 'Reactivate'}
          </button>
        </div>
      </div>
    </div>
  );
}

// ─── Main Component ──────────────────────────────────────────────────────────
const ROLE_FILTERS = ['All', 'USER', 'AUCTION_MANAGER', 'ADMIN'];
const STATUS_FILTERS = ['All', 'ACTIVE', 'SUSPENDED'];

export default function UserManagement() {
  const [users,        setUsers]        = useState([]);
  const [loading,      setLoading]      = useState(true);
  const [error,        setError]        = useState(null);
  const [search,       setSearch]       = useState('');
  const [roleFilter,   setRoleFilter]   = useState('All');
  const [statusFilter, setStatusFilter] = useState('All');
  const [modal,        setModal]        = useState(null); // { type, user? }

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const data = await userApi.getAllUsers();
      setUsers(data || []);
      setError(null);
    } catch (err) {
      setError(err.message || 'Failed to fetch users');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchUsers(); }, []);

  const filtered = useMemo(() => {
    return users.filter(u => {
      const nameMatch  = `${u.firstName} ${u.lastName} ${u.email}`.toLowerCase().includes(search.toLowerCase());
      const roleMatch  = roleFilter   === 'All' || u.role   === roleFilter;
      const statMatch  = statusFilter === 'All' || u.status === statusFilter;
      return nameMatch && roleMatch && statMatch;
    });
  }, [users, search, roleFilter, statusFilter]);

  const handleModalSave = () => {
    setModal(null);
    fetchUsers();
  };

  const handleDelete = async (user) => {
    if (!window.confirm(`Delete user "${user.firstName} ${user.lastName}"? This cannot be undone.`)) return;
    try {
      await userApi.deleteUser(user.id);
      message.success('User deleted successfully');
      fetchUsers();
    } catch (err) {
      message.error(err.message || 'Error deleting user');
    }
  };

  return (
    <div className="um-container">
      {/* ── Header ── */}
      <div className="um-page-header">
        <div>
          <h1>User Management</h1>
          <p className="um-subtitle">Manage accounts, assign permissions, suspend or reactivate</p>
        </div>
        <button id="um-add-user-btn" className="um-btn-add" onClick={() => setModal({ type: 'create' })}>
          <Plus size={16} /> Add User
        </button>
      </div>

      {/* ── Role Summary ── */}
      <RoleSummaryCards users={users} />

      {/* ── Filter Bar ── */}
      <div className="um-filter-bar">
        <div className="um-search-wrapper">
          <Search size={14} className="um-search-icon" />
          <input
            id="um-search-input"
            className="um-search-input"
            placeholder="Search username or email…"
            value={search}
            onChange={e => setSearch(e.target.value)}
          />
        </div>

        <div className="um-filter-group" role="group" aria-label="Filter by role">
          {ROLE_FILTERS.map(r => (
            <button
              key={r}
              id={`um-role-filter-${r.toLowerCase()}`}
              className={`um-filter-btn${roleFilter === r ? ' active' : ''}`}
              onClick={() => setRoleFilter(r)}
            >
              {r === 'All' ? 'All' : getRoleLabel(r)}
            </button>
          ))}
        </div>

        <div className="um-filter-group" role="group" aria-label="Filter by status">
          {STATUS_FILTERS.map(s => (
            <button
              key={s}
              id={`um-status-filter-${s.toLowerCase()}`}
              className={`um-filter-btn${statusFilter === s ? ' active' : ''}`}
              onClick={() => setStatusFilter(s)}
            >
              {s}
            </button>
          ))}
        </div>

        <button id="um-refresh-btn" style={{ marginLeft: 'auto', background: 'none', border: 'none', cursor: 'pointer', color: '#64748b' }} onClick={fetchUsers} title="Refresh">
          <RefreshCw size={18} />
        </button>
      </div>

      {/* ── Table ── */}
      <div className="um-table-wrapper">
        {loading ? (
          <div className="um-loading">
            <div className="um-spinner" />
            <span>Loading users…</span>
          </div>
        ) : error ? (
          <div className="um-error-alert" role="alert">{error}</div>
        ) : (
          <table className="um-table">
            <thead>
              <tr>
                <th>Username</th>
                <th>Email</th>
                <th>Role</th>
                <th>Status</th>
                <th>Joined</th>
                <th style={{ textAlign: 'center' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filtered.length === 0 ? (
                <tr>
                  <td colSpan={6}>
                    <div className="um-empty">No users found.</div>
                  </td>
                </tr>
              ) : (
                filtered.map(user => (
                  <tr key={user.id}>
                    <td>
                      <span className="um-username">{user.firstName}_{user.lastName}</span>
                    </td>
                    <td>{user.email}</td>
                    <td>
                      <span className={`um-role-badge ${getRoleBadgeClass(user.role)}`}>
                        {getRoleLabel(user.role)}
                      </span>
                    </td>
                    <td>
                      <span className={`um-status ${getStatusClass(user.status)}`}>
                        <span className="um-status-dot" />
                        {user.status ? user.status.charAt(0) + user.status.slice(1).toLowerCase() : 'N/A'}
                      </span>
                    </td>
                    <td>
                      {user.createdAt ? new Date(user.createdAt).toLocaleDateString('sv-SE') : '—'}
                    </td>
                    <td>
                      <div className="um-actions" style={{ justifyContent: 'center' }}>
                        {/* Edit */}
                        <button
                          id={`um-edit-btn-${user.id}`}
                          className="um-action-btn edit"
                          title="Edit user"
                          onClick={() => setModal({ type: 'edit', user })}
                        >
                          <Pencil size={15} />
                        </button>

                        {/* Assign Role */}
                        <button
                          id={`um-role-btn-${user.id}`}
                          className={`um-action-btn ${user.role === 'ADMIN' ? 'demote' : 'promote'}`}
                          title="Assign role"
                          onClick={() => setModal({ type: 'role', user })}
                        >
                          {user.role === 'ADMIN' ? <ShieldMinus size={15} /> : <ShieldPlus size={15} />}
                        </button>

                        {/* Suspend / Reactivate */}
                        <button
                          id={`um-status-btn-${user.id}`}
                          className={`um-action-btn ${user.status === 'ACTIVE' ? 'suspend' : 'reactivate'}`}
                          title={user.status === 'ACTIVE' ? 'Suspend user' : 'Reactivate user'}
                          onClick={() => setModal({ type: 'status', user })}
                        >
                          {user.status === 'ACTIVE' ? <UserX size={15} /> : <UserCheck size={15} />}
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        )}
      </div>

      {/* ── Modals ── */}
      {modal?.type === 'create' && (
        <CreateUserModal onClose={() => setModal(null)} onSave={handleModalSave} />
      )}
      {modal?.type === 'edit' && (
        <EditUserModal user={modal.user} onClose={() => setModal(null)} onSave={handleModalSave} />
      )}
      {modal?.type === 'role' && (
        <AssignRoleModal user={modal.user} onClose={() => setModal(null)} onSave={handleModalSave} />
      )}
      {modal?.type === 'status' && (
        <StatusModal user={modal.user} onClose={() => setModal(null)} onSave={handleModalSave} />
      )}
    </div>
  );
}
