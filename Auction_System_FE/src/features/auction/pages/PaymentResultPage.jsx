import { useSearchParams, useNavigate } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { CheckCircle, XCircle, Clock, ArrowLeft, Home, Receipt, CreditCard, Calendar, Hash } from 'lucide-react';

const VNP_RESPONSE_MESSAGES = {
  '00': 'Giao dịch thành công',
  '07': 'Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường).',
  '09': 'Thẻ/Tài khoản chưa đăng ký dịch vụ InternetBanking.',
  '10': 'Xác thực thông tin thẻ/tài khoản không đúng quá 3 lần.',
  '11': 'Đã hết hạn chờ thanh toán. Vui lòng thực hiện lại giao dịch.',
  '12': 'Thẻ/Tài khoản bị khóa.',
  '13': 'Mã OTP nhập không đúng. Vui lòng thực hiện lại giao dịch.',
  '24': 'Khách hàng hủy giao dịch.',
  '51': 'Tài khoản không đủ số dư để thực hiện giao dịch.',
  '65': 'Tài khoản đã vượt quá hạn mức giao dịch trong ngày.',
  '75': 'Ngân hàng thanh toán đang bảo trì.',
  '79': 'Nhập sai mật khẩu thanh toán quá số lần quy định.',
  '99': 'Lỗi không xác định.',
};

function formatAmount(vnpAmount) {
  if (!vnpAmount) return '—';
  // VNPay amount is in cents (x100)
  const amount = parseInt(vnpAmount, 10) / 100;
  return amount.toLocaleString('vi-VN') + ' ₫';
}

function formatPayDate(vnpPayDate) {
  if (!vnpPayDate || vnpPayDate.length !== 14) return vnpPayDate || '—';
  // Format: YYYYMMDDHHmmss
  const y = vnpPayDate.slice(0, 4);
  const mo = vnpPayDate.slice(4, 6);
  const d = vnpPayDate.slice(6, 8);
  const h = vnpPayDate.slice(8, 10);
  const mi = vnpPayDate.slice(10, 12);
  const s = vnpPayDate.slice(12, 14);
  return `${d}/${mo}/${y} ${h}:${mi}:${s}`;
}

