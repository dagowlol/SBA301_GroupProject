import React, { useState } from 'react';
import { Table, Button, Form, InputGroup, Badge, Spinner, Modal } from 'react-bootstrap';
import { Search, Edit2, Trash2, Plus, RotateCcw, Archive } from 'lucide-react';
import { useAuctionSessions, useDeleteSession, useDeletedSessions, useRestoreSession } from '../hooks/useAuctionSessions';
import useDebounce from '../hooks/useDebounce';
import SessionFormModal from './SessionFormModal';

// ─── Friendly Error Banner ────────────────────────────────────────────────────
function ErrorBanner({ error }) {
  const isUnauthorized = error?.message?.toLowerCase().includes('unauthorized')
    || error?.message?.toLowerCase().includes('permission')
    || error?.status === 401 || error?.status === 403
    || error?.code === 1001 || error?.code === 1002;

  const isNetwork = error?.message?.toLowerCase().includes('failed to fetch')
    || error?.message?.toLowerCase().includes('network');

  if (isUnauthorized) {
    return (
      <div
        className="text-center py-5 px-4 rounded-4 mb-4"
        style={{ background: 'linear-gradient(135deg, #fff1f2 0%, #ffe4e6 100%)', border: '1.5px solid #fca5a5' }}
      >
        <h5 className="fw-bold mb-1" style={{ color: '#991b1b' }}>Access Denied</h5>
        <p className="mb-0 small" style={{ color: '#b91c1c', maxWidth: 380, margin: '0 auto' }}>
          You don't have permission to view this page. Please log in with an <strong>Admin</strong> or <strong>Auction Manager</strong> account.
        </p>
      </div>
    );
  }

  if (isNetwork) {
    return (
      <div
        className="text-center py-5 px-4 rounded-4 mb-4"
        style={{ background: 'linear-gradient(135deg, #fffbeb 0%, #fef3c7 100%)', border: '1.5px solid #fcd34d' }}
      >
        <h5 className="fw-bold mb-1" style={{ color: '#92400e' }}>Connection Error</h5>
        <p className="mb-0 small" style={{ color: '#b45309', maxWidth: 380, margin: '0 auto' }}>
          Could not reach the server. Please check your internet connection or try again later.
        </p>
      </div>
    );
  }

  return (
    <div
      className="text-center py-5 px-4 rounded-4 mb-4"
      style={{ background: 'linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%)', border: '1.5px solid #7dd3fc' }}
    >
      <h5 className="fw-bold mb-1" style={{ color: '#075985' }}>Something Went Wrong</h5>
      <p className="mb-0 small" style={{ color: '#0c4a6e', maxWidth: 380, margin: '0 auto' }}>
        {error?.message || 'Failed to load sessions. Please refresh the page.'}
      </p>
    </div>
  );
}

// ─── Inline mutation error toast (for delete/create/update failures) ──────────
function MutationError({ message }) {
  if (!message) return null;
  const isPermission = message?.toLowerCase().includes('permission') || message?.toLowerCase().includes('unauthorized');
  return (
    <div
      className="px-4 py-3 rounded-3 mb-3"
      style={{
        background: isPermission ? '#fff1f2' : '#fffbeb',
        border: `1.5px solid ${isPermission ? '#fca5a5' : '#fcd34d'}`,
        color: isPermission ? '#991b1b' : '#92400e'
      }}
    >
      <span className="small fw-medium">{isPermission ? 'You do not have permission to perform this action.' : message}</span>
    </div>
  );
}

// ─── Delete Confirm Modal ─────────────────────────────────────────────────────
function DeleteConfirmModal({ show, onConfirm, onCancel, isPending }) {
  return (
    <Modal show={show} onHide={onCancel} centered size="sm">
      <Modal.Body className="text-center py-4 px-4">
        <h6 className="fw-bold mb-1 text-dark">Delete Session?</h6>
        <p className="small text-muted mb-4">This action cannot be undone. The session will be soft-deleted.</p>
        <div className="d-flex gap-2 justify-content-center">
          <Button variant="light" onClick={onCancel} className="fw-medium border px-3" disabled={isPending}>
            Cancel
          </Button>
          <Button
            onClick={onConfirm}
            disabled={isPending}
            className="fw-medium px-3"
            style={{ backgroundColor: '#dc2626', borderColor: '#dc2626', color: '#fff' }}
          >
            {isPending ? <Spinner size="sm" animation="border" /> : 'Delete'}
          </Button>
        </div>
      </Modal.Body>
    </Modal>
  );
}

