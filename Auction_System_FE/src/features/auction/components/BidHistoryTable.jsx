import { Table } from 'react-bootstrap';
import { User, DollarSign, Calendar } from 'lucide-react';

/**
 * BidHistoryTable Component.
 * Displays historical bids in reverse chronological order.
 *
 * @param {Object} props
 * @param {Array} props.bids - List of mapped frontend bid log models
 */
export default function BidHistoryTable({ bids }) {
  const formatDate = (dateStr) => {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    return date.toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    }) + ' ' + date.toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric'
    });
  };

  return (
    <div className="border rounded bg-white overflow-hidden shadow-sm">
      <div 
        className="px-4 py-3 border-bottom d-flex align-items-center justify-content-between" 
        style={{ backgroundColor: '#fcfcfc' }}
      >
        <h5 className="fw-bold mb-0 text-dark">Live Bid Log</h5>
        <span className="badge bg-secondary-subtle px-3 py-1.5 rounded-pill text-dark small font-monospace">
          {bids.length} {bids.length === 1 ? 'Bid' : 'Bids'}
        </span>
      </div>
      
      {bids && bids.length > 0 ? (
        <div style={{ maxHeight: '350px', overflowY: 'auto' }}>
          <Table responsive hover className="mb-0 text-start align-middle" style={{ borderCollapse: 'separate' }}>
            <thead>
              <tr style={{ position: 'sticky', top: 0, zIndex: 1 }}>
                <th className="px-4 py-3" style={{ width: '45%' }}>Bidder</th>
                <th className="px-4 py-3" style={{ width: '25%' }}>Amount</th>
                <th className="px-4 py-3" style={{ width: '30%' }}>Time</th>
              </tr>
            </thead>
            <tbody>
              {bids.map((bid, idx) => (
                <tr 
                  key={bid.bidId}
                  className="bid-row-animate"
                  style={{
                    animation: idx === 0 ? 'flash-green 1.5s ease-out' : 'none',
                    borderLeft: idx === 0 ? '4px solid #0f9f83' : 'none',
                    transition: 'background-color 0.3s ease'
                  }}
                >
                  <td className="px-4 py-3 fw-medium text-dark font-monospace d-flex align-items-center gap-2">
                    <div 
                      className="rounded-circle d-flex align-items-center justify-content-center text-white"
                      style={{ 
                        backgroundColor: idx === 0 ? '#0f9f83' : '#6c757d', 
                        width: '28px', 
                        height: '28px',
                        fontSize: '0.8rem'
                      }}
                    >
                      <User size={14} />
                    </div>
                    <span>{bid.bidderName}</span>
                    {idx === 0 && (
                      <span className="badge bg-success-subtle border text-success small px-2 py-0.5 rounded-pill">
                        Highest
                      </span>
                    )}
                  </td>
                  <td className="px-4 py-3 fw-bold text-dark font-monospace">
                    ${parseFloat(bid.amount).toLocaleString('en-US', { minimumFractionDigits: 2 })}
                  </td>
                  <td className="px-4 py-3 text-muted small font-monospace">
                    {formatDate(bid.bidTime)}
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        </div>
      ) : (
        <div className="text-center py-5">
          <p className="text-muted italic small mb-0">No bids have been placed yet for this item.</p>
        </div>
      )}

      {/* Embedded style tag for flash micro-animation */}
      <style>{`
        @keyframes flash-green {
          0% {
            background-color: rgba(15, 159, 131, 0.25);
          }
          100% {
            background-color: transparent;
          }
        }
        .bid-row-animate {
          transition: background-color 0.2s ease;
        }
      `}</style>
    </div>
  );
}
