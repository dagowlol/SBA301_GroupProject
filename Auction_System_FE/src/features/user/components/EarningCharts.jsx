import React, { useMemo } from 'react';
import {
  Chart as ChartJS,
  ArcElement,
  BarElement,
  CategoryScale,
  Legend,
  LinearScale,
  Tooltip,
} from 'chart.js';
import { Bar, Doughnut } from 'react-chartjs-2';
import { BarChart3, CircleDollarSign } from 'lucide-react';
import './EarningCharts.css';

ChartJS.register(CategoryScale, LinearScale, BarElement, ArcElement, Tooltip, Legend);

const RANGE_OPTIONS = [
  { value: 'LAST_7_DAYS', label: '7 days' },
  { value: 'LAST_30_DAYS', label: '30 days' },
  { value: 'LAST_6_MONTHS', label: '6 months' },
  { value: 'LAST_12_MONTHS', label: '1 year' },
];

const formatCurrency = (value) => `${new Intl.NumberFormat('vi-VN').format(value || 0)} VNĐ`;

const formatAxisCurrency = (value) => {
  if (value >= 1_000_000_000) return `${(value / 1_000_000_000).toLocaleString('vi-VN')} tỷ`;
  if (value >= 1_000_000) return `${(value / 1_000_000).toLocaleString('vi-VN')} tr`;
  if (value >= 1_000) return `${(value / 1_000).toLocaleString('vi-VN')}k`;
  return value;
};

const formatPeriod = (period) => {
  const parts = period.split('-');
  if (parts.length === 2) return `T${Number(parts[1])}/${parts[0]}`;
  if (parts.length === 3) return `${parts[2]}/${parts[1]}`;
  return period;
};

export default function EarningCharts({ statistics, range, onRangeChange, isLoading, isError }) {
  const points = statistics?.revenueByPeriod || [];
  const paidCount = statistics?.paidCount || 0;
  const pendingCount = statistics?.pendingCount || 0;

  const revenueData = useMemo(() => ({
    labels: points.map((point) => formatPeriod(point.period)),
    datasets: [{
      label: 'Paid revenue',
      data: points.map((point) => Number(point.revenue || 0)),
      backgroundColor: '#087ea4',
      hoverBackgroundColor: '#005f80',
      borderRadius: 7,
      borderSkipped: false,
      maxBarThickness: 46,
    }],
  }), [points]);

  const statusData = useMemo(() => ({
    labels: ['Paid', 'Pending'],
    datasets: [{
      data: [paidCount, pendingCount],
      backgroundColor: ['#14b8a6', '#f59e0b'],
      hoverBackgroundColor: ['#0f9488', '#d97706'],
      borderWidth: 0,
      spacing: 3,
    }],
  }), [paidCount, pendingCount]);

  const revenueOptions = {
    responsive: true,
    maintainAspectRatio: false,
    animation: { duration: 450 },
    plugins: {
      legend: { display: false },
      tooltip: {
        displayColors: false,
        callbacks: {
          label: (context) => formatCurrency(context.parsed.y),
        },
      },
    },
    scales: {
      x: {
        grid: { display: false },
        border: { display: false },
        ticks: { color: '#7c8a9a', font: { size: 11, weight: 500 }, maxRotation: 0 },
      },
      y: {
        beginAtZero: true,
        border: { display: false },
        grid: { color: '#edf1f5' },
        ticks: {
          color: '#94a3b8',
          font: { size: 11 },
          callback: formatAxisCurrency,
        },
      },
    },
  };

  const statusOptions = {
    responsive: true,
    maintainAspectRatio: false,
    cutout: '70%',
    plugins: {
      legend: {
        position: 'bottom',
        labels: {
          usePointStyle: true,
          pointStyle: 'circle',
          boxWidth: 8,
          padding: 18,
          color: '#64748b',
          font: { size: 11, weight: 600 },
        },
      },
      tooltip: {
        callbacks: {
          label: (context) => `${context.label}: ${context.parsed} products`,
        },
      },
    },
  };

  return (
    <section className="earning-analytics" aria-label="Earning analytics">
      <div className="earning-analytics__header">
        <div>
          <h3 className="earning-analytics__title">Revenue analytics</h3>
          <p className="earning-analytics__subtitle">Paid revenue and payment status for the selected period</p>
        </div>
        <div className="earning-range" aria-label="Statistics time range">
          {RANGE_OPTIONS.map((option) => (
            <button
              key={option.value}
              type="button"
              className={`earning-range__button ${range === option.value ? 'earning-range__button--active' : ''}`}
              onClick={() => onRangeChange(option.value)}
              aria-pressed={range === option.value}
            >
              {option.label}
            </button>
          ))}
        </div>
      </div>

      {isLoading ? (
        <div className="earning-analytics__loading">
          <span className="earning-analytics__spinner" />
          Loading analytics...
        </div>
      ) : isError ? (
        <div className="earning-analytics__error">Unable to load analytics. Please try again.</div>
      ) : (
        <div className="earning-analytics__grid">
          <article className="earning-chart-card earning-chart-card--revenue">
            <div className="earning-chart-card__heading">
              <span className="earning-chart-card__icon"><BarChart3 size={18} /></span>
              <div>
                <h4>Revenue over time</h4>
                <p>Completed final payments</p>
              </div>
            </div>
            <div className="earning-chart-card__canvas earning-chart-card__canvas--bar">
              <Bar data={revenueData} options={revenueOptions} />
            </div>
          </article>

          <article className="earning-chart-card earning-chart-card--status">
            <div className="earning-chart-card__heading">
              <span className="earning-chart-card__icon earning-chart-card__icon--amber">
                <CircleDollarSign size={18} />
              </span>
              <div>
                <h4>Payment status</h4>
                <p>{paidCount + pendingCount} finished auctions</p>
              </div>
            </div>
            {paidCount + pendingCount > 0 ? (
              <div className="earning-chart-card__canvas earning-chart-card__canvas--doughnut">
                <Doughnut data={statusData} options={statusOptions} />
                <div className="earning-chart-card__center">
                  <strong>{paidCount}</strong>
                  <span>paid</span>
                </div>
              </div>
            ) : (
              <div className="earning-chart-card__empty">No payment data in this period</div>
            )}
          </article>
        </div>
      )}
    </section>
  );
}
