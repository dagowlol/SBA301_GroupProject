import { Nav, Form, Row, Col, InputGroup } from 'react-bootstrap';
import { Search } from 'lucide-react';

export default function FilterBar({ 
  searchQuery, 
  setSearchQuery, 
  activeTab, 
  setActiveTab, 
  sortBy, 
  setSortBy 
}) {
  return (
    <div className="mb-4 bg-white p-3 rounded shadow-sm border">
      <Row className="align-items-center g-3">
        {/* Left Side: Tabs */}
        <Col lg={6} md={12} className="d-flex justify-content-start">
          <Nav 
            variant="underline" 
            activeKey={activeTab} 
            onSelect={(selectedKey) => setActiveTab(selectedKey)}
            className="w-100 border-0 flex-nowrap overflow-auto scrollbar-hidden"
          >
            <Nav.Item>
              <Nav.Link 
                eventKey="all" 
                className={`text-uppercase font-semibold px-3 py-2 ${activeTab === 'all' ? 'text-dark fw-bold border-dark border-bottom-2' : 'text-muted'}`}
                style={{ fontSize: '0.85rem' }}
              >
                All Auction
              </Nav.Link>
            </Nav.Item>
            <Nav.Item>
              <Nav.Link 
                eventKey="upcoming" 
                className={`text-uppercase font-semibold px-3 py-2 ${activeTab === 'upcoming' ? 'text-dark fw-bold border-dark border-bottom-2' : 'text-muted'}`}
                style={{ fontSize: '0.85rem' }}
              >
                Upcoming Auction
              </Nav.Link>
            </Nav.Item>
            <Nav.Item>
              <Nav.Link 
                eventKey="current" 
                className={`text-uppercase font-semibold px-3 py-2 ${activeTab === 'current' ? 'text-dark fw-bold border-dark border-bottom-2' : 'text-muted'}`}
                style={{ fontSize: '0.85rem' }}
              >
                Current Auction
              </Nav.Link>
            </Nav.Item>
          </Nav>
        </Col>

        {/* Right Side: Search & Sort */}
        <Col lg={6} md={12}>
          <Row className="g-2 justify-content-end">
            <Col sm={7} xs={12}>
              <InputGroup size="sm" className="shadow-xs">
                <InputGroup.Text className="bg-transparent border-end-0 text-muted">
                  <Search size={16} />
                </InputGroup.Text>
                <Form.Control
                  placeholder="Tìm kiếm theo tên sản phẩm..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="border-start-0 py-2"
                  style={{ fontSize: '0.88rem' }}
                />
              </InputGroup>
            </Col>
            
            <Col sm={5} xs={12}>
              <div className="d-flex align-items-center gap-2">
                <Form.Label className="m-0 text-muted text-nowrap small" style={{ fontSize: '0.75rem' }}>
                  Sort:
                </Form.Label>
                <Form.Select 
                  size="sm"
                  value={sortBy} 
                  onChange={(e) => setSortBy(e.target.value)}
                  className="py-2"
                  style={{ fontSize: '0.88rem' }}
                >
                  <option value="default">Mặc định</option>
                  <option value="newest">Mới nhất (startTime)</option>
                  <option value="price_low">Giá tăng dần</option>
                  <option value="price_high">Giá giảm dần</option>
                  <option value="title_a_z">Tên A-Z</option>
                </Form.Select>
              </div>
            </Col>
          </Row>
        </Col>
      </Row>
    </div>
  );
}
