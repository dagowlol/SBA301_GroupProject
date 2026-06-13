import { useContext, useState, useMemo } from 'react';
import { AppContext } from '../../../context/AppContext';
import { Container, Row, Col, Pagination } from 'react-bootstrap';
import FilterBar from '../components/FilterBar';
import ProductCard from '../components/ProductCard';

export default function CatalogPage() {
  const { items } = useContext(AppContext);
  // Local state for filtering/sorting
  const [searchQuery, setSearchQuery] = useState('');
  const [activeTab, setActiveTab] = useState('all'); // all, upcoming, current, pending
  const [sortBy, setSortBy] = useState('default');

  // Pagination mockup
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 8;

  // Filter & Sort Logic
  const filteredAndSortedItems = useMemo(() => {
    if (!Array.isArray(items)) return [];
    let result = [...items];

    // Filter by tab
    if (activeTab === 'upcoming') {
      result = result.filter(item => item.status === 'Approved');
    } else if (activeTab === 'current') {
      result = result.filter(item => item.status === 'Active');
    }
    // 'all' tab shows everything

    // Filter by search
    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase();
      result = result.filter(item =>
        (item.title || '').toLowerCase().includes(q) ||
        (item.artist || '').toLowerCase().includes(q) ||
        (item.category || '').toLowerCase().includes(q)
      );
    }

    // Sorting
    if (sortBy === 'newest') {
      result.sort((a, b) => new Date(b.startTime) - new Date(a.startTime));
    } else if (sortBy === 'most_viewed') {
      result.sort((a, b) => (b.views || 0) - (a.views || 0));
    } else if (sortBy === 'title_a_z') {
      result.sort((a, b) => (a.title || '').localeCompare(b.title || ''));
    }

    return result;
  }, [items, searchQuery, activeTab, sortBy]);

  // Paginated subset
  const paginatedItems = useMemo(() => {
    const startIndex = (currentPage - 1) * itemsPerPage;
    return filteredAndSortedItems.slice(startIndex, startIndex + itemsPerPage);
  }, [filteredAndSortedItems, currentPage]);

  const totalPages = Math.ceil(filteredAndSortedItems.length / itemsPerPage);

  return (
    <div>
      {/* Hero Banner Section */}
      <div
        className="w-100 py-5 text-white position-relative overflow-hidden mb-4 d-flex align-items-center justify-content-center"
        style={{
          background: 'linear-gradient(135deg, #003049 0%, #005f73 100%)',
          height: '240px'
        }}
      >
        {/* Dot pattern overlay */}
        <div
          className="position-absolute top-0 start-0 w-100 h-100 opacity-10"
          style={{
            backgroundImage: 'radial-gradient(circle, #ffffff 1px, transparent 1px)',
            backgroundSize: '16px 16px'
          }}
        />
        <Container className="text-center position-relative z-1">
          <h1 className="display-3 fw-bold tracking-widest text-uppercase m-0 text-white" style={{ letterSpacing: '4px' }}>
            Auctions
          </h1>
          <p className="text-light opacity-75 small text-uppercase tracking-wider mt-2">
            Bid on authentic fine art and historical masterpieces
          </p>
        </Container>
      </div>

      {/* Main catalog layout */}
      <Container className="pb-5">
        <FilterBar
          searchQuery={searchQuery}
          setSearchQuery={setSearchQuery}
          activeTab={activeTab}
          setActiveTab={(tab) => {
            setActiveTab(tab);
            setCurrentPage(1);
          }}
          sortBy={sortBy}
          setSortBy={setSortBy}
        />

        {/* Product Grid */}
        {paginatedItems.length > 0 ? (
          <Row className="g-4">
            {paginatedItems.map(item => (
              <Col key={item.id} xl={3} lg={4} md={6} sm={12}>
                <ProductCard item={item} />
              </Col>
            ))}
          </Row>
        ) : (
          <div className="text-center py-5 border rounded bg-white shadow-xs">
            <h4 className="text-muted">No items found matching your filters.</h4>
            <p className="text-muted small">Try adjusting your keywords or switching tabs.</p>
          </div>
        )}

        {/* Pagination Controls */}
        {totalPages > 1 && (
          <div className="d-flex justify-content-center mt-5">
            <Pagination>
              <Pagination.Prev
                disabled={currentPage === 1}
                onClick={() => setCurrentPage(prev => Math.max(1, prev - 1))}
              />
              {[...Array(totalPages)].map((_, i) => (
                <Pagination.Item
                  key={i + 1}
                  active={currentPage === i + 1}
                  onClick={() => setCurrentPage(i + 1)}
                >
                  {i + 1}
                </Pagination.Item>
              ))}
              <Pagination.Next
                disabled={currentPage === totalPages}
                onClick={() => setCurrentPage(prev => Math.min(totalPages, prev + 1))}
              />
            </Pagination>
          </div>
        )}
      </Container>
    </div>
  );
}
