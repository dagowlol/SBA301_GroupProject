import { useContext, useEffect, useMemo, useState } from 'react';
import { Alert, Col, Container, Row, Spinner } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../../../context/AuthContext';
import { useHomeAuctions } from '../hooks/useHomeAuctions';
import '../styles/home.css';
import valueImg from '../../../assets/images/Home.png';

const PROMOTION_TAB = {
  id: 'announcement',
  title: 'Welcome offer',
  subtitle: 'Announcement',
  content: {
    type: 'promo',
    titleLeft: <>Discover<br />Rare<br />Treasures</>,
    titleRight: <>Join live auctions,<br />place secure bids,<br />and sell your<br />valued collection.</>,
    btnText: 'Complete your profile',
  },
};

const formatCurrency = (amount) => {
  if (amount === null || amount === undefined) return 'Contact for price';
  return `${new Intl.NumberFormat('en-US').format(Number(amount))} VND`;
};

const formatDateTime = (value) => {
  if (!value) return 'Schedule to be announced';
  return new Intl.DateTimeFormat('en-GB', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value));
};

const truncate = (value, maxLength = 22) => {
  if (!value || value.length <= maxLength) return value || 'Untitled auction';
  return `${value.slice(0, maxLength - 1)}…`;
};

const toAuctionTab = (session) => {
  const active = session.status === 'ACTIVE';
  const price = session.currentHighestBid ?? session.reservePrice;

  return {
    id: `session-${session.id}`,
    title: truncate(session.itemName),
    subtitle: active ? 'Live auction' : 'Upcoming auction',
    content: {
      type: 'item',
      sessionId: session.id,
      status: session.status,
      title: session.itemName || 'Untitled auction',
      description: session.itemDescription || 'A curated item from our upcoming collection.',
      metaLabel: active ? 'Current bid' : 'Auction starts',
      metaValue: active ? formatCurrency(price) : formatDateTime(session.startTime),
      btnText: active ? 'Join live auction' : 'View auction',
    },
  };
};

export default function HomePage() {
  const navigate = useNavigate();
  const { isAuthenticated, openAuthModal } = useContext(AuthContext);
  const [activeTab, setActiveTab] = useState(PROMOTION_TAB.id);
  const { data: sessions = [], isLoading, isError, refetch } = useHomeAuctions();

  const tabs = useMemo(
    () => [PROMOTION_TAB, ...sessions.map(toAuctionTab)],
    [sessions],
  );

  useEffect(() => {
    if (!tabs.some((tab) => tab.id === activeTab)) {
      setActiveTab(PROMOTION_TAB.id);
    }
  }, [activeTab, tabs]);

  const currentContent = tabs.find((tab) => tab.id === activeTab)?.content ?? PROMOTION_TAB.content;

  const handleProfile = () => {
    if (isAuthenticated) navigate('/my-account');
    else openAuthModal();
  };

  const handleSellItem = () => {
    if (isAuthenticated) navigate('/user/items/create');
    else openAuthModal();
  };

  const openAuction = (content) => {
    navigate(content.status === 'ACTIVE'
      ? `/auction/${content.sessionId}`
      : `/product/${content.sessionId}`);
  };

  return (
    <div className="homepage-wrapper">
      <section className="home-hero">
        <div className="home-hero-bg" />
        <div className="halftone-circle" />

        <div className="flex-grow-1 d-flex align-items-center w-100 position-relative">
          <Container className="position-relative z-3">
            <div className="home-hero-content">
              {currentContent.type === 'promo' ? (
                <>
                  <div className="hero-left-col glass-box tilt-3d">
                    <div className="hero-title-left">{currentContent.titleLeft}</div>
                    <button className="btn-register-redeem" onClick={handleProfile} type="button">
                      {isAuthenticated ? 'View my account' : currentContent.btnText}
                    </button>
                  </div>
                  <div className="tilt-3d">
                    <div className="hero-title-right glass-box">{currentContent.titleRight}</div>
                  </div>
                </>
              ) : (
                <div className="hero-item-content tilt-3d glass-box">
                  <div className="hero-left-col">
                    <span className={`hero-status hero-status--${currentContent.status.toLowerCase()}`}>
                      {currentContent.status === 'ACTIVE' ? 'Live now' : 'Upcoming'}
                    </span>
                    <h1 className="hero-item-title">{currentContent.title}</h1>
                    <p className="hero-item-author">{currentContent.description}</p>
                    <button
                      className="btn-register-redeem"
                      onClick={() => openAuction(currentContent)}
                      type="button"
                    >
                      {currentContent.btnText}
                    </button>
                  </div>
                  <div className="hero-item-meta-col">
                    <p className="hero-item-meta">
                      {currentContent.metaLabel}<br />
                      <strong>{currentContent.metaValue}</strong>
                    </p>
                  </div>
                </div>
              )}
            </div>
          </Container>
        </div>

        <Container className="position-relative z-3">
          {isError && (
            <Alert variant="warning" className="home-auction-alert">
              Auctions could not be loaded.{' '}
              <button type="button" className="home-retry-button" onClick={() => refetch()}>Try again</button>
            </Alert>
          )}
          <div className="hero-tabs-container" aria-label="Featured auctions">
            {tabs.map((tab) => (
              <button
                key={tab.id}
                className={`hero-tab ${activeTab === tab.id ? 'active' : ''}`}
                onClick={() => setActiveTab(tab.id)}
                type="button"
              >
                <div className="hero-tab-title">{tab.title}</div>
                <div className="hero-tab-subtitle">{tab.subtitle}</div>
              </button>
            ))}
            {isLoading && (
              <div className="home-tabs-loading" role="status">
                <Spinner animation="border" size="sm" />
                <span>Loading auctions…</span>
              </div>
            )}
          </div>
        </Container>
      </section>

      <section className="value-section">
        <Row className="g-0 align-items-center h-100">
          <Col md={6} className="value-img-wrapper">
            <img src={valueImg} alt="Auction collection preview" className="value-img" loading="lazy" />
          </Col>
          <Col md={6} className="value-content-wrapper">
            <h2 className="value-title">Value,<br />Buy, Sell</h2>
            <p className="value-desc">
              Discover curated collectibles, compete transparently in real-time auctions,
              or submit your own valuable items for professional review and sale.
            </p>
            <div className="value-buttons">
              <button className="btn-value-outline" onClick={() => navigate('/auction')} type="button">
                Explore auctions
              </button>
              <button className="btn-value-outline" onClick={handleSellItem} type="button">
                Sell an item
              </button>
            </div>
          </Col>
        </Row>
      </section>
    </div>
  );
}
