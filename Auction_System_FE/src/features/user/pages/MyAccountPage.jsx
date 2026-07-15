import React, { useState, useEffect, useContext } from 'react';
import { Container, Row, Col, Nav, Card, Spinner, Button } from 'react-bootstrap';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { AuthContext } from '../../../context/AuthContext';
import { userApi } from '../../../api/userApi';
import { LogOut, User, Gavel, Upload, DollarSign, Settings, MapPin, Key } from 'lucide-react';
import ChangePasswordModal from './ChangePasswordModal';
import EditAddressTab from './EditAddressTab';
import AuctionItemsTab from './AuctionItemsTab';

const TABS = [
  { id: 'auction-item', label: 'Auction Item', icon: Gavel },
  { id: 'upload-item', label: 'Upload item', icon: Upload },
  { id: 'earning-report', label: 'Earning Report', icon: DollarSign },
  { id: 'account-setting', label: 'Account Setting', icon: Settings },
  { id: 'edit-address', label: 'Edit Address', icon: MapPin },
];

export default function MyAccountPage() {
  const { user, logout } = useContext(AuthContext);
  const [searchParams, setSearchParams] = useSearchParams();
  const navigate = useNavigate();
  
  const currentTab = searchParams.get('tab') || 'auction-item';
  const [userInfo, setUserInfo] = useState(null);
  const [loading, setLoading] = useState(true);
  const [showPasswordModal, setShowPasswordModal] = useState(false);

  useEffect(() => {
    const fetchUser = async () => {
      if (user?.id) {
        try {
          const res = await userApi.getUserById(user.id);
          setUserInfo(res);
        } catch (error) {
          console.error("Failed to fetch user details", error);
        } finally {
          setLoading(false);
        }
      } else {
        setLoading(false);
      }
    };
    fetchUser();
  }, [user]);

  const handleTabChange = (tabId) => {
    setSearchParams({ tab: tabId });
  };

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  // Render content based on current tab
  const renderTabContent = () => {
    switch (currentTab) {
      case 'auction-item':
        return <AuctionItemsTab />;
      case 'upload-item':
        return <div><h4>Upload Item</h4><p className="text-muted">Content for Upload Item will be implemented here.</p></div>;
      case 'earning-report':
        return <div><h4>Earning Report</h4><p className="text-muted">Content for Earning Report will be implemented here.</p></div>;
      case 'account-setting':
        return <div><h4>Account Setting</h4><p className="text-muted">Content for Account Setting will be implemented here.</p></div>;
      case 'edit-address':
        return <EditAddressTab user={user} />;
      default:
        return <div>Tab not found</div>;
    }
  };

  return (
    <div className="bg-light py-5 min-vh-100">
      <Container>
        {/* Banner Section */}
        <div className="mb-4 pb-3 border-bottom border-secondary-subtle">
          <h2 className="fw-bold mb-0" style={{ color: '#004e64' }}>My account</h2>
        </div>

        <Row className="gy-4">
          {/* Sidebar */}
          <Col lg={3} md={4}>
            <Card className="border-0 shadow-sm mb-4">
              <Card.Body className="text-center p-4">
                {/* User Info - Placeholder for Avatar */}
                <div 
                  className="rounded-circle bg-secondary-subtle d-inline-flex align-items-center justify-content-center mb-3"
                  style={{ width: '80px', height: '80px' }}
                >
                  <User size={40} className="text-secondary" />
                </div>
                
                {loading ? (
                  <Spinner animation="border" size="sm" />
                ) : userInfo ? (
                  <>
                    <h5 className="fw-bold mb-1">
                      {userInfo.firstName} {userInfo.lastName}
                    </h5>
                    <p className="text-muted small mb-3">{userInfo.email}</p>
                  </>
                ) : (
                  <>
                    <h5 className="fw-bold mb-1">Unknown User</h5>
                    <p className="text-muted small mb-3">No email found</p>
                  </>
                )}
                
                <div className="d-flex flex-column gap-2 mt-3">
                  <Button 
                    variant="outline-primary" 
                    size="sm" 
                    className="w-100 d-flex align-items-center justify-content-center gap-2"
                    onClick={() => setShowPasswordModal(true)}
                  >
                    <Key size={16} /> Change Password
                  </Button>
                  <Button 
                    variant="outline-danger" 
                    size="sm" 
                    className="w-100 d-flex align-items-center justify-content-center gap-2"
                    onClick={handleLogout}
                  >
                    <LogOut size={16} /> Logout
                  </Button>
                </div>
              </Card.Body>
            </Card>

            {/* Navigation Tabs */}
            <Card className="border-0 shadow-sm overflow-hidden">
              <Nav className="flex-column">
                {TABS.map((tab) => {
                  const Icon = tab.icon;
                  const isActive = currentTab === tab.id;
                  return (
                    <Nav.Link
                      key={tab.id}
                      onClick={() => handleTabChange(tab.id)}
                      className={`d-flex align-items-center gap-3 py-3 px-4 ${isActive ? 'active-sidebar' : 'text-dark hover-teal'}`}
                      style={{
                        borderLeft: isActive ? '4px solid #004e64' : '4px solid transparent',
                        backgroundColor: isActive ? 'rgba(0, 78, 100, 0.08)' : 'transparent',
                        color: isActive ? '#004e64' : '#2b2d42',
                        fontWeight: isActive ? '600' : '500',
                        transition: 'all 0.2s ease',
                        cursor: 'pointer'
                      }}
                    >
                      <Icon size={18} className={isActive ? 'text-teal' : 'text-secondary'} style={{ color: isActive ? '#004e64' : 'inherit' }} />
                      {tab.label}
                    </Nav.Link>
                  );
                })}
              </Nav>
            </Card>
          </Col>

          {/* Main Content Area */}
          <Col lg={9} md={8}>
            <Card className="border-0 shadow-sm h-100">
              <Card.Body className="p-4 p-lg-5">
                {renderTabContent()}
              </Card.Body>
            </Card>
          </Col>
        </Row>
      </Container>
      
      {/* Change Password Modal */}
      <ChangePasswordModal 
        show={showPasswordModal} 
        onHide={() => setShowPasswordModal(false)} 
      />
    </div>
  );
}
