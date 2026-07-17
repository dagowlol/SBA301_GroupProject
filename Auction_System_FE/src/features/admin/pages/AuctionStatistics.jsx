import { useMemo, useState } from 'react';
import {
  CalendarOutlined,
  DollarOutlined,
  PlayCircleOutlined,
  RiseOutlined,
  UserAddOutlined,
} from '@ant-design/icons';
import { Alert, Button, Card, Col, DatePicker, Empty, Row, Segmented, Select, Skeleton, Spin, Statistic, Typography } from 'antd';
import dayjs from 'dayjs';
import {
  ArcElement,
  CategoryScale,
  Chart as ChartJS,
  Filler,
  Legend,
  LinearScale,
  LineElement,
  PointElement,
  Tooltip,
} from 'chart.js';
import { Doughnut, Line } from 'react-chartjs-2';
import { useAuctionStatistics } from '../hooks/useAuctionStatistics';
import './AuctionStatistics.css';

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, ArcElement, Filler, Tooltip, Legend);

const { RangePicker } = DatePicker;
const { Title, Text } = Typography;
const COLORS = ['#006d75', '#2f7ea1', '#0f9f83', '#d99132', '#6f6ac8', '#d15f76', '#4f8a62', '#8a6846'];

const formatDate = (date) => date.format('YYYY-MM-DD');
const formatVnd = (value) => new Intl.NumberFormat('en-US', {
  style: 'currency',
  currency: 'VND',
  maximumFractionDigits: 0,
}).format(Number(value || 0));
const shortCurrency = (value) => {
  const number = Number(value || 0);
  if (number >= 1_000_000_000) return `${(number / 1_000_000_000).toLocaleString('en-US')}B`;
  if (number >= 1_000_000) return `${(number / 1_000_000).toLocaleString('en-US')}M`;
  if (number >= 1_000) return `${(number / 1_000).toLocaleString('en-US')}K`;
  return number;
};

function createRange(days) {
  const to = dayjs().endOf('day');
  return [to.subtract(days - 1, 'day').startOf('day'), to];
}

function createThisMonthRange() {
  return [dayjs().startOf('month'), dayjs().endOf('day')];
}

