import { useState, useMemo, useEffect, useCallback } from 'react';
import { Container, Row, Col, Spinner, Alert } from 'react-bootstrap';
import FilterBar from '../components/FilterBar';
import ProductCard from '../components/ProductCard';
import { itemApi } from '../../../api/itemApi';
import { sessionApi } from '../../../api/sessionApi';

/**
 * CatalogPage — Displays auction sessions (not raw items).
 *
 * Data flow:
 *   sessionApi.getSessions() → AuctionSessionListResponse[]
 *   Each session has: { id, itemId, itemName, itemDescription,
 *                       reservePrice, currentHighestBid,
 *                       status (SCHEDULED|ACTIVE|ENDED|CANCELLED),
 *                       startTime, endTime }
 *
 * Tab mapping:
 *   "all"      → show SCHEDULED + ACTIVE sessions
 *   "upcoming" → SCHEDULED only
 *   "current"  → ACTIVE only
 */
export default function CatalogPage() {
  const [sessions, setSessions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [searchQuery, setSearchQuery] = useState('');
  const [activeTab, setActiveTab] = useState('all');
  const [sortBy, setSortBy] = useState('default');
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 8;

  // Fetch all live+upcoming sessions on mount
  const loadSessions = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      // Fetch a broad page of sessions — backend supports cursor pagination
      const res = await sessionApi.getSessions({ size: 100 });
      // Backend returns CursorPageResponse<AuctionSessionListResponse>
      const list = res?.content ?? res?.data ?? [];
      // Only show publicly meaningful sessions: SCHEDULED and ACTIVE
      const publicSessions = list.filter(s =>
        s.status === 'SCHEDULED' || s.status === 'ACTIVE' || s.status === 'APPROVED'
      );
      setSessions(publicSessions);
    } catch (err) {
      console.error('Failed to load sessions:', err);
      setError('Failed to load auction sessions. Please try again later.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadSessions();
  }, [loadSessions]);

  // Filter & sort sessions
  const filteredSessions = useMemo(() => {
    let result = [...sessions];

    // Tab filter maps to SessionStatus
    if (activeTab === 'upcoming') {
      result = result.filter(s => s.status === 'SCHEDULED');
    } else if (activeTab === 'current') {
      result = result.filter(s => s.status === 'ACTIVE');
    }
    // 'all' shows both SCHEDULED and ACTIVE

    // Search by item name / description
    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase();
      result = result.filter(s =>
        (s.itemName || '').toLowerCase().includes(q) ||
        (s.itemDescription || '').toLowerCase().includes(q)
      );
    }

    // Sorting
    if (sortBy === 'newest') {
      result.sort((a, b) => new Date(b.startTime) - new Date(a.startTime));
    } else if (sortBy === 'price_low') {
      result.sort((a, b) =>
        (parseFloat(a.currentHighestBid ?? a.reservePrice) || 0) -
        (parseFloat(b.currentHighestBid ?? b.reservePrice) || 0)
      );
    } else if (sortBy === 'price_high') {
      result.sort((a, b) =>
        (parseFloat(b.currentHighestBid ?? b.reservePrice) || 0) -
        (parseFloat(a.currentHighestBid ?? a.reservePrice) || 0)
      );
    } else if (sortBy === 'title_a_z') {
      result.sort((a, b) => (a.itemName || '').localeCompare(b.itemName || ''));
    }

    return result;
  }, [sessions, searchQuery, activeTab, sortBy]);

  // Pagination
  const totalPages = Math.ceil(filteredSessions.length / itemsPerPage);
  const paginatedSessions = useMemo(() => {
    const start = (currentPage - 1) * itemsPerPage;
    return filteredSessions.slice(start, start + itemsPerPage);
  }, [filteredSessions, currentPage]);

  const handleTabChange = (tab) => {
    setActiveTab(tab);
    setCurrentPage(1);
  };

  return (
    <div>
      {/* Hero Banner */}
      <div
        className="w-100 py-5 text-white position-relative overflow-hidden mb-4 d-flex align-items-center justify-content-center"
        style={{ background: 'linear-gradient(135deg, #003049 0%, #005f73 100%)', height: '240px' }}
      >
        <div
          className="position-absolute top-0 start-0 w-100 h-100 opacity-10"
          style={{
            backgroundImage: 'radial-gradient(circle, #ffffff 1px, transparent 1px)',
            backgroundSize: '16px 16px'
          }}
        />
        <Container className="text-center position-relative z-1">
          <h1 className="display-3 fw-bold text-uppercase m-0 text-white" style={{ letterSpacing: '4px' }}>
            Auctions
          </h1>
          <p className="text-light opacity-75 small text-uppercase mt-2" style={{ letterSpacing: '2px' }}>
            Bid on authentic fine art and historical masterpieces
          </p>
        </Container>
      </div>

      <Container className="pb-5">
        <FilterBar
          searchQuery={searchQuery}
          setSearchQuery={setSearchQuery}
          activeTab={activeTab}
          setActiveTab={handleTabChange}
          sortBy={sortBy}
          setSortBy={setSortBy}
        />

        {/* Loading State */}
        {loading && (
          <div className="text-center py-5">
            <Spinner animation="border" style={{ color: '#005f73', width: '3rem', height: '3rem' }} />
            <p className="text-muted mt-3">Loading auction sessions...</p>
          </div>
        )}

        {/* Error State */}
        {!loading && error && (
          <Alert variant="danger" className="rounded-4">
            {error}
          </Alert>
        )}

        {/* Session Grid */}
        {!loading && !error && paginatedSessions.length > 0 && (
          <Row className="g-4">
            {paginatedSessions.map(session => (
              <Col key={session.id} xl={3} lg={4} md={6} sm={12}>
                <ProductCard session={session} />
              </Col>
            ))}
          </Row>
        )}

        {/* Empty State */}
        {!loading && !error && paginatedSessions.length === 0 && (
          <div className="text-center py-5 border rounded-4 bg-white shadow-sm">
            <h4 className="text-muted">No auction sessions found.</h4>
            <p className="text-muted small">
              {activeTab === 'current'
                ? 'No active auction sessions at the moment.'
                : activeTab === 'upcoming'
                  ? 'No upcoming auction sessions scheduled yet.'
                  : 'Try adjusting your filters or search keywords.'}
            </p>
          </div>
        )}

        {/* Pagination */}
        {totalPages > 1 && !loading && (
          <div className="d-flex justify-content-center mt-5 gap-1">
            <button
              className="btn btn-outline-secondary btn-sm rounded-3 px-3"
              disabled={currentPage === 1}
              onClick={() => setCurrentPage(p => Math.max(1, p - 1))}
            >
              ← Prev
            </button>
            {[...Array(totalPages)].map((_, i) => (
              <button
                key={i + 1}
                className={`btn btn-sm rounded-3 px-3 ${currentPage === i + 1 ? 'btn-dark' : 'btn-outline-secondary'}`}
                onClick={() => setCurrentPage(i + 1)}
              >
                {i + 1}
              </button>
            ))}
            <button
              className="btn btn-outline-secondary btn-sm rounded-3 px-3"
              disabled={currentPage === totalPages}
              onClick={() => setCurrentPage(p => Math.min(totalPages, p + 1))}
            >
              Next →
            </button>
          </div>
        )}
      </Container>
    </div>
  );
}