// ─── Recycle Bin Modal ────────────────────────────────────────────────────────
function RecycleBinModal({ show, onHide }) {
  const { data: deletedSessions, isLoading, error } = useDeletedSessions();
  const restoreMutation = useRestoreSession();
  const [restoreError, setRestoreError] = useState('');

  const handleRestore = async (id) => {
    try {
      await restoreMutation.mutateAsync(id);
      setRestoreError('');
    } catch (err) {
      setRestoreError(err?.message || 'Failed to restore session.');
    }
  };

  const sessionsList = Array.isArray(deletedSessions) ? deletedSessions : (deletedSessions?.result || []);

  return (
    <Modal show={show} onHide={onHide} size="lg" centered>
      <Modal.Header closeButton className="border-0 pb-0">
        <Modal.Title className="fw-bold text-dark h5 d-flex align-items-center gap-2">
          <Archive size={20} className="text-secondary" />
          <span>Recycle Bin (Soft-Deleted Sessions)</span>
        </Modal.Title>
      </Modal.Header>
      <Modal.Body className="pt-3 pb-4 px-4">
        {restoreError && <MutationError message={restoreError} />}
        {isLoading ? (
          <div className="text-center py-5">
            <Spinner animation="border" variant="secondary" />
          </div>
        ) : error ? (
          <ErrorBanner error={error} />
        ) : sessionsList.length === 0 ? (
          <div className="text-center py-5 text-muted">
            <p className="mb-0">Recycle bin is empty.</p>
          </div>
        ) : (
          <div className="table-responsive">
            <Table hover className="align-middle border-0 small">
              <thead className="bg-light text-muted text-uppercase" style={{ letterSpacing: '0.5px' }}>
                <tr>
                  <th className="py-2 px-3 border-0 rounded-start">ID</th>
                  <th className="py-2 border-0">Item Name</th>
                  <th className="py-2 border-0">Start Time</th>
                  <th className="py-2 border-0">End Time</th>
                  <th className="py-2 text-center border-0 rounded-end">Action</th>
                </tr>
              </thead>
              <tbody>
                {sessionsList.map(session => (
                  <tr key={session.id} className="border-bottom">
                    <td className="fw-semibold text-dark py-2 px-3">#{session.id}</td>
                    <td className="text-dark py-2 fw-medium">{session.itemName}</td>
                    <td className="text-muted py-2">{new Date(session.startTime).toLocaleString()}</td>
                    <td className="text-muted py-2">{new Date(session.endTime).toLocaleString()}</td>
                    <td className="text-center py-2">
                      <Button
                        variant="outline-success"
                        size="sm"
                        className="d-flex align-items-center gap-1 mx-auto py-1 px-2 fw-medium"
                        onClick={() => handleRestore(session.id)}
                        disabled={restoreMutation.isPending}
                      >
                        <RotateCcw size={14} />
                        <span>Restore</span>
                      </Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </Table>
          </div>
        )}
      </Modal.Body>
    </Modal>
  );
}

// ─── Main Page ────────────────────────────────────────────────────────────────
export default function SessionManagement() {
  const [searchTerm, setSearchTerm] = useState('');
  const debouncedSearch = useDebounce(searchTerm, 300);
  const [statusFilter, setStatusFilter] = useState('All');
  const [mutationError, setMutationError] = useState('');

  const [showModal, setShowModal] = useState(false);
  const [showRecycleBin, setShowRecycleBin] = useState(false);
  const [selectedSession, setSelectedSession] = useState(null);
  const [modalMode, setModalMode] = useState('create');

  const [deleteTargetId, setDeleteTargetId] = useState(null);

  const filters = React.useMemo(() => ({
    search: debouncedSearch.length >= 2 ? debouncedSearch : '',
    status: statusFilter !== 'All' ? statusFilter : undefined
  }), [debouncedSearch, statusFilter]);

  const {
    data,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    status: queryStatus,
    error
  } = useAuctionSessions(filters);

  const deleteMutation = useDeleteSession();

  const handleOpenAdd = () => {
    setModalMode('create');
    setSelectedSession(null);
    setMutationError('');
    setShowModal(true);
  };

  const handleOpenEdit = (session) => {
    setModalMode('edit');
    setSelectedSession(session);
    setMutationError('');
    setShowModal(true);
  };

  const handleDeleteConfirm = async () => {
    try {
      await deleteMutation.mutateAsync(deleteTargetId);
      setDeleteTargetId(null);
      setMutationError('');
    } catch (err) {
      setDeleteTargetId(null);
      setMutationError(err?.message || 'Failed to delete session.');
    }
  };

  const getBadgeStyle = (status) => {
    switch (status) {
      case 'ACTIVE':    return { background: '#dcfce7', color: '#166534', border: '1px solid #86efac' };
      case 'SCHEDULED': return { background: '#dbeafe', color: '#1e40af', border: '1px solid #93c5fd' };
      case 'ENDED':     return { background: '#f3f4f6', color: '#374151', border: '1px solid #d1d5db' };
      case 'CANCELLED': return { background: '#fee2e2', color: '#991b1b', border: '1px solid #fca5a5' };
      default:          return { background: '#f9fafb', color: '#6b7280', border: '1px solid #e5e7eb' };
    }
  };

  const sessions = data?.pages.flatMap(page => page.content) || [];
  const isUnauthorized = queryStatus === 'error' && (
    error?.message?.toLowerCase().includes('permission') ||
    error?.message?.toLowerCase().includes('unauthorized') ||
    error?.code === 1001 || error?.code === 1002
  );

  return (
    <div className="bg-white rounded-3 p-4 shadow-sm border border-light text-start" style={{ minHeight: '80vh' }}>
      {/* Header */}
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h3 className="fw-bold text-dark mb-1">Auction Session Management</h3>
          <p className="text-muted small m-0">Minimalist &amp; clean session management</p>
        </div>
        {!isUnauthorized && (
          <div className="d-flex gap-2">
            <Button
              variant="outline-secondary"
              onClick={() => setShowRecycleBin(true)}
              className="d-flex align-items-center gap-2 px-3 fw-medium"
              style={{ borderRadius: '8px' }}
            >
              <Archive size={18} />
              <span>Recycle Bin</span>
            </Button>
            <Button
              variant="primary"
              onClick={handleOpenAdd}
              className="d-flex align-items-center gap-2 px-3 fw-medium"
              style={{ backgroundColor: '#004e64', borderColor: '#004e64', borderRadius: '8px' }}
            >
              <Plus size={18} />
              <span>Create New Session</span>
            </Button>
          </div>
        )}
      </div>

      {/* Mutation error (delete/update failures) */}
      <MutationError message={mutationError} />

      {/* Query error state */}
      {queryStatus === 'error' && <ErrorBanner error={error} />}

      {/* Filters — hide when fully unauthorized */}
      {!isUnauthorized && (
        <div className="d-flex gap-3 mb-4">
          <InputGroup style={{ maxWidth: '300px' }}>
            <InputGroup.Text className="bg-white border-end-0">
              <Search size={18} className="text-muted" />
            </InputGroup.Text>
            <Form.Control
              placeholder="Search by item name..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="border-start-0 ps-0"
              style={{ boxShadow: 'none' }}
            />
          </InputGroup>

          <Form.Select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            style={{ maxWidth: '200px', boxShadow: 'none' }}
          >
            <option value="All">All Status</option>
            <option value="SCHEDULED">SCHEDULED</option>
            <option value="ACTIVE">ACTIVE</option>
            <option value="ENDED">ENDED</option>
            <option value="CANCELLED">CANCELLED</option>
          </Form.Select>
        </div>
      )}

      {/* Table */}
      {!isUnauthorized && queryStatus !== 'error' && (
        <div className="table-responsive">
          <Table hover className="align-middle border-0">
            <thead className="bg-light text-muted small text-uppercase" style={{ letterSpacing: '0.5px' }}>
              <tr>
                <th className="py-3 px-3 border-0 rounded-start">ID</th>
                <th className="py-3 border-0">Item Name</th>
                <th className="py-3 border-0">Start Time</th>
                <th className="py-3 border-0">End Time</th>
                <th className="py-3 text-end border-0">Reserve Price</th>
                <th className="py-3 text-center border-0">Status</th>
                <th className="py-3 text-center border-0 rounded-end" style={{ width: '100px' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {queryStatus === 'pending' ? (
                <tr>
                  <td colSpan="7" className="text-center py-5">
                    <Spinner animation="border" variant="secondary" />
                  </td>
                </tr>
              ) : sessions.length === 0 ? (
                <tr>
                  <td colSpan="7" className="text-center py-5 text-muted">No sessions found.</td>
                </tr>
              ) : (
                sessions.map((session) => (
                  <tr key={session.id} className="border-bottom">
                    <td className="fw-semibold text-dark py-3 px-3">#{session.id}</td>
                    <td className="text-dark py-3 fw-medium">{session.itemName}</td>
                    <td className="text-muted py-3 small">{new Date(session.startTime).toLocaleString()}</td>
                    <td className="text-muted py-3 small">{new Date(session.endTime).toLocaleString()}</td>
                    <td className="text-end py-3">
                      <div className="fw-semibold text-dark">${session.reservePrice || 0}</div>
                      {session.currentHighestBid > 0 && (
                        <div className="small text-success fw-medium">Highest: ${session.currentHighestBid}</div>
                      )}
                    </td>
                    <td className="text-center py-3">
                      <span
                        className="px-2 py-1 rounded-2 small fw-semibold"
                        style={{ ...getBadgeStyle(session.status), display: 'inline-block' }}
                      >
                        {session.status}
                      </span>
                    </td>
                    <td className="text-center py-3">
                      <div className="d-flex gap-2 justify-content-center">
                        <Button
                          variant="light" size="sm"
                          className="p-1 border-0 bg-transparent"
                          style={{ color: '#6b7280' }}
                          onClick={() => handleOpenEdit(session)}
                          title="Edit session"
                        >
                          <Edit2 size={16} />
                        </Button>
                        <Button
                          variant="light" size="sm"
                          className="p-1 border-0 bg-transparent"
                          style={{ color: '#dc2626' }}
                          onClick={() => setDeleteTargetId(session.id)}
                          disabled={session.status === 'ACTIVE' || session.status === 'ENDED' || deleteMutation.isPending}
                          title="Delete session"
                        >
                          <Trash2 size={16} />
                        </Button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </Table>
        </div>
      )}

      {/* Load More */}
      {hasNextPage && (
        <div className="text-center mt-4 mb-2">
          <Button
            variant="outline-secondary"
            className="px-4 py-2 rounded-pill small fw-medium"
            onClick={() => fetchNextPage()}
            disabled={isFetchingNextPage}
          >
            {isFetchingNextPage ? <Spinner size="sm" animation="border" /> : 'Load More'}
          </Button>
        </div>
      )}

      {/* Session Form Modal */}
      {showModal && (
        <SessionFormModal
          show={showModal}
          onHide={() => setShowModal(false)}
          sessionData={selectedSession}
          mode={modalMode}
        />
      )}

      {/* Recycle Bin Modal */}
      {showRecycleBin && (
        <RecycleBinModal
          show={showRecycleBin}
          onHide={() => setShowRecycleBin(false)}
        />
      )}

      {/* Delete Confirm Modal */}
      <DeleteConfirmModal
        show={!!deleteTargetId}
        onConfirm={handleDeleteConfirm}
        onCancel={() => setDeleteTargetId(null)}
        isPending={deleteMutation.isPending}
      />
    </div>
  );
}
