import { Link, Outlet, useNavigate } from 'react-router-dom';
import { Navbar, Nav, Container, Button, Form, InputGroup } from 'react-bootstrap';
import { Search, Mail, ArrowRight, Compass, Heart, Award, Shield } from 'lucide-react';

export default function MainLayout() {
  const navigate = useNavigate();

  return (
    <div className="d-flex flex-column min-vh-100 bg-light">
      {/* Header / Navbar */}
      <Navbar bg="white" expand="lg" className="border-bottom py-3 sticky-top shadow-sm">
        <Container>
          <Navbar.Brand as={Link} to="/" className="fw-bold fs-3 tracking-wider text-dark d-flex align-items-center">
            <span style={{ color: '#005f73', letterSpacing: '1px' }}>ANNEXE</span>
            <span className="ms-1 fw-light text-muted fs-6 uppercase">Auction</span>
          </Navbar.Brand>
          
          <Navbar.Toggle aria-controls="basic-navbar-nav" />
          
          <Navbar.Collapse id="basic-navbar-nav" className="justify-content-between">
            <Nav className="mx-auto gap-3 text-uppercase fw-semibold" style={{ fontSize: '0.85rem' }}>
              <Nav.Link as={Link} to="/" className="text-dark hover-teal">Home</Nav.Link>
              <Nav.Link as={Link} to="/" className="text-dark hover-teal">Auction</Nav.Link>
              <Nav.Link as={Link} to="/" className="text-dark hover-teal">Results</Nav.Link>
              <Nav.Link as={Link} to="/" className="text-dark hover-teal">Value, Buy, Sell</Nav.Link>
              <Nav.Link as={Link} to="/" className="text-dark hover-teal">About Us</Nav.Link>
              <Nav.Link as={Link} to="/" className="text-dark hover-teal">Contact Us</Nav.Link>
            </Nav>
            
            <div className="d-flex align-items-center gap-3">
              <Button 
                variant="outline-dark" 
                size="sm" 
                className="rounded-circle p-2 border-0" 
                aria-label="Search"
              >
                <Search size={18} />
              </Button>
              <Button 
                variant="dark" 
                className="px-4 py-2 rounded-pill font-semibold text-uppercase" 
                style={{ fontSize: '0.8rem', backgroundColor: '#004e64', borderColor: '#004e64' }}
                onClick={() => navigate('/admin/items')}
              >
                Staff Portal
              </Button>
            </div>
          </Navbar.Collapse>
        </Container>
      </Navbar>

      {/* Main Content */}
      <main className="flex-grow-1">
        <Outlet />
      </main>

      {/* Footer */}
      <footer className="text-white pt-5 pb-3" style={{ backgroundColor: '#2b2d31' }}>
        <Container>
          <div className="row g-4 mb-5">
            {/* Left Column: Brand & Info */}
            <div className="col-lg-4 col-md-6">
              <h5 className="fw-bold mb-3 text-uppercase tracking-wider">Annexe Auction</h5>
              <p className="text-muted small mb-4" style={{ lineHeight: '1.6' }}>
                Wisma Geha, Jl. Timor No.25, RT.9/RW.4,<br />
                Gondangdia, Kec. Menteng, Kota Jakarta Pusat,<br />
                Daerah Khusus Ibukota Jakarta 10350<br />
                p. 0813-1055-1511<br />
                e. info@annexeauctions.com
              </p>
              <div className="d-flex gap-2">
                <Button variant="outline-secondary" size="sm" className="rounded-circle p-2 border-muted text-white">
                  <Compass size={16} />
                </Button>
                <Button variant="outline-secondary" size="sm" className="rounded-circle p-2 border-muted text-white">
                  <Heart size={16} />
                </Button>
                <Button variant="outline-secondary" size="sm" className="rounded-circle p-2 border-muted text-white">
                  <Award size={16} />
                </Button>
                <Button variant="outline-secondary" size="sm" className="rounded-circle p-2 border-muted text-white">
                  <Shield size={16} />
                </Button>
              </div>
            </div>

            {/* Middle Column: Links */}
            <div className="col-lg-4 col-md-6">
              <div className="row">
                <div className="col-6">
                  <h6 className="fw-bold text-uppercase mb-3 small tracking-wide">Services</h6>
                  <ul className="list-unstyled small d-flex flex-column gap-2 text-muted">
                    <li><Link to="/" className="text-decoration-none text-reset hover-white">Consign with us</Link></li>
                    <li><Link to="/" className="text-decoration-none text-reset hover-white">Register to bid</Link></li>
                    <li><Link to="/" className="text-decoration-none text-reset hover-white">Auctions list</Link></li>
                    <li><Link to="/" className="text-decoration-none text-reset hover-white">Valuation</Link></li>
                  </ul>
                </div>
                <div className="col-6">
                  <h6 className="fw-bold text-uppercase mb-3 small tracking-wide">Legal</h6>
                  <ul className="list-unstyled small d-flex flex-column gap-2 text-muted">
                    <li><Link to="/" className="text-decoration-none text-reset hover-white">Terms & Conditions</Link></li>
                    <li><Link to="/" className="text-decoration-none text-reset hover-white">Privacy Policy</Link></li>
                    <li><Link to="/" className="text-decoration-none text-reset hover-white">Buyer Premium</Link></li>
                    <li><Link to="/" className="text-decoration-none text-reset hover-white">FAQ</Link></li>
                  </ul>
                </div>
              </div>
            </div>

            {/* Right Column: Newsletter */}
            <div className="col-lg-4 col-md-12">
              <h6 className="fw-bold text-uppercase mb-3 small tracking-wide">subscribe to our newsletter</h6>
              <p className="text-muted small mb-3">Stay updated with our latest auction collections and hot bids.</p>
              <Form onSubmit={(e) => e.preventDefault()}>
                <InputGroup className="mb-3">
                  <Form.Control
                    placeholder="enter your email address"
                    aria-label="Email address"
                    className="bg-transparent border-secondary text-white placeholder-muted py-2"
                    style={{ fontSize: '0.9rem', borderColor: '#495057' }}
                  />
                  <Button 
                    variant="outline-light"
                    className="px-3 border-secondary"
                    style={{ backgroundColor: '#495057', borderColor: '#495057' }}
                    type="submit"
                  >
                    <ArrowRight size={18} />
                  </Button>
                </InputGroup>
              </Form>
            </div>
          </div>

          <hr className="border-secondary mb-3" />

          <div className="d-flex flex-column flex-md-row justify-content-between align-items-center gap-2 small text-muted">
            <span>&copy; {new Date().getFullYear()} Annexe Auction. All rights reserved.</span>
            <div className="d-flex gap-3">
              <Link to="/" className="text-decoration-none text-reset hover-white">Facebook</Link>
              <Link to="/" className="text-decoration-none text-reset hover-white">Instagram</Link>
              <Link to="/" className="text-decoration-none text-reset hover-white">LinkedIn</Link>
            </div>
          </div>
        </Container>
      </footer>
    </div>
  );
}
