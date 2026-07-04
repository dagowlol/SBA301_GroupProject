import React from 'react';
import { Container, Row, Col } from 'react-bootstrap';
import './AboutUsPage.css';

export default function AboutUsPage() {
  return (
    <div className="about-us-page">
      {/* Top Section */}
      <section className="about-top-section position-relative py-5">
        <div className="dotted-bg-top-left"></div>
        <Container className="position-relative z-1 mt-5">
          <Row className="align-items-center">
            <Col md={5} className="text-md-end text-center pe-md-5 mb-4 mb-md-0">
              <h1 className="fw-bolder display-4" style={{ color: '#2C3539', lineHeight: '1.1' }}>
                About<br />Annexe<br />Auction
              </h1>
            </Col>
            <Col md={7} className="ps-md-5" style={{ fontSize: '15px', color: '#555', lineHeight: '1.8' }}>
              <p>
                Welcome to Annexe Auction, your premier destination for exceptional art, antiques,
                and exclusive collectibles. Established with a passion for connecting connoisseurs
                with extraordinary items, we pride ourselves on curating world-class exhibitions
                and transparent auction experiences. Our dedicated team of experts meticulously evaluates
                each piece, ensuring authenticity and unmatched quality for our global community of collectors.
              </p>
              <p className="mb-0">
                With years of trusted expertise in the industry, we have built a reputation for excellence
                and integrity. Whether you are seeking a rare historical artifact or a contemporary masterpiece,
                our curated selections are designed to inspire and captivate. Discover the art of collecting
                with a partner who values history as much as you do.
              </p>
            </Col>
          </Row>
        </Container>
      </section>

      {/* Middle Section with Large Image */}
      <section className="about-middle-section py-5">
        <Container>
          <Row className="mb-5">
            <Col>
              <div className="large-image-wrapper bg-secondary" style={{ minHeight: '500px', borderRadius: '4px', overflow: 'hidden' }}>
                <img
                  src="https://images.unsplash.com/photo-1605429523419-d828acb941d9?q=80&w=1632&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
                  alt="Không gian trưng bày vật phẩm"
                  className="w-100 h-100 object-fit-cover"
                  style={{ minHeight: '500px' }}
                />
              </div>
            </Col>
          </Row>
          <Row className="justify-content-end" style={{ fontSize: '14px', color: '#555', lineHeight: '1.7' }}>
            <Col md={4} className="mb-4 mb-md-0">
              <p>
                At Annexe Auction, we believe that every artifact carries a unique history waiting
                to be discovered. Our platform bridges the gap between passionate sellers and discerning
                buyers through a seamless, secure, and technologically advanced bidding system. We are
                committed to fostering a trustworthy environment where art enthusiasts can explore, bid,
                and acquire masterpieces with absolute confidence.
              </p>
            </Col>
            <Col md={4}>
              <p>
                Our vision extends beyond traditional auctioning. We aim to cultivate a vibrant community
                where history is preserved and celebrated. Whether you are a seasoned collector or participating
                in your first auction, we provide comprehensive support, transparent appraisals, and an
                unforgettable journey into the world of fine art and rare antiquities.
              </p>
            </Col>
          </Row>
        </Container>
      </section>

      {/* Bottom Section */}
      <section className="about-bottom-section position-relative py-5 mb-5">
        <Container>
          <Row>
            <Col md={6} className="position-relative pe-md-5 mb-5 mb-md-0">
              <div className="dotted-bg-circle"></div>
              <div className="position-relative z-1 pt-5">
                <h2 className="fw-bold mb-4" style={{ fontSize: '2.5rem', color: '#000' }}>Our Expertise</h2>
                <hr style={{ width: '100%', backgroundColor: '#000', height: '1px', border: 'none', marginBottom: '2rem' }} />
                <div style={{ fontSize: '15px', color: '#555', lineHeight: '1.8' }}>
                  <p>
                    Our specialists bring decades of experience across various disciplines, including fine art,
                    jewelry, rare books, and vintage collectibles. This extensive knowledge allows us to source
                    hidden gems from around the globe and present them in our exclusive curated events. We are
                    not just auctioneers; we are custodians of history.
                  </p>
                  <p>
                    We leverage cutting-edge technology to offer real-time, global bidding, ensuring that distance
                    is never a barrier to acquiring your next masterpiece. Join us at our next exhibition or online
                    session, and experience the thrill of the auction with Annexe Auction—where every bid tells a story.
                  </p>
                </div>
              </div>
            </Col>
            <Col md={6}>
              <div className="side-image-wrapper h-100 bg-secondary" style={{ minHeight: '400px', borderRadius: '4px', overflow: 'hidden' }}>
                <img
                  src="https://plus.unsplash.com/premium_photo-1706571538582-f40bad685f68?q=80&w=715&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
                  alt="Đồ vật đấu giá trên bục trưng bày"
                  className="w-100 h-100 object-fit-cover"
                />
              </div>
            </Col>
          </Row>
        </Container>
      </section>
    </div>
  );
}
