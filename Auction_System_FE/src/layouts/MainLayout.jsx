import { Link, Outlet, useNavigate } from 'react-router-dom';
import { Navbar, Nav, Container, Button, Form, Dropdown } from 'react-bootstrap';
import { Facebook, Twitter, Instagram, User, LogOut, Package, DollarSign } from 'lucide-react';
import { useContext } from 'react';
import { AuthContext } from '../context/AuthContext';
import AuthModal from '../features/auth/components/AuthModal';

export default function MainLayout() {
  const navigate = useNavigate();
  const { isAuthenticated, user, logout, isAuthModalOpen, openAuthModal, closeAuthModal } = useContext(AuthContext);

  return (
    <div className="d-flex flex-column min-vh-100 bg-light" style={{ border: '3px solid #a855f7' }}>
      {/* Header / Navbar */}
      <Navbar bg="white" expand="lg" className="py-3 sticky-top border-bottom shadow-sm">
        <Container>
          <Navbar.Brand as={Link} to="/" className="fw-bold fs-3 text-dark d-flex align-items-center" style={{ fontStyle: 'italic', position: 'relative' }}>
            <span style={{
              background: 'linear-gradient(180deg, #003d5b 0%, #0077b6 50%, #90e0ef 100%)',
              WebkitBackgroundClip: 'text',
              WebkitTextFillColor: 'transparent',
              letterSpacing: '2px',
              fontWeight: 900,
              fontFamily: 'Impact, sans-serif'
            }}>ANNEXE</span>
          </Navbar.Brand>

          <Navbar.Toggle aria-controls="basic-navbar-nav" />

          <Navbar.Collapse id="basic-navbar-nav" className="justify-content-between">
            <Nav className="mx-auto gap-4 text-dark fw-semibold" style={{ fontSize: '0.9rem' }}>
              <Nav.Link as={Link} to="/" className="text-dark hover-teal">Home</Nav.Link>
              <Nav.Link as={Link} to="/auction" className="text-dark hover-teal">Auction</Nav.Link>
              {/* <Nav.Link as={Link} to="/results" className="text-dark hover-teal">Results</Nav.Link>
              <Nav.Link as={Link} to="/value-buy-sell" className="text-dark hover-teal">Value, Buy, Sell</Nav.Link> */}
              <Nav.Link as={Link} to="/about-us" className="text-dark hover-teal">About Us</Nav.Link>
              <Nav.Link as={Link} to="/contact-us" className="text-dark hover-teal">Contact Us</Nav.Link>
            </Nav>

            <div className="d-flex align-items-center gap-3">
              {isAuthenticated ? (
                <div className="d-flex align-items-center gap-3">
                  {user?.role && user.role !== 'USER' && (
                    <Button
                      variant="dark"
                      className="px-4 py-2 rounded-pill fw-bold"
                      style={{ fontSize: '0.9rem', backgroundColor: '#003d5b', borderColor: '#003d5b' }}
                      onClick={() => navigate('/admin/items')}
                    >
                      Staff Portal
                    </Button>
                  )}
                  <Dropdown align="end">
                    <Dropdown.Toggle
                      variant="light"
                      id="dropdown-user"
                      className="d-flex align-items-center gap-2 rounded-pill border shadow-sm px-3 py-2 bg-white"
                    >
                      <div
                        className="rounded-circle d-flex align-items-center justify-content-center"
                        style={{ width: '28px', height: '28px', backgroundColor: '#003d5b', color: 'white' }}
                      >
                        {user?.email ? user.email.charAt(0).toUpperCase() : <User size={16} />}
                      </div>
                      <span className="fw-semibold text-dark d-none d-md-block" style={{ fontSize: '0.9rem' }}>
                        {user?.email?.split('@')[0] || 'User'}
                      </span>
                    </Dropdown.Toggle>

                    <Dropdown.Menu className="shadow border-0 mt-2 rounded-3" style={{ minWidth: '220px' }}>
                      <Dropdown.Item as={Link} to="/my-account" className="py-2 d-flex align-items-center gap-3">
                        <User size={16} className="text-secondary" />
                        <span style={{ fontSize: '0.9rem', fontWeight: 500 }}>My Profile</span>
                      </Dropdown.Item>
                      <Dropdown.Item as={Link} to="/user/items" className="py-2 d-flex align-items-center gap-3">
                        <Package size={16} className="text-secondary" />
                        <span style={{ fontSize: '0.9rem', fontWeight: 500 }}>My Items</span>
                      </Dropdown.Item>
                      <Dropdown.Item as={Link} to="/user/earnings" className="py-2 d-flex align-items-center gap-3">
                        <DollarSign size={16} className="text-secondary" />
                        <span style={{ fontSize: '0.9rem', fontWeight: 500 }}>My Earnings</span>
                      </Dropdown.Item>

                      <Dropdown.Divider />

                      <Dropdown.Item onClick={logout} className="py-2 d-flex align-items-center gap-3 text-danger">
                        <LogOut size={16} />
                        <span style={{ fontSize: '0.9rem', fontWeight: 500 }}>Logout</span>
                      </Dropdown.Item>
                    </Dropdown.Menu>
                  </Dropdown>
                </div>
              ) : (
                <Button
                  variant="dark"
                  className="px-4 py-2 rounded-pill fw-bold"
                  style={{ fontSize: '0.9rem', backgroundColor: '#003d5b', borderColor: '#003d5b' }}
                  onClick={openAuthModal}
                >
                  Sign In
                </Button>
              )}
            </div>
          </Navbar.Collapse>
        </Container>
      </Navbar>

      {/* Main Content */}
      <main className="flex-grow-1">
        <Outlet />
      </main>

      <AuthModal show={isAuthModalOpen} onHide={closeAuthModal} />

      {/* Footer */}
      <footer className="pt-5 pb-4" style={{ backgroundColor: '#333333', color: '#ffffff' }}>
        <Container>
          <div className="row justify-content-between mb-5">
            {/* Left Column: Brand & Info */}
            <div className="col-lg-5 col-md-6 mb-4 mb-md-0">
              <h2 className="fw-bold mb-4 text-white" style={{ lineHeight: '1.1', fontSize: '2.5rem' }}>Annexe<br />Auction</h2>
              <p className="text-light small opacity-75" style={{ lineHeight: '1.6', maxWidth: '350px' }}>
                Wisma Geha, Jl. Timor No.25, RT.9/RW.4,<br />
                Gondangdia, Kec. Menteng, Kota Jakarta<br />
                Pusat, Daerah Khusus Ibukota Jakarta 10350<br />
                p. 0813-1066-8211<br />
                e. info@annexe-auction.online
              </p>
            </div>

            {/* Right Column: Newsletter & Links */}
            <div className="col-lg-6 col-md-6">
              {/* Newsletter */}
              <div className="mb-5">
                <h6 className="fw-bold text-white mb-3" style={{ fontSize: '1rem' }}>subscribe to our newsletter</h6>
                <Form onSubmit={(e) => e.preventDefault()} className="d-flex align-items-end">
                  <Form.Control
                    placeholder="enter your email address"
                    className="bg-transparent border-0 border-bottom border-light text-white rounded-0 shadow-none px-0 pb-2 me-4"
                    style={{ fontSize: '0.85rem' }}
                  />
                  <Button
                    variant="outline-light"
                    className="px-4 py-1 border-light rounded-0 text-white"
                    style={{ fontSize: '0.85rem' }}
                    type="submit"
                  >
                    Send
                  </Button>
                </Form>
              </div>

              {/* Links */}
              <div className="row mt-4">
                <div className="col-6">
                  <ul className="list-unstyled fw-semibold text-white mb-0 d-flex flex-column gap-3" style={{ fontSize: '0.9rem' }}>
                    <li><Link to="/" className="text-decoration-none text-white" style={{ opacity: 0.9 }}>Consign with us</Link></li>
                    <li><Link to="/" className="text-decoration-none text-white" style={{ opacity: 0.9 }}>Register to bid</Link></li>
                  </ul>
                </div>
                <div className="col-6">
                  <ul className="list-unstyled fw-semibold text-white mb-0 d-flex flex-column gap-3" style={{ fontSize: '0.9rem' }}>
                    <li><Link to="/" className="text-decoration-none text-white" style={{ opacity: 0.9 }}>Terms & Conditions</Link></li>
                    <li><Link to="/" className="text-decoration-none text-white" style={{ opacity: 0.9 }}>Privacy Policy</Link></li>
                  </ul>
                </div>
              </div>
            </div>
          </div>

          {/* Bottom row: Copyright & Socials */}
          <div className="d-flex align-items-center mt-5 pt-3">
            <span className="text-light small opacity-75" style={{ fontSize: '0.8rem', whiteSpace: 'nowrap' }}>
              &copy; 2022 Annexe Auction Online. All rights reserved
            </span>
            <div className="flex-grow-1 border-bottom border-secondary opacity-50 mx-4"></div>
            <div className="d-flex gap-3">
              <Button variant="light" className="rounded-circle p-0 d-flex align-items-center justify-content-center border-0" style={{ width: '32px', height: '32px' }}>
                <Facebook size={16} color="#333" />
              </Button>
              <Button variant="light" className="rounded-circle p-0 d-flex align-items-center justify-content-center border-0" style={{ width: '32px', height: '32px' }}>
                <Twitter size={16} color="#333" />
              </Button>
              <Button variant="light" className="rounded-circle p-0 d-flex align-items-center justify-content-center border-0" style={{ width: '32px', height: '32px' }}>
                <Instagram size={16} color="#333" />
              </Button>
            </div>
          </div>
        </Container>
      </footer>
    </div>
  );
}
