import { useContext, useState, useMemo } from 'react';
import { AppContext } from '../../../context/AppContext';
import {
  Table,
  Button,
  Form,
  InputGroup,
  Modal,
  Badge,
  Row,
  Col,
  Alert,
  Spinner,
  Toast,
  ToastContainer
} from 'react-bootstrap';
import { Plus, Search, Edit2, Trash2, GripVertical, ArrowUpDown } from 'lucide-react';

export default function CategoryManagement() {
  const { categories, addCategory, editCategory, deleteCategory, items, loading } = useContext(AppContext);

  // Search & sorting state
  const [searchQuery, setSearchQuery] = useState('');
  const [sortField, setSortField] = useState('order'); // order, name
  const [sortDirection, setSortDirection] = useState('asc'); // asc, desc

  // Modals state
  const [showAddModal, setShowAddModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [selectedCat, setSelectedCat] = useState(null);
  const [categoryToDelete, setCategoryToDelete] = useState(null);

  // Form states
  const [name, setName] = useState('');
  const [slug, setSlug] = useState('');
  const [description, setDescription] = useState('');
  const [parentCategoryId, setParentCategoryId] = useState('');
  const [status, setStatus] = useState('Active');
  const [order, setOrder] = useState('');
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

  // Handle auto slug generation from name
  const handleNameChange = (val) => {
    setName(val);
    setSlug(val.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/(^-|-$)/g, ''));
  };

  // Open modals
  const handleOpenAdd = () => {
    setName('');
    setDescription('');
    setParentCategoryId('');
    setSlug('');
    setStatus('Active');
    setOrder(categories.length + 1);
    setError('');
    setShowAddModal(true);
  };

  const handleOpenEdit = (cat) => {
    setSelectedCat(cat);
    setName(cat.name);
    setDescription(cat.description || '');
    setParentCategoryId(cat.parentCategoryId || '');
    setSlug(cat.slug);
    setStatus(cat.status);
    setOrder(cat.order);
    setError('');
    setShowEditModal(true);
  };

  // Form Submissions (Async to support Backend API errors)
  const handleAddSubmit = async (e) => {
    e.preventDefault();
    if (!name.trim()) return;
    setError('');

    try {
      await addCategory({
        name,
        description,
        parentCategoryId: parentCategoryId ? parseInt(parentCategoryId) : null,
        order: parseInt(order) || (categories.length + 1)
      });
      setShowAddModal(false);
      triggerToast(`Category "${name}" has been added successfully.`);
    } catch (err) {
      setError(err.message || 'Failed to create category on backend');
    }
  };

  const handleEditSubmit = async (e) => {
    e.preventDefault();
    if (!selectedCat || !name.trim()) return;
    setError('');

    try {
      await editCategory(selectedCat.id, {
        name,
        description,
        parentCategoryId: parentCategoryId ? parseInt(parentCategoryId) : null,
        order: parseInt(order) || selectedCat.order,
        status
      });
      setShowEditModal(false);
      triggerToast(`Category "${name}" has been updated successfully.`);
    } catch (err) {
      setError(err.message || 'Failed to edit category on backend');
    }
  };

  const handleDelete = (cat) => {
    setCategoryToDelete(cat);
    setShowDeleteConfirm(true);
  };

  const confirmDelete = async () => {
    if (!categoryToDelete) return;
    setError('');
    try {
      await deleteCategory(categoryToDelete.id);
      triggerToast(`Category "${categoryToDelete.name}" has been deleted successfully.`);
    } catch (err) {
      setError(err.message || 'Failed to delete category from backend');
    } finally {
      setShowDeleteConfirm(false);
      setCategoryToDelete(null);
    }
  };

  // Dynamically calculate items in category
  const getItemsCount = (categoryName) => {
    return items ? items.filter(item => item.category.toLowerCase() === categoryName.toLowerCase()).length : 0;
  };

  // Sorting helper
  const handleSort = (field) => {
    const dir = sortField === field && sortDirection === 'asc' ? 'desc' : 'asc';
    setSortField(field);
    setSortDirection(dir);
  };

  // Process data (Search & Sort)
  const processedCategories = useMemo(() => {
    let result = [...categories];

    // Search query filter
    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase();
      result = result.filter(c =>
        c.name.toLowerCase().includes(q) ||
        (c.description && c.description.toLowerCase().includes(q))
      );
    }

    // Sort
    result.sort((a, b) => {
      let aVal = a[sortField];
      let bVal = b[sortField];

      // Handle itemsCount sorting dynamically
      if (sortField === 'itemsCount') {
        aVal = getItemsCount(a.name);
        bVal = getItemsCount(b.name);
      }

      if (typeof aVal === 'string') {
        aVal = aVal.toLowerCase();
        bVal = bVal.toLowerCase();
      }

      if (aVal < bVal) return sortDirection === 'asc' ? -1 : 1;
      if (aVal > bVal) return sortDirection === 'asc' ? 1 : -1;
      return 0;
    });

    return result;
  }, [categories, searchQuery, sortField, sortDirection, items]);

  return (
    <div className="bg-white rounded p-4 shadow-sm border text-start">
      {loading ? (
        <div className="d-flex flex-column align-items-center justify-content-center py-5">
          <Spinner animation="border" variant="primary" />
          <span className="mt-3 text-muted">Loading categories...</span>
        </div>
      ) : (
        <>
          {/* Title Header */}
          <div className="d-flex justify-content-between align-items-center mb-4">
            <div>
              <h2 className="fw-bold text-dark mb-1">Auction Category Management</h2>
              <p className="text-muted small m-0">Add, Edit, Delete, Sort and Search Auction Categories</p>
            </div>
            <Button
              variant="dark"
              onClick={handleOpenAdd}
              className="d-flex align-items-center gap-1 text-uppercase fw-semibold"
              style={{ backgroundColor: '#004e64', borderColor: '#004e64', fontSize: '0.85rem' }}
            >
              <Plus size={16} />
              <span>Add Category</span>
            </Button>
          </div>

          {error && <Alert variant="danger" className="mb-4 py-2.5 small">{error}</Alert>}

          {/* Search Filter Bar */}
          <div className="mb-4" style={{ maxWidth: '350px' }}>
            <InputGroup size="sm">
              <InputGroup.Text className="bg-light text-muted">
                <Search size={16} />
              </InputGroup.Text>
              <Form.Control
                placeholder="Search categories..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </InputGroup>
          </div>

          {/* Categories Table */}
          {processedCategories.length > 0 ? (
            <Table responsive hover className="align-middle border rounded shadow-xs" style={{ fontSize: '0.92rem' }}>
              <thead>
                <tr className="text-white bg-dark-teal" style={{ backgroundColor: '#004e64' }}>
                  <th style={{ width: '40px' }} className="py-3 px-3"></th>
                  <th className="py-3 cursor-pointer" onClick={() => handleSort('name')}>
                    <div className="d-flex align-items-center gap-1">
                      <span>Name</span>
                      <ArrowUpDown size={12} className="text-white-50" />
                    </div>
                  </th>
                  <th className="py-3">Slug</th>
                  <th className="py-3">Description</th>
                  <th className="py-3">Parent Category</th>
                  <th className="py-3 cursor-pointer text-center" onClick={() => handleSort('itemsCount')}>
                    <div className="d-flex align-items-center justify-content-center gap-1">
                      <span>Items</span>
                      <ArrowUpDown size={12} className="text-white-50" />
                    </div>
                  </th>
                  <th className="py-3 cursor-pointer text-center" onClick={() => handleSort('order')}>
                    <div className="d-flex align-items-center justify-content-center gap-1">
                      <span>Order</span>
                      <ArrowUpDown size={12} className="text-white-50" />
                    </div>
                  </th>
                  <th className="py-3 text-center">Status</th>
                  <th className="py-3 text-center" style={{ width: '100px' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {processedCategories.map((cat) => {
                  const isActive = cat.status === 'Active';
                  return (
                    <tr key={cat.id}>
                      <td className="text-muted text-center py-3">
                        <GripVertical size={16} />
                      </td>
                      <td className="fw-bold text-dark py-3">
                        {cat.name}
                      </td>
                      <td className="text-muted font-monospace py-3" style={{ fontSize: '0.82rem' }}>{cat.slug}</td>
                      <td className="text-muted small py-3 text-truncate" style={{ maxWidth: '180px' }}>
                        {cat.description || <span className="text-light-muted italic">No description</span>}
                      </td>
                      <td className="text-muted py-3">
                        {cat.parentCategoryName || <span className="text-muted opacity-50 small">-</span>}
                      </td>
                      <td className="text-center py-3 fw-semibold text-muted">{getItemsCount(cat.name)}</td>
                      <td className="text-center py-3">{cat.order}</td>
                      <td className="text-center py-3">
                        <Badge
                          bg={isActive ? "success-light" : "secondary-light"}
                          className={`px-3 py-2 text-uppercase rounded-pill ${isActive ? 'bg-success-subtle text-success border border-success-subtle' : 'bg-secondary-subtle text-secondary border border-secondary-subtle'
                            }`}
                          style={{ fontSize: '0.75rem' }}
                        >
                          {cat.status}
                        </Badge>
                      </td>
                      <td className="text-center py-3">
                        <div className="d-flex gap-2 justify-content-center">
                          <Button
                            variant="link"
                            size="sm"
                            className="text-info p-0 hover-opacity"
                            onClick={() => handleOpenEdit(cat)}
                          >
                            <Edit2 size={16} />
                          </Button>
                          <Button
                            variant="link"
                            size="sm"
                            className="text-danger p-0 hover-opacity"
                            onClick={() => handleDelete(cat)}
                          >
                            <Trash2 size={16} />
                          </Button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </Table>
          ) : (
            <Alert variant="warning" className="text-center py-4">
              No categories found.
            </Alert>
          )}

          {/* Add Category Modal */}
          <Modal show={showAddModal} onHide={() => setShowAddModal(false)} centered>
            <Modal.Header closeButton className="bg-light">
              <Modal.Title className="fw-bold fs-5">Add New Category</Modal.Title>
            </Modal.Header>
            <Form onSubmit={handleAddSubmit}>
              <Modal.Body className="p-4">
                {error && <Alert variant="danger" className="py-2.5 small mb-3">{error}</Alert>}

                <Form.Group className="mb-3">
                  <Form.Label className="small fw-semibold">Category Name</Form.Label>
                  <Form.Control
                    type="text"
                    placeholder="e.g. Paintings, Sculptures"
                    value={name}
                    onChange={(e) => handleNameChange(e.target.value)}
                    required
                    maxLength={100}
                  />
                </Form.Group>

                <Form.Group className="mb-3">
                  <Form.Label className="small fw-semibold">Description</Form.Label>
                  <Form.Control
                    as="textarea"
                    rows={2}
                    placeholder="Add description..."
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                  />
                </Form.Group>

                <Form.Group className="mb-3">
                  <Form.Label className="small fw-semibold">Parent Category (Optional)</Form.Label>
                  <Form.Select
                    value={parentCategoryId}
                    onChange={(e) => setParentCategoryId(e.target.value)}
                  >
                    <option value="">None (Top Level)</option>
                    {categories.map(c => (
                      <option key={c.id} value={c.id}>{c.name}</option>
                    ))}
                  </Form.Select>
                </Form.Group>

                <Row className="g-3">
                  <Col sm={6}>
                    <Form.Group className="mb-3">
                      <Form.Label className="small fw-semibold">Sort Order</Form.Label>
                      <Form.Control
                        type="number"
                        min={0}
                        value={order}
                        onChange={(e) => setOrder(e.target.value)}
                      />
                    </Form.Group>
                  </Col>
                  <Col sm={6}>
                    <Form.Group className="mb-3">
                      <Form.Label className="small fw-semibold">Status</Form.Label>
                      <Form.Select value={status} onChange={(e) => setStatus(e.target.value)}>
                        <option value="Active">Active</option>
                        <option value="Inactive">Inactive</option>
                      </Form.Select>
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
                  Add Category
                </Button>
              </Modal.Footer>
            </Form>
          </Modal>

          {/* Edit Category Modal */}
          <Modal show={showEditModal} onHide={() => setShowEditModal(false)} centered>
            <Modal.Header closeButton className="bg-light">
              <Modal.Title className="fw-bold fs-5">Edit Category</Modal.Title>
            </Modal.Header>
            <Form onSubmit={handleEditSubmit}>
              <Modal.Body className="p-4">
                {error && <Alert variant="danger" className="py-2.5 small mb-3">{error}</Alert>}

                <Form.Group className="mb-3">
                  <Form.Label className="small fw-semibold">Category Name</Form.Label>
                  <Form.Control
                    type="text"
                    value={name}
                    onChange={(e) => handleNameChange(e.target.value)}
                    required
                    maxLength={100}
                  />
                </Form.Group>

                <Form.Group className="mb-3">
                  <Form.Label className="small fw-semibold">Description</Form.Label>
                  <Form.Control
                    as="textarea"
                    rows={2}
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                  />
                </Form.Group>

                <Form.Group className="mb-3">
                  <Form.Label className="small fw-semibold">Parent Category (Optional)</Form.Label>
                  <Form.Select
                    value={parentCategoryId}
                    onChange={(e) => setParentCategoryId(e.target.value)}
                  >
                    <option value="">None (Top Level)</option>
                    {categories
                      .filter(c => c.id !== selectedCat?.id) // Prevent self-referencing parent
                      .map(c => (
                        <option key={c.id} value={c.id}>{c.name}</option>
                      ))
                    }
                  </Form.Select>
                </Form.Group>

                <Row className="g-3">
                  <Col sm={6}>
                    <Form.Group className="mb-3">
                      <Form.Label className="small fw-semibold">Sort Order</Form.Label>
                      <Form.Control
                        type="number"
                        min={0}
                        value={order}
                        onChange={(e) => setOrder(e.target.value)}
                      />
                    </Form.Group>
                  </Col>
                  <Col sm={6}>
                    <Form.Group className="mb-3">
                      <Form.Label className="small fw-semibold">Status</Form.Label>
                      <Form.Select value={status} onChange={(e) => setStatus(e.target.value)}>
                        <option value="Active">Active</option>
                        <option value="Inactive">Inactive</option>
                      </Form.Select>
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
        </>
      )}

      {/* Delete Confirmation Modal */}
      <Modal show={showDeleteConfirm} onHide={() => setShowDeleteConfirm(false)} centered>
        <Modal.Header closeButton className="bg-light">
          <Modal.Title className="fw-bold fs-5">Confirm Deletion</Modal.Title>
        </Modal.Header>
        <Modal.Body className="p-4">
          <p className="mb-0">
            Are you sure you want to delete category <strong>"{categoryToDelete?.name}"</strong>?
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