export default function AuctionStatistics() {
  const [draftRange, setDraftRange] = useState(() => createRange(7));
  const [appliedRange, setAppliedRange] = useState(() => {
    const [from, to] = createRange(7);
    return { from: formatDate(from), to: formatDate(to) };
  });
  const [revenueView, setRevenueView] = useState('Daily');
  const [selectedCategory, setSelectedCategory] = useState('ALL');
  const { data, isLoading, isFetching, isError, error, refetch } = useAuctionStatistics(appliedRange);

  const appliedPeriodLabel = `${dayjs(appliedRange.from).format('DD MMM YYYY')} – ${dayjs(appliedRange.to).format('DD MMM YYYY')}`;

  const applyRange = (range = draftRange) => {
    if (!range?.[0] || !range?.[1]) return;
    setDraftRange(range);
    setAppliedRange({ from: formatDate(range[0]), to: formatDate(range[1]) });
  };

  const quickRange = (days) => applyRange(createRange(days));
  const summary = data?.summary || {};
  const revenuePoints = data?.revenueTrend || [];
  const categories = data?.categorySuccess || [];

  const displayedRevenuePoints = useMemo(() => {
    if (revenueView === 'Daily') return revenuePoints;
    let runningTotal = 0;
    return revenuePoints.map((point) => {
      runningTotal += Number(point.revenue || 0);
      return { ...point, revenue: runningTotal };
    });
  }, [revenuePoints, revenueView]);

  const displayedCategories = useMemo(
    () => selectedCategory === 'ALL'
      ? categories
      : categories.filter((item) => String(item.categoryId) === selectedCategory),
    [categories, selectedCategory],
  );

  const successfulTotal = displayedCategories.reduce(
    (total, item) => total + Number(item.successfulAuctions || 0),
    0,
  );

  const lineData = useMemo(() => ({
    labels: displayedRevenuePoints.map((point) => dayjs(point.date).format('DD/MM')),
    datasets: [{
      label: revenueView === 'Daily' ? 'Daily Revenue' : 'Cumulative Revenue',
      data: displayedRevenuePoints.map((point) => Number(point.revenue || 0)),
      borderColor: '#006d75',
      backgroundColor: (context) => {
        const { chart } = context;
        const { ctx, chartArea } = chart;
        if (!chartArea) return 'rgba(0, 109, 117, 0.14)';
        const gradient = ctx.createLinearGradient(0, chartArea.top, 0, chartArea.bottom);
        gradient.addColorStop(0, 'rgba(0, 109, 117, 0.26)');
        gradient.addColorStop(0.68, 'rgba(47, 126, 161, 0.08)');
        gradient.addColorStop(1, 'rgba(255, 255, 255, 0)');
        return gradient;
      },
      pointBackgroundColor: '#ffffff',
      pointHoverBackgroundColor: '#006d75',
      pointBorderColor: '#fff',
      pointHoverBorderColor: '#ffffff',
      pointBorderWidth: 0,
      pointHoverBorderWidth: 3,
      pointRadius: 0,
      pointHoverRadius: 6,
      pointHitRadius: 18,
      borderWidth: 2.5,
      tension: 0.4,
      fill: true,
    }],
  }), [displayedRevenuePoints, revenueView]);

  const pieData = useMemo(() => ({
    labels: displayedCategories.map((item) => item.categoryName),
    datasets: [{
      data: displayedCategories.map((item) => Number(item.successfulAuctions || 0)),
      backgroundColor: displayedCategories.map((_, index) => COLORS[index % COLORS.length]),
      borderWidth: 4,
      borderColor: '#fff',
      borderRadius: 5,
      spacing: 1,
      hoverOffset: 8,
    }],
  }), [displayedCategories]);

  const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    animation: { duration: 550, easing: 'easeOutQuart' },
    interaction: { intersect: false, mode: 'index' },
    plugins: {
      legend: { display: false },
      tooltip: {
        backgroundColor: '#123746',
        titleColor: '#d9f4f3',
        bodyColor: '#ffffff',
        displayColors: false,
        padding: 12,
        cornerRadius: 8,
        titleFont: { size: 12, weight: 500 },
        bodyFont: { size: 13, weight: 600 },
        callbacks: { label: (context) => `Revenue: ${formatVnd(context.parsed.y)}` },
      },
    },
    scales: {
      x: {
        grid: { display: false },
        border: { display: false },
        ticks: { color: '#718096', font: { size: 11, weight: 500 }, maxRotation: 0 },
      },
      y: {
        beginAtZero: true,
        grace: '8%',
        border: { display: false },
        grid: { color: 'rgba(148, 163, 184, 0.18)', drawTicks: false },
        ticks: {
          callback: (value) => `₫${shortCurrency(value)}`,
          color: '#718096',
          font: { size: 11, weight: 500 },
          padding: 10,
        },
      },
    },
  };

  const pieOptions = {
    responsive: true,
    maintainAspectRatio: false,
    cutout: '70%',
    animation: { animateRotate: true, duration: 650, easing: 'easeOutQuart' },
    plugins: {
      legend: {
        position: 'bottom',
        labels: {
          usePointStyle: true,
          pointStyle: 'circle',
          boxWidth: 7,
          boxHeight: 7,
          padding: 16,
          color: '#52616b',
          font: { size: 11, weight: 500 },
        },
      },
      tooltip: {
        backgroundColor: '#123746',
        titleColor: '#d9f4f3',
        bodyColor: '#ffffff',
        padding: 12,
        cornerRadius: 8,
        callbacks: {
          label: (context) => {
            const total = context.dataset.data.reduce((sum, value) => sum + Number(value || 0), 0);
            const percentage = total ? Math.round((context.parsed / total) * 100) : 0;
            return ` ${context.label}: ${context.parsed} (${percentage}%)`;
          },
        },
      },
    },
  };

  const cards = [
    { title: 'Revenue', caption: 'Paid revenue in this period', value: summary.revenue, formatter: formatVnd, icon: <DollarOutlined />, color: '#006d75', background: '#e4f6f4' },
    { title: 'Running Sessions', caption: 'Active auction sessions', value: summary.runningSessions, icon: <PlayCircleOutlined />, color: '#2f7ea1', background: '#e8f2f8' },
    { title: 'Total Bids', caption: 'Valid bids placed', value: summary.totalBids, icon: <RiseOutlined />, color: '#6f6ac8', background: '#efedfb' },
    { title: 'New Users', caption: 'Accounts registered', value: summary.newUsers, icon: <UserAddOutlined />, color: '#d88722', background: '#fff2de' },
  ];

  return (
    <main className="auction-statistics-page">
      <div className="statistics-heading">
        <div className="statistics-heading__copy">
          <span className="statistics-eyebrow">Admin analytics</span>
          <Title level={2}>Auction Statistics</Title>
          <Text type="secondary">Monitor marketplace revenue and auction performance.</Text>
        </div>
        <div className="statistics-period">
          <span className="statistics-period__icon"><CalendarOutlined /></span>
          <span>
            <small>Selected period</small>
            <strong>{appliedPeriodLabel}</strong>
          </span>
        </div>
      </div>

      <Card className="statistics-filter" bordered={false}>
        <div className="statistics-filter__controls">
          <div className="statistics-filter__range">
            <Text strong><CalendarOutlined /> Date Range</Text>
            <RangePicker
              value={draftRange}
              onChange={setDraftRange}
              format="MM/DD/YYYY"
              allowClear={false}
              disabledDate={(current) => current && current.isAfter(dayjs(), 'day')}
            />
          </div>
          <div className="statistics-filter__actions">
            <Button className="filter-preset" onClick={() => quickRange(90)}>Last 90 Days</Button>
            <Button className="filter-preset" onClick={() => applyRange(createThisMonthRange())}>This Month</Button>
            <Button type="primary" onClick={() => applyRange()} disabled={!draftRange?.[0] || !draftRange?.[1]}>
              Apply
            </Button>
          </div>
        </div>
      </Card>

      {isError && (
        <Alert
          type="error"
          showIcon
          message="Unable to load auction statistics"
          description={error?.message || 'Please try again later.'}
          action={<Button onClick={() => refetch()}>Try Again</Button>}
        />
      )}

      <Row gutter={[16, 16]} className="statistics-summary">
        {cards.map((card) => (
          <Col xs={24} sm={12} xl={6} key={card.title}>
            <Card
              bordered={false}
              className="summary-card"
              style={{ '--summary-accent': card.color, '--summary-soft': card.background }}
            >
              <Skeleton loading={isLoading} active paragraph={false}>
                <div className="summary-card__content">
                  <div className="summary-card__copy">
                    <Statistic title={card.title} value={card.value || 0} formatter={card.formatter} />
                    <span className="summary-card__caption">{card.caption}</span>
                  </div>
                  <span className="summary-card__icon">{card.icon}</span>
                </div>
              </Skeleton>
            </Card>
          </Col>
        ))}
      </Row>

      <Spin spinning={isFetching && !isLoading} tip="Updating statistics...">
        <Row gutter={[16, 16]} className="statistics-charts">
          <Col xs={24} xl={16}>
            <Card
              bordered={false}
              className="analytics-chart-card analytics-chart-card--revenue"
              title={(
                <div className="chart-card__title">
                  <strong>Revenue Trend</strong>
                  <span>Paid revenue movement across the selected period</span>
                </div>
              )}
              extra={<Segmented size="small" options={['Daily', 'Cumulative']} value={revenueView} onChange={setRevenueView} />}
            >
              <div className="chart-card__summary">
                <span>Total revenue</span>
                <strong>{formatVnd(summary.revenue)}</strong>
              </div>
              <div className="chart-container">
                {isLoading ? <Skeleton active /> : revenuePoints.length ? <Line data={lineData} options={chartOptions} /> : <Empty description="No revenue data" />}
              </div>
            </Card>
          </Col>
          <Col xs={24} xl={8}>
            <Card
              bordered={false}
              className="analytics-chart-card analytics-chart-card--categories"
              title={(
                <div className="chart-card__title">
                  <strong>Successful Auctions</strong>
                  <span>Distribution by product category</span>
                </div>
              )}
              extra={(
                <Select
                  size="small"
                  className="category-filter"
                  value={selectedCategory}
                  onChange={setSelectedCategory}
                  options={[
                    { value: 'ALL', label: 'All Categories' },
                    ...categories.map((item) => ({ value: String(item.categoryId), label: item.categoryName })),
                  ]}
                />
              )}
            >
              <div className="chart-container chart-container--pie">
                {isLoading ? <Skeleton active /> : displayedCategories.length ? (
                  <>
                    <Doughnut data={pieData} options={pieOptions} />
                    <div className="doughnut-center" aria-hidden="true">
                      <strong>{successfulTotal}</strong>
                      <span>Auctions</span>
                    </div>
                  </>
                ) : <Empty description="No successful auctions" />}
              </div>
            </Card>
          </Col>
        </Row>
      </Spin>
    </main>
  );
}
