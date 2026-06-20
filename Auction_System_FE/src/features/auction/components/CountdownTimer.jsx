import { useState, useEffect } from 'react';
import { Card } from 'react-bootstrap';
import { Clock } from 'lucide-react';

/**
 * CountdownTimer Component.
 * Computes remaining time based on endTime prop and updates every second.
 *
 * @param {Object} props
 * @param {string} props.endTime - ISO date string of auction session end
 * @param {function} [props.onTimeUp] - Callback triggered when countdown hits zero
 */
export default function CountdownTimer({ endTime, onTimeUp }) {
  const [timeLeft, setTimeLeft] = useState({
    days: 0,
    hours: 0,
    minutes: 0,
    seconds: 0,
    isEnded: false
  });

  useEffect(() => {
    if (!endTime) return;

    const calculateTimeLeft = () => {
      const difference = +new Date(endTime) - +new Date();
      
      if (difference <= 0) {
        return {
          days: 0,
          hours: 0,
          minutes: 0,
          seconds: 0,
          isEnded: true
        };
      }

      return {
        days: Math.floor(difference / (1000 * 60 * 60 * 24)),
        hours: Math.floor((difference / (1000 * 60 * 60)) % 24),
        minutes: Math.floor((difference / 1000 / 60) % 60),
        seconds: Math.floor((difference / 1000) % 60),
        isEnded: false
      };
    };

    // Initial check
    const initialTime = calculateTimeLeft();
    setTimeLeft(initialTime);
    if (initialTime.isEnded && onTimeUp) {
      onTimeUp();
    }

    const timer = setInterval(() => {
      const computed = calculateTimeLeft();
      setTimeLeft(computed);
      
      if (computed.isEnded) {
        clearInterval(timer);
        if (onTimeUp) {
          onTimeUp();
        }
      }
    }, 1000);

    return () => clearInterval(timer);
  }, [endTime, onTimeUp]);

  const { days, hours, minutes, seconds, isEnded } = timeLeft;
  const isUrgent = !isEnded && days === 0 && hours === 0 && minutes < 5; // Less than 5 minutes remaining

  if (isEnded) {
    return (
      <Card className="border-0 shadow-sm text-center" style={{ backgroundColor: '#fee2e2' }}>
        <Card.Body className="py-3 px-4 d-flex align-items-center justify-content-center gap-2">
          <Clock size={20} className="text-danger animate-pulse" />
          <span className="fw-bold text-danger text-uppercase tracking-wider">Auction Ended</span>
        </Card.Body>
      </Card>
    );
  }

  const timeBlocks = [
    { label: 'Days', value: days },
    { label: 'Hours', value: hours },
    { label: 'Min', value: minutes },
    { label: 'Sec', value: seconds }
  ];

  return (
    <Card className="border-0 shadow-sm overflow-hidden" style={{ transition: 'all 0.3s ease' }}>
      <div 
        className="w-100 py-1" 
        style={{ 
          background: isUrgent 
            ? 'linear-gradient(90deg, #dc2626 0%, #ef4444 100%)' 
            : 'linear-gradient(90deg, #003d5b 0%, #0077b6 100%)' 
        }} 
      />
      <Card.Body className="p-3 text-center">
        <div className="d-flex align-items-center justify-content-center gap-2 mb-2 text-muted small text-uppercase tracking-wider fw-bold">
          <Clock size={16} className={isUrgent ? "text-danger" : "text-primary"} />
          <span>Time Remaining</span>
        </div>
        <div className="d-flex justify-content-center gap-3">
          {timeBlocks.map((block, idx) => (
            <div key={idx} className="d-flex flex-column align-items-center">
              <div 
                className="d-flex align-items-center justify-content-center rounded fw-bold text-white shadow-sm"
                style={{ 
                  width: '50px', 
                  height: '45px', 
                  fontSize: '1.25rem',
                  backgroundColor: isUrgent ? '#dc2626' : '#2b2d42',
                  transition: 'background-color 0.5s ease',
                  fontFamily: 'monospace'
                }}
              >
                {String(block.value).padStart(2, '0')}
              </div>
              <span className="text-muted mt-1" style={{ fontSize: '0.65rem', fontWeight: 600, uppercase: true }}>
                {block.label}
              </span>
            </div>
          ))}
        </div>
      </Card.Body>
    </Card>
  );
}
