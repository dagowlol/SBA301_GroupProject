import { useState, useContext } from 'react';
import { Container, Row, Col, Form, Button } from 'react-bootstrap';
import { useLocation, useNavigate } from 'react-router-dom';
import { authApi } from '../../../api/authApi';
import { AuthContext } from '../../../context/AuthContext';
import '../styles/auth.css';

export default function VerifyEmailPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const { loginSuccess } = useContext(AuthContext);

  const { email, type } = location.state || { email: '', type: 'login' };
  
  const [otpCode, setOtpCode] = useState('');
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');
  const [isVerifying, setIsVerifying] = useState(false);

  // Mask email for display
  const maskedEmail = email 
    ? email.replace(/^(.)(.*)(.@.*)$/, (_, a, b, c) => a + b.replace(/./g, '*') + c)
    : 'your email';

  const handleVerify = async (e) => {
    e.preventDefault();
    setError('');
    setSuccessMsg('');
    setIsVerifying(true);
    
    try {
      if (type === 'register') {
        await authApi.verifyRegisterOtp({ email, otp: otpCode });
        setSuccessMsg('Account verified successfully! You can now sign in.');
        setTimeout(() => navigate('/auth'), 2000);
      } else {
        const res = await authApi.verifyLoginOtp({ email, otp: otpCode });
        if (res && res.accessToken) {
          loginSuccess(res.accessToken);
          navigate('/');
        } else {
          setError('Invalid response from server.');
        }
      }
    } catch (err) {
      setError(err.message || 'Verification failed. Please check your code.');
    } finally {
      setIsVerifying(false);
    }
  };

  const handleResend = async () => {
    setError('');
    setSuccessMsg('');
    try {
      await authApi.resendOtp({ email });
      setSuccessMsg('A new code has been sent to your email.');
    } catch (err) {
      setError(err.message || 'Failed to resend code.');
    }
  };

  if (!email) {
    return (
      <Container className="text-center py-5">
        <p>No email provided for verification. Please <a href="/auth">Sign In</a> or Register.</p>
      </Container>
    );
  }

  return (
    <>
      <section className="auth-hero">
        <Container>
          <h1 className="auth-hero-title">Sign in / Register</h1>
        </Container>
      </section>

      <section className="auth-form-container bg-white text-center">
        <Container>
          <Row className="justify-content-center">
            <Col md={8} lg={6}>
              <h3 className="auth-form-title mb-4">Check your email</h3>
              <p className="small-text mb-5" style={{ fontSize: '0.95rem' }}>
                We have receive your registration info, please check your email <br/>
                ({maskedEmail}) for the confirmation link to sign in (please note to <br/>
                check your spam folder too, in case you don't get it in your inbox)
              </p>

              {error && <div className="alert alert-danger p-2 small">{error}</div>}
              {successMsg && <div className="alert alert-success p-2 small">{successMsg}</div>}

              <Form onSubmit={handleVerify} className="mb-4">
                <Form.Group className="mb-4">
                  <Form.Control
                    type="text"
                    placeholder="Enter 6-digit OTP Code"
                    className="auth-input text-center fs-5 tracking-widest"
                    maxLength={6}
                    value={otpCode}
                    onChange={(e) => setOtpCode(e.target.value)}
                    required
                    style={{ letterSpacing: '0.5rem', width: '250px', margin: '0 auto' }}
                  />
                </Form.Group>

                <div className="d-flex justify-content-center gap-3">
                  <Button type="submit" className="auth-btn-outline px-4" disabled={isVerifying || otpCode.length < 6}>
                    {isVerifying ? 'Wait...' : 'Account Verification'}
                  </Button>
                  <Button type="button" className="auth-btn-outline px-4" onClick={handleResend}>
                    Resend email
                  </Button>
                </div>
              </Form>
            </Col>
          </Row>
        </Container>
      </section>
    </>
  );
}
