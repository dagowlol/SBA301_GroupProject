import { useContext, useState } from 'react';
import { Container, Row, Col, Button } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../../../context/AuthContext';
import '../styles/home.css';
import valueImg from '../../../assets/images/Home.png';

export default function HomePage() {
  const navigate = useNavigate();
  const { isAuthenticated, openAuthModal } = useContext(AuthContext);
  const [activeTab, setActiveTab] = useState('announcement');

  const tabs = [
    { 
      id: 'announcement', 
      title: "10% Buyer's Pre..", 
      subtitle: "Announcement",
      content: {
        type: 'promo',
        titleLeft: <>10% off<br />Buyer's<br />Premium</>,
        titleRight: <>for your first<br />purchase by<br />completing your<br />account info</>,
        btnText: 'Register to redeem'
      }
    },
    { 
      id: 'upcoming_1', 
      title: "Mona Lisa", 
      subtitle: "Upcoming item",
      content: {
        type: 'item',
        title: 'Mona Lisa',
        author: 'by Leonardo Da Vinci',
        meta: 'available to bid on 05.09.22',
        btnText: 'View item'
      }
    },
    { 
      id: 'bid_1', 
      title: "Greek woman..", 
      subtitle: "Bid item",
      content: {
        type: 'item',
        title: 'Greek woman with baskets',
        author: 'by Steven Alvs',
        meta: <>bid start from<br/><strong className="fs-5 text-dark">$500 &nbsp;|&nbsp; SGD700 &nbsp;|&nbsp; IDR7.500.000</strong></>,
        btnText: 'View item'
      }
    },
    { 
      id: 'bid_2', 
      title: "Plant & Pots", 
      subtitle: "Bid item",
      content: {
        type: 'item',
        title: 'Plant & Pots',
        author: 'by Jose Guillermo',
        meta: <>bid start from<br/><strong className="fs-5 text-dark">$500 &nbsp;|&nbsp; SGD700 &nbsp;|&nbsp; IDR7.500.000</strong></>,
        btnText: 'View item'
      }
    }
  ];

  const currentContent = tabs.find(t => t.id === activeTab).content;

  return (
    <div className="homepage-wrapper">
      {/* Hero Section */}
      <section className="home-hero">
        <div className="home-hero-bg"></div>
        <div className="halftone-circle"></div>
        
        <div className="flex-grow-1 d-flex align-items-center w-100 position-relative">
          <Container className="position-relative z-3">
            <div className="home-hero-content">
              {currentContent.type === 'promo' ? (
                <>
                  <div className="hero-left-col glass-box tilt-3d" style={{ padding: '40px', borderRadius: '24px' }}>
                    <div className="hero-title-left">{currentContent.titleLeft}</div>
                    <button 
                      className="btn-register-redeem"
                      onClick={() => isAuthenticated ? navigate('/admin/items') : openAuthModal()}
                    >
                      {isAuthenticated ? 'Go to Profile' : currentContent.btnText}
                    </button>
                  </div>
                  <div className="tilt-3d">
                    <div className="hero-title-right glass-box" style={{ padding: '30px', borderRadius: '16px' }}>{currentContent.titleRight}</div>
                  </div>
                </>
              ) : (
                <div className="hero-item-content tilt-3d glass-box" style={{ padding: '40px', borderRadius: '24px' }}>
                  <div className="hero-left-col">
                    <h1 className="hero-item-title">{currentContent.title}</h1>
                    <p className="hero-item-author">{currentContent.author}</p>
                    <button 
                      className="btn-register-redeem"
                      onClick={() => navigate('/auction')}
                    >
                      {currentContent.btnText}
                    </button>
                  </div>
                  <div className="hero-item-meta-col">
                    <p className="hero-item-meta">{currentContent.meta}</p>
                  </div>
                </div>
              )}
            </div>
          </Container>
        </div>

        {/* Overlapping Tabs */}
        <Container className="position-relative z-3">
          <div className="hero-tabs-container">
            {tabs.map(tab => (
              <button 
                key={tab.id}
                className={`hero-tab ${activeTab === tab.id ? 'active' : ''}`}
                onClick={() => setActiveTab(tab.id)}
              >
                <div className="hero-tab-title">{tab.title}</div>
                <div className="hero-tab-subtitle">{tab.subtitle}</div>
              </button>
            ))}
          </div>
        </Container>
      </section>

      {/* Value, Buy, Sell Section */}
      <section className="value-section">
        <Row className="g-0 align-items-center h-100">
          <Col md={6} className="value-img-wrapper">
            <img 
              src={valueImg} 
              alt="Value, Buy, Sell" 
              className="value-img"
            />
          </Col>
          <Col md={6} className="value-content-wrapper">
            <h2 className="value-title">Value,<br />Buy, Sell</h2>
            <p className="value-desc">
              Lorem ipsum dolor sit amet, consectetur adipiscing elit. 
              Pulvinar interdum enim a vestibulum, nunc cras. Gravida morbi sit 
              sed egestas cursus risus imperdiet bibendum nisl enim.
            </p>
            <div className="value-buttons">
              <button className="btn-value-outline" onClick={() => navigate('/auction')}>Bid your item</button>
              <button className="btn-value-outline" onClick={() => isAuthenticated ? navigate('/admin/items') : openAuthModal()}>Become a Seller</button>
            </div>
          </Col>
        </Row>
      </section>
    </div>
  );
}
