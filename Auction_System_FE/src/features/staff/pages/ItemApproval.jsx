import { useContext, useState, useMemo } from 'react';
import { AppContext } from '../../../context/AppContext';
import { 
  Table, 
  Button, 
  Form, 
  InputGroup, 
  Modal, 
  Badge, 
  Alert, 
  Row, 
  Col 
} from 'react-bootstrap';
import { 
  Search, 
  Edit2, 
  Trash2, 
  Check, 
  X, 
  Plus, 
  AlertCircle 
} from 'lucide-react';

export default function ItemApproval() {
  const { 
    items, 
    categories, 
    approveItem, 
    rejectItem, 
    addItem, 
    editItem, 
    deleteItem 
  } = useContext(AppContext);

  // Search, Status, and Sort state
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('All'); // All, Pending, Active, Approved, Rejected
  const [sortBy, setSortBy] = useState('title_a_z'); // title_a_z, title_z_a, reserve_low, reserve_high

  // Modals state
  const [showAddModal, setShowAddModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [selectedItem, setSelectedItem] = useState(null);

  // Form states
  const [title, setTitle] = useState('');
  const [artist, setArtist] = useState('');
  const [category, setCategory] = useState('');
  const [reserve, setReserve] = useState('');
  const [image, setImage] = useState('');
  const [description, setDescription] = useState('');
  const [startTime, setStartTime] = useState('');
  const [endTime, setEndTime] = useState('');
  const [status, setStatus] = useState('Pending');
  const [submittedBy, setSubmittedBy] = useState('');

  // Count pending items
  const pendingCount = useMemo(() => {
    return items.filter(item => item.status === 'Pending').length;
  }, [items]);

  // Open modals
  const handleOpenAdd = () => {
    setTitle('');
    setArtist('');
    setCategory(categories[0]?.name || 'Paintings');
    setReserve('');
    setImage('https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=500&auto=format&fit=crop&q=60'); // default mock art img
    setDescription('');
    // Defaults: start tomorrow, end in 10 days
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const tenDaysLater = new Date();
    tenDaysLater.setDate(tenDaysLater.getDate() + 10);
    
    setStartTime(tomorrow.toISOString().substring(0, 16));
    setEndTime(tenDaysLater.toISOString().substring(0, 16));
    setStatus('Pending');
    setSubmittedBy(`seller_${Math.floor(Math.random() * 100)}`);
    setShowAddModal(true);
  };

  const handleOpenEdit = (item) => {
    setSelectedItem(item);
    setTitle(item.title);
    setArtist(item.artist);
    setCategory(item.category);
    setReserve(item.reserve);
    setImage(item.image);
    setDescription(item.description);
    setStartTime(new Date(item.startTime).toISOString().substring(0, 16));
    setEndTime(new Date(item.endTime).toISOString().substring(0, 16));
    setStatus(item.status);
    setSubmittedBy(item.submittedBy);
    setShowEditModal(true);
  };

  // Form handlers
  const handleAddSubmit = (e) => {
    e.preventDefault();
    if (!title.trim() || !artist.trim() || !reserve) return;

    addItem({
      title,
      artist,
      category,
      reserve: parseFloat(reserve),
      image,
      description,
      startTime: new Date(startTime).toISOString(),
      endTime: new Date(endTime).toISOString(),
      status,
      submittedBy
    });
    setShowAddModal(false);
  };

  const handleEditSubmit = (e) => {
    e.preventDefault();
    if (!selectedItem || !title.trim() || !artist.trim() || !reserve) return;

    editItem(selectedItem.id, {
      title,
      artist,
      category,
      reserve: parseFloat(reserve),
      image,
      description,
      startTime: new Date(startTime).toISOString(),
      endTime: new Date(endTime).toISOString(),
      status,
      submittedBy
    });
    setShowEditModal(false);
  };

  const handleDelete = (id) => {
    if (window.confirm("Are you sure you want to delete this auction item?")) {
      deleteItem(id);
    }
  };

  // Process data (Filter & Sort)
  const processedItems = useMemo(() => {
    let result = [...items];

    // Status filter
    if (statusFilter !== 'All') {
      result = result.filter(item => item.status === statusFilter);
    }

    // Search query filter
    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase();
      result = result.filter(item => 
        item.title.toLowerCase().includes(q) || 
        item.artist.toLowerCase().includes(q) ||
        item.category.toLowerCase().includes(q)
      );
    }

    // Sort
    if (sortBy === 'title_a_z') {
      result.sort((a, b) => a.title.localeCompare(b.title));
    } else if (sortBy === 'title_z_a') {
      result.sort((a, b) => b.title.localeCompare(a.title));
    } else if (sortBy === 'reserve_low') {
      result.sort((a, b) => a.reserve - b.reserve);
    } else if (sortBy === 'reserve_high') {
      result.sort((a, b) => b.reserve - a.reserve);
    }

    return result;
  }, [items, searchQuery, statusFilter, sortBy]);

  // Badge colors mapping
  const getBadgeStyle = (status) => {
    switch (status) {
      case 'Active':
        return 'bg-success text-white';
      case 'Approved':
        return 'bg-info text-white';
      case 'Pending':
        return 'bg-warning text-dark';
      case 'Rejected':
        return 'bg-danger text-white';
      default:
        return 'bg-secondary text-white';
    }
  };

  return (
    <div className="bg-white rounded p-4 shadow-sm border text-start">
      {/* Title Header */}
      <div className="d-flex justify-content-between align-items-center mb-3">
        <div>
          <h2 className="fw-bold text-dark mb-1">Auction Item Management</h2>
          <p className="text-muted small m-0">Manage items, approve submissions, search and sort</p>
        </div>
        <Button 
          variant="dark" 
          onClick={handleOpenAdd}
          className="d-flex align-items-center gap-1 text-uppercase fw-semibold"
          style={{ backgroundColor: '#004e64', borderColor: '#004e64', fontSize: '0.85rem' }}
        >
          <Plus size={16} />
          <span>Add Item</span>
        </Button>
      </div>

      {/* Filter Bar (Search, Status, Sorting) */}
      <Row className="g-3 mb-4">
        <Col md={5} sm={12}>
          <InputGroup size="sm">
            <InputGroup.Text className="bg-light text-muted">
              <Search size={16} />
            </InputGroup.Text>
            <Form.Control
              placeholder="Search by title or artist..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </InputGroup>
        </Col>

        <Col md={3} sm={6}>
          <Form.Select 
            size="sm" 
            value={statusFilter} 
            onChange={(e) => setStatusFilter(e.target.value)}
          >
            <option value="All">All Status</option>
            <option value="Pending">Pending</option>
            <option value="Approved">Approved</option>
            <option value="Active">Active</option>
            <option value="Rejected">Rejected</option>
          </Form.Select>
        </Col>

        <Col md={4} sm={6}>
          <Form.Select 
            size="sm" 
            value={sortBy} 
            onChange={(e) => setSortBy(e.target.value)}
          >
            <option value="title_a_z">Title A-Z</option>
            <option value="title_z_a">Title Z-A</option>
            <option value="reserve_low">Reserve Price (Low to High)</option>
            <option value="reserve_high">Reserve Price (High to Low)</option>
          </Form.Select>
        </Col>
      </Row>

      {/* Pending Items Banner */}
      {pendingCount > 0 && (
        <Alert 
          variant="warning" 
          className="d-flex align-items-center gap-2 py-3 border-0 border-start border-4 border-warning mb-4 rounded-0 shadow-xs"
          style={{ backgroundColor: '#fffbf2' }}
        >
          <AlertCircle size={20} className="text-warning flex-shrink-0" />
          <div className="text-dark small">
            <span className="fw-bold">{pendingCount} items</span> awaiting approval from sellers
          </div>
        </Alert>
      )}

      {/* Items Table */}
      {processedItems.length > 0 ? (
        <Table responsive hover className="align-middle border rounded shadow-xs" style={{ fontSize: '0.92rem' }}>
          <thead>
            <tr className="text-white bg-dark-teal" style={{ backgroundColor: '#004e64' }}>
              <th className="py-3 px-3">Title</th>
              <th className="py-3">Artist</th>
              <th className="py-3">Category</th>
              <th className="py-3 text-end">Reserve</th>
              <th className="py-3 text-end">Current Bid</th>
              <th className="py-3 text-center">Status</th>
              <th className="py-3 text-center">Submitted By</th>
              <th className="py-3 text-center" style={{ width: '150px' }}>Actions</th>
            </tr>
          </thead>
          <tbody>
            {processedItems.map((item) => (
              <tr key={item.id}>
                <td className="fw-bold text-dark py-3 px-3">{item.title}</td>
                <td className="text-muted py-3">{item.artist}</td>
                <td className="text-muted py-3">{item.category}</td>
                <td className="text-end fw-semibold py-3">${item.reserve}</td>
                <td className="text-end py-3 text-teal fw-bold">
                  {item.currentBid ? `$${item.currentBid}` : '-'}
                </td>
                <td className="text-center py-3">
                  <Badge 
                    className={`px-3 py-2 text-uppercase rounded-pill ${getBadgeStyle(item.status)}`}
                    style={{ fontSize: '0.75rem', fontWeight: '500' }}
                  >
                    {item.status}
                  </Badge>
                </td>
                <td className="text-center text-muted font-monospace py-3">{item.submittedBy}</td>
                <td className="text-center py-3">
                  <div className="d-flex gap-2 justify-content-center">
                    {item.status === 'Pending' && (
                      <>
                        <Button 
                          variant="link" 
                          size="sm" 
                          className="text-success p-0 hover-opacity"
                          title="Approve"
                          onClick={() => approveItem(item.id)}
                        >
                          <Check size={18} />
                        </Button>
                        <Button 
                          variant="link" 
                          size="sm" 
                          className="text-danger p-0 hover-opacity"
                          title="Reject"
                          onClick={() => rejectItem(item.id)}
                        >
                          <X size={18} />
                        </Button>
                      </>
                    )}
                    <Button 
                      variant="link" 
                      size="sm" 
                      className="text-info p-0 hover-opacity"
                      title="Edit"
                      onClick={() => handleOpenEdit(item)}
                    >
                      <Edit2 size={16} />
                    </Button>
                    <Button 
                      variant="link" 
                      size="sm" 
                      className="text-danger p-0 hover-opacity"
                      title="Delete"
                      onClick={() => handleDelete(item.id)}
                    >
                      <Trash2 size={16} />
                    </Button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </Table>
      ) : (
        <Alert variant="warning" className="text-center py-4">
          No auction items found.
        </Alert>
      )}

      {/* Add Item Modal */}
      <Modal show={showAddModal} onHide={() => setShowAddModal(false)} size="lg" centered>
        <Modal.Header closeButton className="bg-light">
          <Modal.Title className="fw-bold fs-5">Add New Auction Item</Modal.Title>
        </Modal.Header>
        <Form onSubmit={handleAddSubmit}>
          <Modal.Body className="p-4">
            <Row className="g-3">
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label className="small fw-semibold">Item Title</Form.Label>
                  <Form.Control
                    type="text"
                    placeholder="e.g. Starry Night"
                    value={title}
                    onChange={(e) => setTitle(e.target.value)}
                    required
                  />
                </Form.Group>
              </Col>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label className="small fw-semibold">Artist / Creator</Form.Label>
                  <Form.Control
                    type="text"
                    placeholder="e.g. Vincent Van Gogh"
                    value={artist}
                    onChange={(e) => setArtist(e.target.value)}
                    required
                  />
                </Form.Group>
              </Col>
            </Row>

            <Row className="g-3">
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label className="small fw-semibold">Category</Form.Label>
                  <Form.Select value={category} onChange={(e) => setCategory(e.target.value)}>
                    {categories.map(c => (
                      <option key={c.id} value={c.name}>{c.name}</option>
                    ))}
                  </Form.Select>
                </Form.Group>
              </Col>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label className="small fw-semibold">Reserve Price (USD)</Form.Label>
                  <Form.Control
                    type="number"
                    placeholder="e.g. 500"
                    value={reserve}
                    onChange={(e) => setReserve(e.target.value)}
                    required
                  />
                </Form.Group>
              </Col>
            </Row>

            <Form.Group className="mb-3">
              <Form.Label className="small fw-semibold">Image URL</Form.Label>
              <Form.Control
                type="url"
                value={image}
                onChange={(e) => setImage(e.target.value)}
                required
              />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label className="small fw-semibold">Description</Form.Label>
              <Form.Control
                as="textarea"
                rows={3}
                placeholder="Details about the artwork..."
                value={description}
                onChange={(e) => setDescription(e.target.value)}
              />
            </Form.Group>

            <Row className="g-3">
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label className="small fw-semibold">Start Time</Form.Label>
                  <Form.Control
                    type="datetime-local"
                    value={startTime}
                    onChange={(e) => setStartTime(e.target.value)}
                    required
                  />
                </Form.Group>
              </Col>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label className="small fw-semibold">End Time</Form.Label>
                  <Form.Control
                    type="datetime-local"
                    value={endTime}
                    onChange={(e) => setEndTime(e.target.value)}
                    required
                  />
                </Form.Group>
              </Col>
            </Row>

            <Row className="g-3">
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label className="small fw-semibold">Status</Form.Label>
                  <Form.Select value={status} onChange={(e) => setStatus(e.target.value)}>
                    <option value="Pending">Pending</option>
                    <option value="Approved">Approved</option>
                    <option value="Active">Active</option>
                    <option value="Rejected">Rejected</option>
                  </Form.Select>
                </Form.Group>
              </Col>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label className="small fw-semibold">Submitted By (Seller Name)</Form.Label>
                  <Form.Control
                    type="text"
                    value={submittedBy}
                    onChange={(e) => setSubmittedBy(e.target.value)}
                    required
                  />
                </Form.Group>
              </Col>
            </Row>
          </Modal.Body>
          <Modal.Footer className="bg-light">
            <Button variant="secondary" size="sm" onClick={() => setShowAddModal(false)}>
              Cancel
            </Button>
            <Button 
              variant="dark" 
              size="sm" 
              type="submit" 
              style={{ backgroundColor: '#004e64', borderColor: '#004e64' }}
            >
              Submit Item
            </Button>
          </Modal.Footer>
        </Form>
      </Modal>

      {/* Edit Item Modal */}
      <Modal show={showEditModal} onHide={() => setShowEditModal(false)} size="lg" centered>
        <Modal.Header closeButton className="bg-light">
          <Modal.Title className="fw-bold fs-5">Edit Auction Item</Modal.Title>
        </Modal.Header>
        <Form onSubmit={handleEditSubmit}>
          <Modal.Body className="p-4">
            <Row className="g-3">
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label className="small fw-semibold">Item Title</Form.Label>
                  <Form.Control
                    type="text"
                    value={title}
                    onChange={(e) => setTitle(e.target.value)}
                    required
                  />
                </Form.Group>
              </Col>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label className="small fw-semibold">Artist / Creator</Form.Label>
                  <Form.Control
                    type="text"
                    value={artist}
                    onChange={(e) => setArtist(e.target.value)}
                    required
                  />
                </Form.Group>
              </Col>
            </Row>

            <Row className="g-3">
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label className="small fw-semibold">Category</Form.Label>
                  <Form.Select value={category} onChange={(e) => setCategory(e.target.value)}>
                    {categories.map(c => (
                      <option key={c.id} value={c.name}>{c.name}</option>
                    ))}
                  </Form.Select>
                </Form.Group>
              </Col>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label className="small fw-semibold">Reserve Price (USD)</Form.Label>
                  <Form.Control
                    type="number"
                    value={reserve}
                    onChange={(e) => setReserve(e.target.value)}
                    required
                  />
                </Form.Group>
              </Col>
            </Row>

            <Form.Group className="mb-3">
              <Form.Label className="small fw-semibold">Image URL</Form.Label>
              <Form.Control
                type="url"
                value={image}
                onChange={(e) => setImage(e.target.value)}
                required
              />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label className="small fw-semibold">Description</Form.Label>
              <Form.Control
                as="textarea"
                rows={3}
                value={description}
                onChange={(e) => setDescription(e.target.value)}
              />
            </Form.Group>

            <Row className="g-3">
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label className="small fw-semibold">Start Time</Form.Label>
                  <Form.Control
                    type="datetime-local"
                    value={startTime}
                    onChange={(e) => setStartTime(e.target.value)}
                    required
                  />
                </Form.Group>
              </Col>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label className="small fw-semibold">End Time</Form.Label>
                  <Form.Control
                    type="datetime-local"
                    value={endTime}
                    onChange={(e) => setEndTime(e.target.value)}
                    required
                  />
                </Form.Group>
              </Col>
            </Row>

            <Row className="g-3">
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label className="small fw-semibold">Status</Form.Label>
                  <Form.Select value={status} onChange={(e) => setStatus(e.target.value)}>
                    <option value="Pending">Pending</option>
                    <option value="Approved">Approved</option>
                    <option value="Active">Active</option>
                    <option value="Rejected">Rejected</option>
                  </Form.Select>
                </Form.Group>
              </Col>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label className="small fw-semibold">Submitted By</Form.Label>
                  <Form.Control
                    type="text"
                    value={submittedBy}
                    onChange={(e) => setSubmittedBy(e.target.value)}
                    required
                  />
                </Form.Group>
              </Col>
            </Row>
          </Modal.Body>
          <Modal.Footer className="bg-light">
            <Button variant="secondary" size="sm" onClick={() => setShowEditModal(false)}>
              Cancel
            </Button>
            <Button 
              variant="dark" 
              size="sm" 
              type="submit" 
              style={{ backgroundColor: '#004e64', borderColor: '#004e64' }}
            >
              Save Changes
            </Button>
          </Modal.Footer>
        </Form>
      </Modal>
    </div>
  );
}
