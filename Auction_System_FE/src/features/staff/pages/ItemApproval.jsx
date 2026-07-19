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
  Col,
  Spinner,
  Toast,
  ToastContainer
} from 'react-bootstrap';
import {
  Search,
  Edit2,
  Trash2,
  Check,
  X,
  Plus,
  AlertCircle
  ,ImagePlus,
  Ban
} from 'lucide-react';

const formatVND = (value) => `${new Intl.NumberFormat('en-US', {
  maximumFractionDigits: 0,
}).format(Number(value) || 0)} VND`;

export default function ItemApproval() {
  const {
    items,
    categories,
    loading,
    loadError,
    refetch,
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
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [showRejectModal, setShowRejectModal] = useState(false);
  const [itemToDelete, setItemToDelete] = useState(null);
  const [itemToReject, setItemToReject] = useState(null);
  const [rejectionReason, setRejectionReason] = useState('');
  const [rejectError, setRejectError] = useState('');
  const [isRejecting, setIsRejecting] = useState(false);
  const [selectedItem, setSelectedItem] = useState(null);

  // Form states matching ItemRequest and ItemResponse
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [categoryId, setCategoryId] = useState('');
  const [reserve, setReserve] = useState('');
  const [startingPrice, setStartingPrice] = useState('');
  const [condition, setCondition] = useState('NEW');
  const [status, setStatus] = useState('Pending');
  const [imageFile, setImageFile] = useState(null);

  // Error state for handling Spring Boot constraint exceptions
  const [error, setError] = useState('');

  // Toast state
  const [showToast, setShowToast] = useState(false);
  const [toastMessage, setToastMessage] = useState('');
  const [toastVariant, setToastVariant] = useState('success');

  const triggerToast = (message, variant = 'success') => {
    setToastMessage(message);
    setToastVariant(variant);
    setShowToast(true);
  };

  // Count pending items
  const pendingCount = useMemo(() => {
    return items.filter(item => item.status === 'Pending').length;
  }, [items]);

  // Open modals
  const handleOpenAdd = () => {
    setTitle('');
    setDescription('');
    setCategoryId(categories[0]?.id || '');
    setReserve('');
    setStartingPrice('');
    setCondition('NEW');
    setStatus('Pending');
    setImageFile(null);
    setError('');
    setShowAddModal(true);
  };

  const handleOpenEdit = (item) => {
    setSelectedItem(item);
    setTitle(item.title || item.name || '');
    setDescription(item.description);

    // Attempt to match categoryId
    const matchedCategory = categories.find(c => c.name.toLowerCase() === item.category.toLowerCase());
    setCategoryId(item.categoryId || matchedCategory?.id || categories[0]?.id || '');

    setReserve(item.reserve || item.reservePrice || '');
    setStartingPrice(item.startingPrice || '');
    setCondition(item.condition || 'NEW');
    setStatus(item.status);
    setImageFile(null);
    setError('');
    setShowEditModal(true);
  };

  // Action handlers
  const handleApprove = async (id) => {
    setError('');
    try {
      await approveItem(id);
    } catch (err) {
      setError(err.message || 'Failed to approve item on backend.');
    }
  };

  const handleOpenReject = (item) => {
    setItemToReject(item);
    setRejectionReason('');
    setRejectError('');
    setShowRejectModal(true);
  };

  const handleCloseReject = () => {
    if (isRejecting) return;
    setShowRejectModal(false);
    setItemToReject(null);
    setRejectionReason('');
    setRejectError('');
  };

  const handleRejectSubmit = async (e) => {
    e.preventDefault();
    const reason = rejectionReason.trim();
    if (!reason) {
      setRejectError('Please provide a reason for rejecting this item.');
      return;
    }

    setRejectError('');
    setIsRejecting(true);
    try {
      await rejectItem(itemToReject.id, reason);
      triggerToast(`Item "${itemToReject.title}" has been rejected.`);
      setShowRejectModal(false);
      setItemToReject(null);
      setRejectionReason('');
    } catch (err) {
      setRejectError(err.message || 'Failed to reject item. Please try again.');
    } finally {
      setIsRejecting(false);
    }
  };

  // Form submit handlers (Async try-catch)
  const handleAddSubmit = async (e) => {
    e.preventDefault();
    if (!title.trim() || !categoryId || !startingPrice) return;
    setError('');

    try {
      await addItem({
        itemName: title,
        description,
        categoryId: parseInt(categoryId),
        startingPrice: parseFloat(startingPrice),
        reservePrice: reserve ? parseFloat(reserve) : null,
        condition,
        imageFile
      });
      setShowAddModal(false);
      triggerToast(`Item "${title}" has been submitted successfully.`);
    } catch (err) {
      setError(err.message || 'Failed to submit item to backend.');
    }
  };

  const handleEditSubmit = async (e) => {
    e.preventDefault();
    if (!selectedItem || !title.trim() || !categoryId || !startingPrice) return;
    setError('');

    try {
      await editItem(selectedItem.id, {
        name: title,
        description,
        categoryId: parseInt(categoryId),
        startingPrice: parseFloat(startingPrice),
        reservePrice: reserve ? parseFloat(reserve) : null,
        condition,
        imageFile
      });
      setShowEditModal(false);
      triggerToast(`Item "${title}" has been updated successfully.`);
    } catch (err) {
      setError(err.message || 'Failed to update item on backend.');
    }
  };

  const handleDelete = (item) => {
    setItemToDelete(item);
    setShowDeleteConfirm(true);
  };

  const confirmDelete = async () => {
    if (!itemToDelete) return;
    setError('');
    try {
      await deleteItem(itemToDelete.id);
      triggerToast(`Item "${itemToDelete.title}" has been deleted successfully.`);
    } catch (err) {
      setError(err.message || 'Failed to delete item from backend.');
    } finally {
      setShowDeleteConfirm(false);
      setItemToDelete(null);
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
      case 'Sold':
        return 'bg-primary text-white';
      case 'Paid':
        return 'bg-success text-white';
      case 'Shipping':
        return 'bg-info text-dark';
      case 'Delivered':
        return 'bg-success text-white';
      default:
        return 'bg-secondary text-white';
    }
  };

  return (
    <div className="bg-white rounded p-4 shadow-sm border text-start">
      {loading ? (
        <div className="d-flex flex-column align-items-center justify-content-center py-5">
          <Spinner animation="border" variant="primary" />
          <span className="mt-3 text-muted">Loading auction items...</span>
        </div>
      ) : (
        <>
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

          {(error || loadError) && (
            <Alert variant="danger" className="py-2.5 small mb-3 d-flex justify-content-between align-items-center">
              <span>{error || loadError}</span>
              {loadError && <Button size="sm" variant="outline-danger" onClick={refetch}>Retry</Button>}
            </Alert>
          )}

          {/* Filter Bar (Search, Status, Sorting) */}
          <Row className="g-3 mb-4">
            <Col md={5} sm={12}>
              <InputGroup size="sm">
                <InputGroup.Text className="bg-light text-muted">
                  <Search size={16} />
                </InputGroup.Text>
                <Form.Control
                  placeholder="Search by title or seller..."
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
                <option value="Sold">Sold</option>
                <option value="Paid">Paid</option>
                <option value="Shipping">Shipping</option>
                <option value="Delivered">Delivered</option>
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
          {!loadError && processedItems.length > 0 ? (
            <Table responsive hover className="align-middle border rounded shadow-xs" style={{ fontSize: '0.92rem' }}>
              <thead>
                <tr className="text-white bg-dark-teal" style={{ backgroundColor: '#004e64' }}>
                  <th className="py-3 px-3">Title</th>
                  <th className="py-3">Seller</th>
                  <th className="py-3">Category</th>
                  <th className="py-3 text-end">Starting Price</th>
                  <th className="py-3 text-end">Reserve</th>
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
                    <td className="text-end py-3">{formatVND(item.startingPrice)}</td>
                    <td className="text-end fw-semibold py-3">{formatVND(item.reserve)}</td>
                    <td className="text-center py-3">
                      <Badge
                        className={`px-3 py-2 text-uppercase rounded-pill ${getBadgeStyle(item.status)}`}
                        style={{ fontSize: '0.75rem', fontWeight: '500' }}
                      >
                        {item.status}
                      </Badge>
                      {item.status === 'Rejected' && item.rejectionReason && (
                        <div className="text-danger small mt-1 font-monospace" style={{ fontSize: '0.7rem' }}>
                          Reason: {item.rejectionReason}
                        </div>
                      )}
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
                              onClick={() => handleApprove(item.id)}
                            >
                              <Check size={18} />
                            </Button>
                            <Button
                              variant="link"
                              size="sm"
                              className="text-danger p-0 hover-opacity"
                              title="Reject"
                              onClick={() => handleOpenReject(item)}
                            >
                              <X size={18} />
                            </Button>
                          </>
                        )}
                        {!['Sold', 'Paid', 'Shipping', 'Delivered'].includes(item.status) && (
                          <Button variant="link" size="sm" className="text-info p-0 hover-opacity"
                            title={item.status === 'Active' ? 'Edit description or image' : 'Edit'}
                            onClick={() => handleOpenEdit(item)}>
                            <Edit2 size={16} />
                          </Button>
                        )}
                        {['Pending', 'Rejected'].includes(item.status) && (
                          <Button variant="link" size="sm" className="text-danger p-0 hover-opacity"
                            title="Delete" onClick={() => handleDelete(item)}>
                            <Trash2 size={16} />
                          </Button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </Table>
          ) : !loadError ? (
            <Alert variant="warning" className="text-center py-4">
              No auction items found.
            </Alert>
          ) : null}

          {/* Add Item Modal */}
          <Modal show={showAddModal} onHide={() => setShowAddModal(false)} size="lg" centered>
            <Modal.Header closeButton className="bg-light">
              <Modal.Title className="fw-bold fs-5">Add New Auction Item</Modal.Title>
            </Modal.Header>
            <Form onSubmit={handleAddSubmit}>
              <Modal.Body className="p-4">
                {error && <Alert variant="danger" className="py-2.5 small mb-3">{error}</Alert>}

                <Row className="g-3">
                  <Col md={6}>
                    <Form.Group className="mb-3">
                      <Form.Label className="small fw-semibold">Item Name (Title)</Form.Label>
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
                      <Form.Label className="small fw-semibold">Condition</Form.Label>
                      <Form.Select value={condition} onChange={(e) => setCondition(e.target.value)} required>
                        <option value="NEW">New</option>
                        <option value="LIKE_NEW">Like New</option>
                        <option value="GOOD">Good</option>
                        <option value="FAIR">Fair</option>
                        <option value="POOR">Poor</option>
                      </Form.Select>
                    </Form.Group>
                  </Col>
                </Row>

                <Row className="g-3">
                  <Col md={3}>
                    <Form.Group className="mb-3">
                      <Form.Label className="small fw-semibold">Category</Form.Label>
                      <Form.Select value={categoryId} onChange={(e) => setCategoryId(e.target.value)} required>
                        {categories.map(c => (
                          <option key={c.id} value={c.id}>{c.name}</option>
                        ))}
                      </Form.Select>
                    </Form.Group>
                  </Col>
                  <Col md={3}>
                    <Form.Group className="mb-3">
                      <Form.Label className="small fw-semibold">Starting Price (VND)</Form.Label>
                      <Form.Control
                        type="number"
                        step="0.01"
                        placeholder="e.g. 150.00"
                        value={startingPrice}
                        onChange={(e) => setStartingPrice(e.target.value)}
                        required
                      />
                    </Form.Group>
                  </Col>
                  <Col md={3}>
                    <Form.Group className="mb-3">
                      <Form.Label className="small fw-semibold">Reserve Price (VND)</Form.Label>
                      <Form.Control
                        type="number"
                        step="0.01"
                        placeholder="e.g. 500.00"
                        value={reserve}
                        onChange={(e) => setReserve(e.target.value)}
                        required
                      />
                    </Form.Group>
                  </Col>
                </Row>

                <Form.Group className="mb-3">
                  <Form.Label className="small fw-semibold">Description</Form.Label>
                  <Form.Control
                    as="textarea"
                    rows={3}
                    placeholder="Enter the item's description..."
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    required
                  />
                </Form.Group>

                <Form.Group className="mb-3">
                  <Form.Label className="small fw-semibold d-flex align-items-center gap-2">
                    <ImagePlus size={16} /> Item image
                  </Form.Label>
                  <Form.Control
                    id="staff-new-item-image"
                    className="d-none"
                    type="file"
                    accept="image/png,image/jpeg,image/webp"
                    onChange={(e) => setImageFile(e.target.files?.[0] || null)}
                  />
                  <div className="d-flex align-items-center border rounded overflow-hidden bg-white">
                    <label htmlFor="staff-new-item-image" className="btn btn-outline-secondary rounded-0 border-0 border-end mb-0 text-nowrap">
                      Choose Image
                    </label>
                    <span className="px-3 text-muted text-truncate small">
                      {imageFile?.name || 'No image selected'}
                    </span>
                  </div>
                  {imageFile && (
                    <img
                      src={URL.createObjectURL(imageFile)}
                      alt="New auction item preview"
                      className="mt-2 rounded border object-fit-cover"
                      style={{ width: '120px', height: '90px' }}
                      onLoad={(event) => URL.revokeObjectURL(event.currentTarget.src)}
                    />
                  )}
                  <Form.Text>Choose a JPG, PNG or WebP image. It will be uploaded using the staff image link.</Form.Text>
                </Form.Group>

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
                {error && <Alert variant="danger" className="py-2.5 small mb-3">{error}</Alert>}
                {status === 'Approved' && (
                  <Alert variant="warning" className="small">
                    Changing the name, category or prices sends this item back to Pending. Those fields are locked after a session is assigned.
                  </Alert>
                )}
                {status === 'Active' && (
                  <Alert variant="info" className="small">
                    This auction is active. Only its description and image can be changed.
                  </Alert>
                )}

                <Row className="g-3">
                  <Col md={6}>
                    <Form.Group className="mb-3">
                      <Form.Label className="small fw-semibold">Item Title</Form.Label>
                      <Form.Control
                        type="text"
                        disabled={status === 'Active'}
                        value={title}
                        onChange={(e) => setTitle(e.target.value)}
                        required
                      />
                    </Form.Group>
                  </Col>
                  <Col md={6}>
                    <Form.Group className="mb-3">
                      <Form.Label className="small fw-semibold">Condition</Form.Label>
                      <Form.Select disabled={status === 'Active'} value={condition} onChange={(e) => setCondition(e.target.value)} required>
                        <option value="NEW">New</option>
                        <option value="LIKE_NEW">Like New</option>
                        <option value="GOOD">Good</option>
                        <option value="FAIR">Fair</option>
                        <option value="POOR">Poor</option>
                      </Form.Select>
                    </Form.Group>
                  </Col>
                </Row>

                <Row className="g-3">
                  <Col md={3}>
                    <Form.Group className="mb-3">
                      <Form.Label className="small fw-semibold">Category</Form.Label>
                      <Form.Select disabled={status === 'Active'} value={categoryId} onChange={(e) => setCategoryId(e.target.value)} required>
                        {categories.map(c => (
                          <option key={c.id} value={c.id}>{c.name}</option>
                        ))}
                      </Form.Select>
                    </Form.Group>
                  </Col>
                  <Col md={3}>
                    <Form.Group className="mb-3">
                      <Form.Label className="small fw-semibold">Starting Price (VND)</Form.Label>
                      <Form.Control
                        type="number"
                        disabled={status === 'Active'}
                        step="0.01"
                        value={startingPrice}
                        onChange={(e) => setStartingPrice(e.target.value)}
                        required
                      />
                    </Form.Group>
                  </Col>
                  <Col md={3}>
                    <Form.Group className="mb-3">
                      <Form.Label className="small fw-semibold">Reserve Price (VND)</Form.Label>
                      <Form.Control
                        type="number"
                        disabled={status === 'Active'}
                        step="0.01"
                        value={reserve}
                        onChange={(e) => setReserve(e.target.value)}
                        required
                      />
                    </Form.Group>
                  </Col>
                </Row>

                <Form.Group className="mb-3">
                  <Form.Label className="small fw-semibold">Description</Form.Label>
                  <Form.Control
                    as="textarea"
                    rows={3}
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    required
                  />
                </Form.Group>

                <Form.Group className="mb-3">
                  <Form.Label className="small fw-semibold d-flex align-items-center gap-2">
                    <ImagePlus size={16} /> Item image
                  </Form.Label>
                  <Row className="g-2">
                    <Col md={12}>
                      <Form.Control id="staff-item-image" className="d-none" type="file"
                        accept="image/png,image/jpeg,image/webp"
                        onChange={(e) => setImageFile(e.target.files?.[0] || null)} />
                      <div className="d-flex align-items-center border rounded overflow-hidden bg-white">
                        <label htmlFor="staff-item-image" className="btn btn-outline-secondary rounded-0 border-0 border-end mb-0 text-nowrap">
                          Choose File
                        </label>
                        <span className="px-3 text-muted text-truncate small">
                          {imageFile?.name || 'No file selected'}
                        </span>
                      </div>
                      <Form.Text>Choose a JPG, PNG or WebP image from this computer.</Form.Text>
                    </Col>
                  </Row>
                </Form.Group>

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
        </>
      )}

      {/* Delete Confirmation Modal */}
      <Modal show={showRejectModal} onHide={handleCloseReject} centered backdrop={isRejecting ? 'static' : true}>
        <Form onSubmit={handleRejectSubmit}>
          <Modal.Header closeButton={!isRejecting} className="border-0 px-4 pt-4 pb-2">
            <div className="d-flex align-items-center gap-3">
              <div
                className="d-flex align-items-center justify-content-center rounded-circle flex-shrink-0"
                style={{ width: 46, height: 46, backgroundColor: '#fee2e2', color: '#dc2626' }}
              >
                <Ban size={23} />
              </div>
              <div>
                <Modal.Title className="fw-bold fs-5">Reject auction item</Modal.Title>
                <div className="text-muted small mt-1">This action will notify the seller.</div>
              </div>
            </div>
          </Modal.Header>
          <Modal.Body className="px-4 py-3">
            <div className="rounded-3 border bg-light p-3 mb-3">
              <div className="text-muted text-uppercase fw-semibold mb-1" style={{ fontSize: '0.7rem', letterSpacing: '0.06em' }}>
                Selected item
              </div>
              <div className="fw-semibold text-dark">{itemToReject?.title}</div>
              {itemToReject?.artist && <div className="text-muted small mt-1">Seller: {itemToReject.artist}</div>}
            </div>

            {rejectError && (
              <Alert variant="danger" className="d-flex align-items-center gap-2 py-2 small">
                <AlertCircle size={16} className="flex-shrink-0" /> {rejectError}
              </Alert>
            )}

            <Form.Group controlId="rejectionReason">
              <Form.Label className="fw-semibold">
                Rejection reason <span className="text-danger">*</span>
              </Form.Label>
              <Form.Control
                as="textarea"
                rows={4}
                maxLength={500}
                autoFocus
                disabled={isRejecting}
                isInvalid={Boolean(rejectError) && !rejectionReason.trim()}
                placeholder="Clearly explain what the seller needs to correct..."
                value={rejectionReason}
                onChange={(e) => {
                  setRejectionReason(e.target.value);
                  if (rejectError) setRejectError('');
                }}
                style={{ resize: 'none' }}
              />
              <div className="d-flex justify-content-between mt-2">
                <Form.Text className="text-muted">Be specific and constructive.</Form.Text>
                <Form.Text className={rejectionReason.length >= 450 ? 'text-danger' : 'text-muted'}>
                  {rejectionReason.length}/500
                </Form.Text>
              </div>
            </Form.Group>
          </Modal.Body>
          <Modal.Footer className="border-0 bg-light px-4 py-3">
            <Button variant="outline-secondary" onClick={handleCloseReject} disabled={isRejecting}>
              Cancel
            </Button>
            <Button variant="danger" type="submit" disabled={isRejecting || !rejectionReason.trim()} className="d-flex align-items-center gap-2 fw-semibold">
              {isRejecting ? <Spinner animation="border" size="sm" /> : <X size={17} />}
              {isRejecting ? 'Rejecting...' : 'Reject item'}
            </Button>
          </Modal.Footer>
        </Form>
      </Modal>

      {/* Delete Confirmation Modal */}
      <Modal show={showDeleteConfirm} onHide={() => setShowDeleteConfirm(false)} centered>
        <Modal.Header closeButton className="bg-light">
          <Modal.Title className="fw-bold fs-5">Confirm Deletion</Modal.Title>
        </Modal.Header>
        <Modal.Body className="p-4">
          <p className="mb-0">
            Are you sure you want to delete item <strong>"{itemToDelete?.title}"</strong>?
          </p>
        </Modal.Body>
        <Modal.Footer className="bg-light">
          <Button variant="secondary" size="sm" onClick={() => setShowDeleteConfirm(false)}>
            Cancel
          </Button>
          <Button
            variant="danger"
            size="sm"
            className="fw-semibold"
            onClick={confirmDelete}
          >
            Delete
          </Button>
        </Modal.Footer>
      </Modal>

      <ToastContainer position="bottom-end" className="p-3" style={{ zIndex: 1080 }}>
        <Toast
          onClose={() => setShowToast(false)}
          show={showToast}
          delay={3000}
          autohide
          bg={toastVariant}
        >
          <Toast.Header>
            <strong className="me-auto">
              {toastVariant === 'success' ? 'Success' : 'Error'}
            </strong>
          </Toast.Header>
          <Toast.Body className={toastVariant === 'success' ? 'text-white' : ''}>
            {toastMessage}
          </Toast.Body>
        </Toast>
      </ToastContainer>
    </div>
  );
}
