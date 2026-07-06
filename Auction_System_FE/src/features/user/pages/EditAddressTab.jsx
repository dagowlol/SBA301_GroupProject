import React, { useState, useEffect } from 'react';
import { Form, Button, Alert, Spinner } from 'react-bootstrap';
import { userApi } from '../../../api/userApi';

export default function EditAddressTab({ user }) {
  const [address, setAddress] = useState('');
  const [loading, setLoading] = useState(false);
  const [fetching, setFetching] = useState(true);
  const [success, setSuccess] = useState('');
  const [error, setError] = useState('');

  const [isEditing, setIsEditing] = useState(false);

  useEffect(() => {
    const fetchUser = async () => {
      if (user?.id) {
        try {
          const res = await userApi.getUserById(user.id);
          setAddress(res.address || '');
        } catch (err) {
          console.error("Failed to fetch user details", err);
          setError("Failed to load current address.");
        } finally {
          setFetching(false);
        }
      }
    };
    fetchUser();
  }, [user]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSuccess('');
    setError('');
    setLoading(true);

    try {
      await userApi.updateUser(user.id, { address });
      setSuccess('Shipping address updated successfully!');
      setIsEditing(false);
    } catch (err) {
      setError(err?.message || 'Failed to update address.');
    } finally {
      setLoading(false);
    }
  };

  if (fetching) {
    return <Spinner animation="border" variant="primary" />;
  }

  return (
    <div>
      <h4 className="mb-4 text-teal fw-bold">Shipping Address</h4>
      
      {success && <Alert variant="success">{success}</Alert>}
      {error && <Alert variant="danger">{error}</Alert>}

      <div className="w-75 p-4 border rounded bg-white shadow-sm">
        {!isEditing ? (
          <div>
            <h6 className="fw-semibold text-secondary mb-3">Current Default Address</h6>
            <p className="mb-4 text-dark" style={{ whiteSpace: 'pre-line' }}>
              {address || <em>No address provided yet.</em>}
            </p>
            <Button 
              variant="outline-primary" 
              onClick={() => setIsEditing(true)}
            >
              Edit Address
            </Button>
          </div>
        ) : (
          <Form onSubmit={handleSubmit}>
            <Form.Group className="mb-4">
              <Form.Label className="fw-semibold text-secondary">Update Shipping Address</Form.Label>
              <Form.Control 
                as="textarea" 
                rows={4}
                placeholder="Enter your full shipping address" 
                value={address}
                onChange={(e) => setAddress(e.target.value)}
                required
              />
              <Form.Text className="text-muted">
                This address will be used as the default for shipping your items.
              </Form.Text>
            </Form.Group>

            <div className="d-flex gap-2">
              <Button 
                variant="secondary" 
                onClick={() => {
                  setIsEditing(false);
                  setSuccess('');
                  setError('');
                }}
                disabled={loading}
              >
                Cancel
              </Button>
              <Button 
                variant="teal" 
                type="submit" 
                disabled={loading}
                style={{ backgroundColor: '#004e64', color: 'white' }}
              >
                {loading ? <Spinner size="sm" /> : 'Save Changes'}
              </Button>
            </div>
          </Form>
        )}
      </div>
    </div>
  );
}
