import React, { useEffect, useState } from 'react';
import { Modal, Form, InputNumber, Switch, Button, Spin, message } from 'antd';
import { Cpu, AlertTriangle, ShieldCheck } from 'lucide-react';
import { auctionApi } from '../../../api/auctionApi';

export default function AutoBidModal({ open, onClose, sessionId, currentPrice, minimumIncrement, onConfigSaved }) {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [isFormValid, setIsFormValid] = useState(false);

  // Minimum required max bid amount
  const minRequiredMaxBid = (currentPrice || 0) + (minimumIncrement || 0);

  // Load config on open
  useEffect(() => {
    if (open && sessionId) {
      setLoading(true);
      setIsFormValid(false);
      auctionApi.getAutoBidConfig(sessionId)
        .then((config) => {
          if (config && config.id) {
            form.setFieldsValue({
              maxBidAmount: config.maxBidAmount,
              bidIncrement: config.bidIncrement || minimumIncrement,
              isActive: config.isActive,
            });
            // Re-validate to set button state correctly
            form.validateFields()
              .then(() => setIsFormValid(true))
              .catch(() => setIsFormValid(false));
          } else {
            form.setFieldsValue({
              maxBidAmount: undefined,
              bidIncrement: minimumIncrement,
              isActive: false,
            });
            setIsFormValid(false);
          }
        })
        .catch((err) => {
          console.error('Failed to fetch auto-bid config:', err);
          message.error('Unable to load the auto-bid configuration.');
        })
        .finally(() => {
          setLoading(false);
        });
    }
  }, [open, sessionId, form, minimumIncrement]);

  // Handle form field validation on changes
  const handleValuesChange = async () => {
    try {
      const maxBid = form.getFieldValue('maxBidAmount');
      const bidInc = form.getFieldValue('bidIncrement');
      
      // Perform manual check to determine if valid
      if (maxBid && maxBid >= minRequiredMaxBid && (!bidInc || bidInc >= minimumIncrement)) {
        setIsFormValid(true);
      } else {
        setIsFormValid(false);
      }
    } catch {
      setIsFormValid(false);
    }
  };

  const handleSave = async () => {
    try {
      const values = await form.validateFields();
      setSaving(true);
      const savedConfig = await auctionApi.saveAutoBidConfig(sessionId, {
        maxBidAmount: values.maxBidAmount,
        bidIncrement: values.bidIncrement,
        isActive: values.isActive,
      });
      message.success('Auto-bid configuration saved successfully.');
      if (onConfigSaved) {
        onConfigSaved(savedConfig);
      }
      onClose();
    } catch (err) {
      if (err.name === 'FieldsError') return;
      console.error('Failed to save auto-bid config:', err);
      message.error(err.message || 'Failed to save the configuration. Please try again.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <Modal
      open={open}
      onCancel={onClose}
      title={
        <div className="d-flex align-items-center gap-2 border-bottom pb-3 mb-3">
          <Cpu className="text-primary" size={24} />
          <span className="fw-bold fs-5" style={{ color: '#004e64' }}>Auto-bid Configuration</span>
        </div>
      }
      footer={[
        <Button key="cancel" onClick={onClose} className="rounded-3 px-4" disabled={saving}>
          Cancel
        </Button>,
        <Button
          key="submit"
          type="primary"
          onClick={handleSave}
          className="rounded-3 px-4"
          style={{ backgroundColor: isFormValid ? '#004e64' : '#d9d9d9', borderColor: isFormValid ? '#004e64' : '#d9d9d9' }}
          disabled={!isFormValid || saving}
          loading={saving}
        >
          Save Configuration
        </Button>,
      ]}
      centered
      destroyOnClose
      width={480}
      styles={{ body: { padding: '8px 12px' } }}
    >
      <Spin spinning={loading}>
        <div className="mb-4 text-muted small bg-light p-3 rounded-4 border border-light d-flex gap-2">
          <ShieldCheck className="text-success flex-shrink-0" size={18} />
          <span>
            The system will bid automatically for you within the limits configured below.
          </span>
        </div>

        <Form
          form={form}
          layout="vertical"
          requiredMark={false}
          onValuesChange={handleValuesChange}
          initialValues={{
            bidIncrement: minimumIncrement,
            isActive: false,
          }}
        >
          {/* Max Bid Amount */}
          <Form.Item
            name="maxBidAmount"
            label={<span className="fw-semibold text-dark">Maximum Bid Amount</span>}
            validateTrigger={['onChange', 'onBlur']}
            rules={[
              { required: true, message: 'Please enter a maximum bid amount.' },
              () => ({
                validator(_, value) {
                  if (value === undefined || value === null || value >= minRequiredMaxBid) {
                    return Promise.resolve();
                  }
                  return Promise.reject(
                    new Error(`Maximum bid must be at least ${minRequiredMaxBid.toLocaleString()} VND (current price plus minimum increment).`)
                  );
                },
              }),
            ]}
          >
            <InputNumber
              className="w-100 rounded-3"
              style={{ fontSize: '1.05rem', padding: '6px 12px' }}
              placeholder={`At least ${minRequiredMaxBid.toLocaleString()}`}
              formatter={(value) => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
              parser={(value) => value.replace(/\$\s?|(,*)/g, '')}
              addonAfter="VND"
              size="large"
            />
          </Form.Item>

          {/* Bid Increment */}
          <Form.Item
            name="bidIncrement"
            label={<span className="fw-semibold text-dark">Auto-bid Increment</span>}
            validateTrigger={['onChange', 'onBlur']}
            rules={[
              { required: true, message: 'Please enter an auto-bid increment.' },
              () => ({
                validator(_, value) {
                  if (value === undefined || value === null || value >= minimumIncrement) {
                    return Promise.resolve();
                  }
                  return Promise.reject(
                    new Error(`Bid increment must be at least ${minimumIncrement.toLocaleString()} VND.`)
                  );
                },
              }),
            ]}
          >
            <InputNumber
              className="w-100 rounded-3"
              style={{ fontSize: '1.05rem', padding: '6px 12px' }}
              placeholder={`Minimum ${minimumIncrement.toLocaleString()}`}
              formatter={(value) => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
              parser={(value) => value.replace(/\$\s?|(,*)/g, '')}
              addonAfter="VND"
              size="large"
            />
          </Form.Item>

          {/* Active Switch */}
          <div className="d-flex justify-content-between align-items-center bg-light p-3 rounded-4 border border-light mt-4">
            <div>
              <div className="fw-bold text-dark mb-1">Enable Auto-bid</div>
              <div className="text-muted small">Turn this on to start automatic bidding immediately.</div>
            </div>
            <Form.Item name="isActive" valuePropName="checked" className="mb-0">
              <Switch checkedChildren="ON" unCheckedChildren="OFF" style={{ backgroundColor: form.getFieldValue('isActive') ? '#004e64' : '#d9d9d9' }} />
            </Form.Item>
          </div>
        </Form>
      </Spin>
    </Modal>
  );
}
