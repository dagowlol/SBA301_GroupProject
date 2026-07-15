import { useEffect, useContext, useState, useMemo } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Modal, Form, Button, Row, Col, Spinner, Alert } from 'react-bootstrap';
import { sessionSchema } from '../schemas/sessionSchema';
import { useIdempotencyKey } from '../hooks/useIdempotencyKey';
import { useCreateSession, useUpdateSession } from '../hooks/useAuctionSessions';
import { AppContext } from '../../../context/AppContext';
import { sessionService } from '../services/sessionService';

const toLocalISOString = (date) => {
  const d = new Date(date);
  const tzOffset = d.getTimezoneOffset() * 60000;
  return new Date(d.getTime() - tzOffset).toISOString().slice(0, 16);
};

export default function SessionFormModal({ show, onHide, sessionData, mode = 'create' }) {
  const { items } = useContext(AppContext);
  const approvedItems = useMemo(() => items.filter(item => item.status === 'Approved'), [items]);
  const defaultItemId = approvedItems.length > 0 ? approvedItems[0].id : '';

  const { idempotencyKey, resetIdempotencyKey } = useIdempotencyKey();
  const createMutation = useCreateSession();
  const updateMutation = useUpdateSession();

  const isEdit = mode === 'edit';
  const isActive = isEdit && sessionData?.status === 'ACTIVE';

  const {
    register,
    handleSubmit,
    reset,
    watch,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: zodResolver(sessionSchema),
  });

  const [apiError, setApiError] = useState('');
  const [loadingDetails, setLoadingDetails] = useState(false);

  const watchStatus = watch('status');

  useEffect(() => {
    if (show) {
      setApiError('');
      resetIdempotencyKey();

      if (isEdit && sessionData) {
        setLoadingDetails(true);
        sessionService.getSessionById(sessionData.id)
          .then(detail => {
            reset({
              itemId: detail.itemId || '',
              startTime: toLocalISOString(detail.startTime),
              endTime: toLocalISOString(detail.endTime),
              reservePrice: detail.reservePrice || '',
              minimumIncrement: detail.minimumIncrement || '',
              antiSnipeWindowSeconds: detail.antiSnipeWindowSeconds ?? 10,
              antiSnipeExtensionSeconds: detail.antiSnipeExtensionSeconds ?? 30,
              status: detail.status || 'SCHEDULED',
              cancellationReason: detail.cancellationReason || ''
            });
          })
          .catch(err => setApiError(err.message))
          .finally(() => setLoadingDetails(false));
      } else {
        const tomorrow = new Date();
        tomorrow.setDate(tomorrow.getDate() + 1);
        const tenDaysLater = new Date();
        tenDaysLater.setDate(tenDaysLater.getDate() + 10);

        reset({
          itemId: defaultItemId,
          startTime: toLocalISOString(tomorrow),
          endTime: toLocalISOString(tenDaysLater),
          reservePrice: '',
          minimumIncrement: '',
          antiSnipeWindowSeconds: 10,
          antiSnipeExtensionSeconds: 30,
          status: 'SCHEDULED',
          cancellationReason: ''
        });
      }
    }
  }, [show, isEdit, sessionData, reset, defaultItemId, resetIdempotencyKey]);

  const onSubmit = async (data) => {
    setApiError('');
    try {
      const payload = {
        endTime: data.endTime,
        reservePrice: data.reservePrice ? Number(data.reservePrice) : null,
        minimumIncrement: data.minimumIncrement ? Number(data.minimumIncrement) : null,
        antiSnipeWindowSeconds: Number(data.antiSnipeWindowSeconds),
        antiSnipeExtensionSeconds: Number(data.antiSnipeExtensionSeconds),
        ...(isEdit ? {
          status: data.status,
          cancellationReason: data.status === 'CANCELLED' ? data.cancellationReason : null
        } : {
          itemId: Number(data.itemId),
          startTime: data.startTime
        })
      };

      if (isEdit) {
        await updateMutation.mutateAsync({ id: sessionData.id, data: payload });
      } else {
        await createMutation.mutateAsync({ data: payload, idempotencyKey });
      }
      onHide();
    } catch (error) {
      setApiError(error.message || 'Failed to save session');
    }
  };

  return (
    <Modal show={show} onHide={onHide} size="lg" centered>
      <Modal.Header closeButton className="border-0 pb-0">
        <Modal.Title className="fw-bold fs-5">{isEdit ? `Edit Session #${sessionData?.id}` : 'Create New Session'}</Modal.Title>
      </Modal.Header>

      <Form onSubmit={handleSubmit(onSubmit)}>
        <Modal.Body className="px-4">
          {apiError && <Alert variant="danger" className="py-2 small">{apiError}</Alert>}
          {loadingDetails ? (
            <div className="text-center py-4"><Spinner animation="border" variant="secondary" /></div>
          ) : (
            <>
              {!isEdit && (
                <Form.Group className="mb-3">
                  <Form.Label className="small fw-semibold text-muted mb-1">Select Item</Form.Label>
                  <Form.Select {...register('itemId')} isInvalid={!!errors.itemId}>
                    <option value="">-- Choose Approved Item --</option>
                    {approvedItems.map(item => (
                      <option key={item.id} value={item.id}>{item.title}</option>
                    ))}
                  </Form.Select>
                  <Form.Control.Feedback type="invalid">{errors.itemId?.message}</Form.Control.Feedback>
                </Form.Group>
              )}

              <Row className="g-3 mb-3">
                <Col md={6}>
                  <Form.Group>
                    <Form.Label className="small fw-semibold text-muted mb-1">Start Time</Form.Label>
                    <Form.Control type="datetime-local" {...register('startTime')} isInvalid={!!errors.startTime} disabled={isEdit} />
                    <Form.Control.Feedback type="invalid">{errors.startTime?.message}</Form.Control.Feedback>
                  </Form.Group>
                </Col>
                <Col md={6}>
                  <Form.Group>
                    <Form.Label className="small fw-semibold text-muted mb-1">End Time</Form.Label>
                    <Form.Control type="datetime-local" {...register('endTime')} isInvalid={!!errors.endTime} />
                    <Form.Control.Feedback type="invalid">{errors.endTime?.message}</Form.Control.Feedback>
                  </Form.Group>
                </Col>
              </Row>

              {isEdit && (
                <Row className="g-3 mb-3">
                  <Col md={6}>
                    <Form.Group>
                      <Form.Label className="small fw-semibold text-muted mb-1">Status</Form.Label>
                      <Form.Select {...register('status')}>
                        <option value="SCHEDULED">Scheduled</option>
                        <option value="ACTIVE">Active</option>
                        <option value="ENDED">Ended</option>
                        <option value="CANCELLED">Cancelled</option>
                      </Form.Select>
                    </Form.Group>
                  </Col>
                  <Col md={6}>
                    {watchStatus === 'CANCELLED' && (
                      <Form.Group>
                        <Form.Label className="small fw-semibold text-muted mb-1">Cancellation Reason</Form.Label>
                        <Form.Control type="text" {...register('cancellationReason')} isInvalid={!!errors.cancellationReason} />
                        <Form.Control.Feedback type="invalid">{errors.cancellationReason?.message}</Form.Control.Feedback>
                      </Form.Group>
                    )}
                  </Col>
                </Row>
              )}

              <Row className="g-3 mb-3">
                <Col md={6}>
                  <Form.Group>
                    <Form.Label className="small fw-semibold text-muted mb-1">Reserve Price (USD)</Form.Label>
                    <Form.Control
                      type="number"
                      step="0.01"
                      placeholder="e.g. 500.00"
                      {...register('reservePrice')}
                      isInvalid={!!errors.reservePrice}
                      disabled={isActive}
                    />
                    <Form.Control.Feedback type="invalid">{errors.reservePrice?.message}</Form.Control.Feedback>
                  </Form.Group>
                </Col>
                <Col md={6}>
                  <Form.Group>
                    <Form.Label className="small fw-semibold text-muted mb-1">Minimum Increment (USD)</Form.Label>
                    <Form.Control
                      type="number"
                      step="0.01"
                      placeholder="e.g. 50.00"
                      {...register('minimumIncrement')}
                      isInvalid={!!errors.minimumIncrement}
                      disabled={isActive}
                    />
                    <Form.Control.Feedback type="invalid">{errors.minimumIncrement?.message}</Form.Control.Feedback>
                  </Form.Group>
                </Col>
              </Row>

              <Row className="g-3">
                <Col md={6}>
                  <Form.Group>
                    <Form.Label className="small fw-semibold text-muted mb-1">Anti-Snipe Window (s)</Form.Label>
                    <Form.Control type="number" {...register('antiSnipeWindowSeconds')} isInvalid={!!errors.antiSnipeWindowSeconds} disabled={isActive} />
                    <Form.Control.Feedback type="invalid">{errors.antiSnipeWindowSeconds?.message}</Form.Control.Feedback>
                  </Form.Group>
                </Col>
                <Col md={6}>
                  <Form.Group>
                    <Form.Label className="small fw-semibold text-muted mb-1">Anti-Snipe Extension (s)</Form.Label>
                    <Form.Control type="number" {...register('antiSnipeExtensionSeconds')} isInvalid={!!errors.antiSnipeExtensionSeconds} disabled={isActive} />
                    <Form.Control.Feedback type="invalid">{errors.antiSnipeExtensionSeconds?.message}</Form.Control.Feedback>
                  </Form.Group>
                </Col>
              </Row>
            </>
          )}
        </Modal.Body>
        <Modal.Footer className="border-0 pt-0">
          <Button variant="light" onClick={onHide} className="text-muted fw-medium border">Cancel</Button>
          <Button variant="primary" type="submit" disabled={isSubmitting || loadingDetails} style={{ backgroundColor: '#004e64', borderColor: '#004e64' }}>
            {isSubmitting ? <Spinner size="sm" animation="border" /> : (isEdit ? 'Save Changes' : 'Create Session')}
          </Button>
        </Modal.Footer>
      </Form>
    </Modal>
  );
}
