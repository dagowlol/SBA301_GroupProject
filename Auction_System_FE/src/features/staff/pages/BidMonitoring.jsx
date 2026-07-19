import React, { useState, useEffect } from 'react';
import { Table, Input, Button, Modal, Space, Alert, message, Popconfirm } from 'antd';
import { Eye, AlertTriangle, Radio, Trash2 } from 'lucide-react';
import { fetchBidLogs, cancelBidApi } from '../services/bidMonitoringApi';
import 'bootstrap/dist/css/bootstrap.min.css';
import './BidMonitoring.css';

export default function BidMonitoring() {
  const [bids, setBids] = useState([]);
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({ totalBids: 0, suspiciousBids: 0 });
  
  const [searchText, setSearchText] = useState('');
  const [filterTab, setFilterTab] = useState('AllBids'); // 'AllBids', 'Suspicious'
  
  const [selectedBid, setSelectedBid] = useState(null);
  const [isModalVisible, setIsModalVisible] = useState(false);

  const loadData = async () => {
    setLoading(true);
    try {
      const response = await fetchBidLogs(0, 100);
      const bidsData = response?.content || [];
      
      setBids(bidsData);

      setStats({
        totalBids: bidsData.length,
        suspiciousBids: bidsData.filter(b => b.isSuspicious && b.status !== 'CANCELLED').length,
      });
    } catch (error) {
      console.error('Error loading bid logs:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
    const interval = setInterval(loadData, 30000);
    return () => clearInterval(interval);
  }, []);

  const handleCancelBid = async (bidId) => {
    try {
      await cancelBidApi(bidId);
      message.success('Bid has been cancelled successfully');
      loadData();
    } catch (error) {
      console.error('Error cancelling bid:', error);
      message.error(error.response?.data?.message || 'Failed to cancel bid');
    }
  };

  // ----- Filtering -----
  const getFilteredBids = () => {
    return bids.filter(bid => {
      const matchesSearch = 
        String(bid.sessionId).includes(searchText) || 
        String(bid.bidderName).toLowerCase().includes(searchText.toLowerCase()) ||
        String(bid.userId).includes(searchText);

      if (filterTab === 'Suspicious') return matchesSearch && bid.isSuspicious;
      return matchesSearch;
    });
  };

  // ----- Columns -----
  const bidColumns = [
    {
      title: 'Time',
      dataIndex: 'bidTime',
      key: 'bidTime',
      render: (text) => <span className="text-muted">{new Date(text).toLocaleString()}</span>,
    },
    {
      title: 'Session ID',
      dataIndex: 'sessionId',
      key: 'sessionId',
      render: (text) => <strong>#{text}</strong>,
    },
    {
      title: 'Bidder',
      dataIndex: 'bidderName',
      key: 'bidderName',
    },
    {
      title: 'Amount',
      dataIndex: 'amount',
      key: 'amount',
      render: (val) => <strong>${val}</strong>,
    },
    {
      title: 'Status',
      key: 'status',
      render: (_, record) => {
        if (record.status === 'CANCELLED') {
          return (
            <span className="text-secondary text-decoration-line-through d-flex align-items-center gap-1">
              <span className="status-dot bg-secondary opacity-50"></span> Cancelled
            </span>
          );
        }
        if (record.isSuspicious) {
          return (
            <span className="text-danger fw-bold d-flex align-items-center gap-1">
              <span className="status-dot bg-danger"></span> Suspicious
            </span>
          );
        }
        return (
          <span className="text-muted d-flex align-items-center gap-1">
            <span className="status-dot bg-secondary"></span> Normal
          </span>
        );
      },
    },
    {
      title: 'Actions',
      key: 'actions',
      render: (_, record) => (
        <Space size="middle">
          <Eye
            size={18}
            className="action-icon text-primary cursor-pointer"
            onClick={() => {
              setSelectedBid(record);
              setIsModalVisible(true);
            }}
          />
          {record.isSuspicious && record.status !== 'CANCELLED' && (
            <Popconfirm
              title="Cancel this suspicious bid?"
              description="Are you sure you want to cancel this bid? This will recalculate the current price."
              onConfirm={() => handleCancelBid(record.bidId)}
              okText="Yes, Cancel"
              cancelText="No"
              okButtonProps={{ danger: true }}
            >
              <Trash2
                size={18}
                className="action-icon text-danger cursor-pointer"
              />
            </Popconfirm>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div className="bid-monitoring-container p-4">
      <div className="d-flex justify-content-between align-items-center mb-3">
        <div>
          <h2 className="mb-1 fw-bold">Bid Monitoring</h2>
          <p className="text-muted mb-0">Live bid logs and suspicious activity detection</p>
        </div>
        <div>
          <span className="badge bg-success live-badge d-flex align-items-center gap-2 px-3 py-2">
            <Radio size={16} /> LIVE
          </span>
        </div>
      </div>

      {stats.suspiciousBids > 0 && (
        <div className="alert alert-danger d-flex align-items-center gap-2 border-0 bg-danger-subtle text-danger mb-4">
          <AlertTriangle size={20} />
          <strong>{stats.suspiciousBids} suspicious bids</strong> require attention
        </div>
      )}

      {/* Stats Cards */}
      <div className="row mb-4">
        <div className="col-md-6">
          <div className="card stat-card shadow-sm border-0 h-100">
            <div className="card-body">
              <div className="d-flex align-items-center gap-2 mb-2">
                <span className="status-dot bg-dark"></span>
                <span className="text-muted">Total Bids</span>
              </div>
              <h2 className="fw-bold mb-0">{stats.totalBids}</h2>
            </div>
          </div>
        </div>
        <div className="col-md-6">
          <div className="card stat-card shadow-sm border-0 h-100">
            <div className="card-body">
              <div className="d-flex align-items-center gap-2 mb-2">
                <span className="status-dot bg-danger"></span>
                <span className="text-muted">Suspicious Bids</span>
              </div>
              <h2 className="fw-bold mb-0">{stats.suspiciousBids}</h2>
            </div>
          </div>
        </div>
      </div>

      {/* Filters and Tabs */}
      <div className="d-flex flex-wrap gap-3 mb-4 align-items-center">
        <Input.Search
          placeholder="Search Session ID or User..."
          onChange={(e) => setSearchText(e.target.value)}
          style={{ width: 300 }}
          size="large"
        />
        <div className="btn-group shadow-sm" role="group">
          <button
            type="button"
            className={`btn ${filterTab === 'AllBids' ? 'btn-dark' : 'btn-outline-secondary bg-white text-dark border-light-subtle'}`}
            onClick={() => setFilterTab('AllBids')}
          >
            All Bids
          </button>
          <button
            type="button"
            className={`btn ${filterTab === 'Suspicious' ? 'btn-danger' : 'btn-outline-danger bg-white text-danger border-light-subtle'}`}
            onClick={() => setFilterTab('Suspicious')}
          >
            Suspicious
          </button>
        </div>
      </div>

      {/* Table */}
      <div className="bg-white shadow-sm rounded overflow-hidden">
        <Table
          columns={bidColumns}
          dataSource={getFilteredBids()}
          rowKey="bidId"
          loading={loading}
          pagination={{ pageSize: 10 }}
          rowClassName={(record) => (record.isSuspicious ? 'suspicious-row' : '')}
        />
      </div>

      {/* Bid Details Modal */}
      <Modal
        title="Bid Details"
        open={isModalVisible}
        onCancel={() => {
          setIsModalVisible(false);
          setSelectedBid(null);
        }}
        footer={null}
      >
        {selectedBid && (
          <div>
            <div className="mb-3">
              <p><strong>Session ID:</strong> {selectedBid.sessionId}</p>
              <p><strong>Bidder Name:</strong> {selectedBid.bidderName}</p>
              <p><strong>User ID:</strong> {selectedBid.userId}</p>
              <p><strong>Amount:</strong> ${selectedBid.amount}</p>
              <p><strong>Time:</strong> {new Date(selectedBid.bidTime).toLocaleString()}</p>
              <p>
                <strong>Status:</strong>{' '}
                {selectedBid.status === 'CANCELLED' ? (
                  <span className="text-secondary text-decoration-line-through fw-bold">Cancelled</span>
                ) : (
                  <span className={selectedBid.isSuspicious ? 'text-danger fw-bold' : 'text-success'}>
                    {selectedBid.isSuspicious ? 'Suspicious' : 'Normal'}
                  </span>
                )}
              </p>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}