export default function PaymentResultPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    const timer = setTimeout(() => setMounted(true), 80);
    return () => clearTimeout(timer);
  }, []);

  const responseCode = searchParams.get('vnp_ResponseCode');
  const transactionStatus = searchParams.get('vnp_TransactionStatus');
  const amount = searchParams.get('vnp_Amount');
  const bankCode = searchParams.get('vnp_BankCode');
  const bankTranNo = searchParams.get('vnp_BankTranNo');
  const cardType = searchParams.get('vnp_CardType');
  const orderInfo = searchParams.get('vnp_OrderInfo');
  const payDate = searchParams.get('vnp_PayDate');
  const transactionNo = searchParams.get('vnp_TransactionNo');
  const txnRef = searchParams.get('vnp_TxnRef');

  const isSuccess = responseCode === '00' && transactionStatus === '00';
  const isPending = responseCode === '00' && transactionStatus !== '00';
  const responseMessage = VNP_RESPONSE_MESSAGES[responseCode] || `Mã lỗi: ${responseCode}`;

  return (
    <div style={{ minHeight: '100vh', background: 'linear-gradient(135deg, #f0f4f8 0%, #e8edf2 100%)', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '2rem 1rem' }}>
      <div
        style={{
          maxWidth: 560,
          width: '100%',
          opacity: mounted ? 1 : 0,
          transform: mounted ? 'translateY(0)' : 'translateY(24px)',
          transition: 'opacity 0.5s ease, transform 0.5s ease',
        }}
      >
        {/* Card */}
        <div style={{ background: '#fff', borderRadius: 24, overflow: 'hidden', boxShadow: '0 20px 60px rgba(0,0,0,0.10)' }}>

          {/* Header Banner */}
          <div style={{
            background: isSuccess
              ? 'linear-gradient(135deg, #0f9f83 0%, #0d7a65 100%)'
              : isPending
              ? 'linear-gradient(135deg, #d97706 0%, #b45309 100%)'
              : 'linear-gradient(135deg, #e11d48 0%, #be123c 100%)',
            padding: '2.5rem 2rem',
            textAlign: 'center',
            position: 'relative',
            overflow: 'hidden',
          }}>
            {/* Decorative circles */}
            <div style={{ position: 'absolute', top: -40, right: -40, width: 150, height: 150, borderRadius: '50%', background: 'rgba(255,255,255,0.08)' }} />
            <div style={{ position: 'absolute', bottom: -30, left: -30, width: 100, height: 100, borderRadius: '50%', background: 'rgba(255,255,255,0.06)' }} />

            <div style={{ position: 'relative', zIndex: 1 }}>
              <div style={{
                display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
                width: 80, height: 80, borderRadius: '50%',
                background: 'rgba(255,255,255,0.2)',
                marginBottom: '1rem',
                animation: mounted ? 'popIn 0.4s ease 0.3s both' : 'none',
              }}>
                {isSuccess
                  ? <CheckCircle size={44} color="#fff" strokeWidth={2} />
                  : isPending
                  ? <Clock size={44} color="#fff" strokeWidth={2} />
                  : <XCircle size={44} color="#fff" strokeWidth={2} />}
              </div>

              <h1 style={{ color: '#fff', fontWeight: 800, fontSize: '1.6rem', margin: 0, letterSpacing: '-0.5px' }}>
                {isSuccess ? 'Thanh Toán Thành Công!' : isPending ? 'Đang Xử Lý' : 'Thanh Toán Thất Bại'}
              </h1>
              <p style={{ color: 'rgba(255,255,255,0.85)', marginTop: '0.5rem', marginBottom: 0, fontSize: '0.95rem' }}>
                {responseMessage}
              </p>
            </div>
          </div>

          {/* Amount Highlight */}
          <div style={{ padding: '1.75rem 2rem 0', textAlign: 'center' }}>
            <div style={{ fontSize: '0.75rem', textTransform: 'uppercase', letterSpacing: 1, color: '#9ca3af', fontWeight: 600, marginBottom: 4 }}>
              Số tiền giao dịch
            </div>
            <div style={{
              fontSize: '2.4rem', fontWeight: 800, fontFamily: 'monospace',
              color: isSuccess ? '#0f9f83' : '#374151',
              letterSpacing: '-1px',
            }}>
              {formatAmount(amount)}
            </div>
          </div>

          {/* Divider */}
          <div style={{ margin: '1.5rem 2rem', height: 1, background: '#f1f5f9' }} />

          {/* Transaction Details */}
          <div style={{ padding: '0 2rem 1.5rem' }}>
            <h2 style={{ fontSize: '0.8rem', textTransform: 'uppercase', letterSpacing: 1, color: '#9ca3af', fontWeight: 700, marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: 6 }}>
              <Receipt size={14} /> Chi tiết giao dịch
            </h2>

            <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
              <DetailRow icon={<Hash size={15} color="#6b7280" />} label="Mã tham chiếu" value={txnRef || '—'} />
              <DetailRow icon={<Hash size={15} color="#6b7280" />} label="Mã giao dịch VNPay" value={transactionNo || '—'} />
              <DetailRow icon={<Hash size={15} color="#6b7280" />} label="Mã GD Ngân hàng" value={bankTranNo || '—'} />
              <DetailRow icon={<CreditCard size={15} color="#6b7280" />} label="Ngân hàng" value={bankCode ? `${bankCode} (${cardType || 'N/A'})` : '—'} />
              <DetailRow icon={<Calendar size={15} color="#6b7280" />} label="Thời gian thanh toán" value={formatPayDate(payDate)} />
              <DetailRow icon={<Receipt size={15} color="#6b7280" />} label="Nội dung" value={orderInfo || '—'} />
            </div>
          </div>

          {/* Divider */}
          <div style={{ margin: '0 2rem', height: 1, background: '#f1f5f9' }} />

          {/* Actions */}
          <div style={{ padding: '1.5rem 2rem 2rem', display: 'flex', gap: 12, flexDirection: 'column' }}>
            <button
              onClick={() => navigate('/')}
              style={{
                width: '100%', padding: '0.85rem 1.5rem',
                background: isSuccess ? 'linear-gradient(135deg, #0f9f83 0%, #0d7a65 100%)' : 'linear-gradient(135deg, #004e64 0%, #003347 100%)',
                color: '#fff', border: 'none', borderRadius: 12,
                fontWeight: 700, fontSize: '1rem', cursor: 'pointer',
                display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8,
                transition: 'opacity 0.2s ease',
                fontFamily: 'inherit',
              }}
              onMouseEnter={e => e.currentTarget.style.opacity = '0.88'}
              onMouseLeave={e => e.currentTarget.style.opacity = '1'}
            >
              <Home size={18} /> Về Trang Chủ
            </button>
            <button
              onClick={() => navigate('/auction')}
              style={{
                width: '100%', padding: '0.85rem 1.5rem',
                background: 'transparent', color: '#004e64',
                border: '1.5px solid #004e64', borderRadius: 12,
                fontWeight: 600, fontSize: '0.95rem', cursor: 'pointer',
                display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8,
                transition: 'background 0.2s ease, color 0.2s ease',
                fontFamily: 'inherit',
              }}
              onMouseEnter={e => { e.currentTarget.style.background = '#004e64'; e.currentTarget.style.color = '#fff'; }}
              onMouseLeave={e => { e.currentTarget.style.background = 'transparent'; e.currentTarget.style.color = '#004e64'; }}
            >
              <ArrowLeft size={18} /> Xem Danh Sách Đấu Giá
            </button>
          </div>
        </div>

        {/* Footer note */}
        <p style={{ textAlign: 'center', color: '#9ca3af', fontSize: '0.78rem', marginTop: '1.25rem' }}>
          Được xử lý bảo mật bởi <strong style={{ color: '#6b7280' }}>VNPay</strong>. Vui lòng giữ lại thông tin này cho mục đích tra cứu.
        </p>
      </div>

      <style>{`
        @keyframes popIn {
          from { transform: scale(0.5); opacity: 0; }
          to   { transform: scale(1);   opacity: 1; }
        }
      `}</style>
    </div>
  );
}

function DetailRow({ icon, label, value }) {
  return (
    <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: 12 }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 6, color: '#6b7280', fontSize: '0.88rem', minWidth: 170, flexShrink: 0 }}>
        {icon}
        <span>{label}</span>
      </div>
      <div style={{ fontSize: '0.9rem', fontWeight: 600, color: '#111827', textAlign: 'right', wordBreak: 'break-all' }}>
        {value}
      </div>
    </div>
  );
}
