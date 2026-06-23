import React, { useState, useEffect } from 'react';
import { Table, Input, Button, Modal, Space, Tag, Form } from 'antd';
import { Scale, CheckCircle2, XCircle, Search, Eye } from 'lucide-react';
import { fetchDisputes, resolveDispute } from '../services/disputeApi';
import 'bootstrap/dist/css/bootstrap.min.css';
import './DisputeManagement.css';

export default function DisputeManagement() {
  const [disputes, setDisputes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({ total: 0, open: 0, resolved: 0 });
  
  const [searchText, setSearchText] = useState('');
  const [filterTab, setFilterTab] = useState('All'); // 'All', 'OPEN', 'IN_REVIEW', 'RESOLVED', 'REJECTED'
  
  const [selectedDispute, setSelectedDispute] = useState(null);
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [resolveReason, setResolveReason] = useState('');

  const loadData = async () => {
    setLoading(true);
    try {
      const response = await fetchDisputes(0, 100);
      const data = response?.content || [];
      setDisputes(data);

      setStats({
        total: data.length,
        open: data.filter(d => d.status === 'OPEN' || d.status === 'IN_REVIEW').length,
        resolved: data.filter(d => d.status === 'RESOLVED').length,
      });
    } catch (error) {
      console.error('Error loading disputes:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
    const interval = setInterval(loadData, 60000); // refresh every minute
    return () => clearInterval(interval);
  }, []);

  const handleResolveAction = async (decisionStatus) => {
    if (!selectedDispute || !resolveReason) return;
    try {
      await resolveDispute(selectedDispute.id, decisionStatus, resolveReason);
      setIsModalVisible(false);
      setSelectedDispute(null);
      setResolveReason('');
      loadData();
    } catch (error) {
      console.error('Error resolving dispute:', error);
    }
  };

  // ----- Filtering -----
  const getFilteredDisputes = () => {
    return disputes.filter(disp => {
      const matchesSearch = 
        String(disp.sessionId).includes(searchText) || 
        String(disp.raisedByName).toLowerCase().includes(searchText.toLowerCase()) ||
        String(disp.id).includes(searchText);

      if (filterTab !== 'All') {
        return matchesSearch && disp.status === filterTab;
      }
      return matchesSearch;
    });
  };

  const getStatusTag = (status) => {
    switch (status) {
      case 'OPEN': return <Tag color="warning">OPEN</Tag>;
      case 'IN_REVIEW': return <Tag color="processing">IN REVIEW</Tag>;
      case 'RESOLVED': return <Tag color="success">RESOLVED</Tag>;
      case 'REJECTED': return <Tag color="error">REJECTED</Tag>;
      default: return <Tag>{status}</Tag>;
    }
  };

  // ----- Columns -----
  const columns = [
    {
      title: 'Dispute ID',
      dataIndex: 'id',
      key: 'id',
      render: (text) => <strong>#{text}</strong>,
    },
    {
      title: 'Session',
      dataIndex: 'sessionId',
      key: 'sessionId',
      render: (text) => <span>Session #{text}</span>,
    },
    {
      title: 'Raised By',
      dataIndex: 'raisedByName',
      key: 'raisedByName',
    },
    {
      title: 'Type',
      dataIndex: 'type',
      key: 'type',
    },
    {
      title: 'Date',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (text) => <span className="text-muted">{new Date(text).toLocaleDateString()}</span>,
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (status) => getStatusTag(status),
    },
    {
      title: 'Actions',
      key: 'actions',
      render: (_, record) => (
        <Space size="middle">
          <Button 
            type="primary" 
            ghost 
            size="small" 
            icon={<Eye size={14} />} 
            onClick={() => {
              setSelectedDispute(record);
              setIsModalVisible(true);
            }}
          >
            Review
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div className="dispute-management-container p-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 className="mb-1 fw-bold d-flex align-items-center gap-2">
            <Scale size={28} className="text-primary" /> Dispute Management
          </h2>
          <p className="text-muted mb-0">Review and resolve auction-related conflicts</p>
        </div>
      </div>

      {/* Stats Cards */}
      <div className="row mb-4">
        <div className="col-md-4">
          <div className="card stat-card shadow-sm border-0 h-100">
            <div className="card-body">
              <div className="d-flex align-items-center gap-2 mb-2">
                <span className="status-dot bg-secondary"></span>
                <span className="text-muted">Total Disputes</span>
              </div>
              <h2 className="fw-bold mb-0">{stats.total}</h2>
            </div>
          </div>
        </div>
        <div className="col-md-4">
          <div className="card stat-card shadow-sm border-0 h-100 bg-warning-subtle">
            <div className="card-body">
              <div className="d-flex align-items-center gap-2 mb-2">
                <span className="status-dot bg-warning"></span>
                <span className="text-muted text-warning-emphasis">Pending Review</span>
              </div>
              <h2 className="fw-bold mb-0 text-warning-emphasis">{stats.open}</h2>
            </div>
          </div>
        </div>
        <div className="col-md-4">
          <div className="card stat-card shadow-sm border-0 h-100 bg-success-subtle">
            <div className="card-body">
              <div className="d-flex align-items-center gap-2 mb-2">
                <span className="status-dot bg-success"></span>
                <span className="text-muted text-success-emphasis">Resolved</span>
              </div>
              <h2 className="fw-bold mb-0 text-success-emphasis">{stats.resolved}</h2>
            </div>
          </div>
        </div>
      </div>

      {/* Filters and Tabs */}
      <div className="d-flex flex-wrap gap-3 mb-4 align-items-center">
        <Input
          placeholder="Search by ID, Session, or User..."
          prefix={<Search size={16} className="text-muted" />}
          onChange={(e) => setSearchText(e.target.value)}
          style={{ width: 300 }}
          size="large"
        />
        <div className="btn-group shadow-sm" role="group">
          {['All', 'OPEN', 'IN_REVIEW', 'RESOLVED', 'REJECTED'].map((tab) => (
            <button
              key={tab}
              type="button"
              className={`btn ${filterTab === tab ? 'btn-primary' : 'btn-outline-secondary bg-white text-dark border-light-subtle'}`}
              onClick={() => setFilterTab(tab)}
            >
              {tab === 'All' ? 'All Disputes' : tab.replace('_', ' ')}
            </button>
          ))}
        </div>
      </div>

      {/* Table */}
      <div className="bg-white shadow-sm rounded overflow-hidden p-2 border">
        <Table
          columns={columns}
          dataSource={getFilteredDisputes()}
          rowKey="id"
          loading={loading}
          pagination={{ pageSize: 10 }}
        />
      </div>

      {/* Resolution Modal */}
      <Modal
        title={
          <div className="d-flex align-items-center gap-2">
            <Scale size={20} className="text-primary"/> Dispute Details
          </div>
        }
        open={isModalVisible}
        onCancel={() => {
          setIsModalVisible(false);
          setSelectedDispute(null);
          setResolveReason('');
        }}
        footer={null}
        width={600}
      >
        {selectedDispute && (
          <div className="mt-3">
            <div className="bg-light p-3 rounded mb-4 border">
              <div className="row mb-2">
                <div className="col-4 text-muted">Dispute ID</div>
                <div className="col-8 fw-bold">#{selectedDispute.id}</div>
              </div>
              <div className="row mb-2">
                <div className="col-4 text-muted">Session ID</div>
                <div className="col-8">#{selectedDispute.sessionId}</div>
              </div>
              <div className="row mb-2">
                <div className="col-4 text-muted">Raised By</div>
                <div className="col-8">{selectedDispute.raisedByName} (ID: {selectedDispute.raisedById})</div>
              </div>
              <div className="row mb-2">
                <div className="col-4 text-muted">Type</div>
                <div className="col-8"><Tag>{selectedDispute.type}</Tag></div>
              </div>
              <div className="row mb-2">
                <div className="col-4 text-muted">Current Status</div>
                <div className="col-8">{getStatusTag(selectedDispute.status)}</div>
              </div>
              <hr />
              <div className="mb-2">
                <div className="text-muted mb-1">Description from User:</div>
                <div className="p-2 border rounded bg-white">{selectedDispute.description}</div>
              </div>
            </div>

            {(selectedDispute.status === 'RESOLVED' || selectedDispute.status === 'REJECTED') && (
              <div className={`alert ${selectedDispute.status === 'RESOLVED' ? 'alert-success' : 'alert-danger'} d-flex align-items-start gap-2`}>
                {selectedDispute.status === 'RESOLVED' ? <CheckCircle2 size={20} className="mt-1"/> : <XCircle size={20} className="mt-1"/>}
                <div>
                  <strong>Staff Resolution Note:</strong><br/>
                  {selectedDispute.resolution}<br/>
                  <small className="text-muted">Resolved on: {new Date(selectedDispute.resolvedAt).toLocaleString()}</small>
                </div>
              </div>
            )}

            {(selectedDispute.status === 'OPEN' || selectedDispute.status === 'IN_REVIEW') && (
              <Form layout="vertical">
                <Form.Item label={<span className="fw-bold">Resolution Decision & Notes</span>} required>
                  <Input.TextArea
                    rows={4}
                    value={resolveReason}
                    onChange={(e) => setResolveReason(e.target.value)}
                    placeholder="Enter the official reasoning for your decision..."
                  />
                </Form.Item>
                <div className="d-flex justify-content-end gap-3 mt-4">
                  <Button 
                    size="large"
                    danger
                    icon={<XCircle size={16} />}
                    onClick={() => handleResolveAction('REJECTED')}
                    disabled={!resolveReason.trim()}
                  >
                    Reject Dispute
                  </Button>
                  <Button 
                    size="large"
                    type="primary" 
                    className="bg-success" 
                    icon={<CheckCircle2 size={16} />}
                    onClick={() => handleResolveAction('RESOLVED')}
                    disabled={!resolveReason.trim()}
                  >
                    Resolve Dispute
                  </Button>
                </div>
              </Form>
            )}
          </div>
        )}
      </Modal>
    </div>
  );
}
