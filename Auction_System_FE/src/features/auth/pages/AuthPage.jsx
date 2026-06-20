import { useState, useContext } from 'react';
import { Container, Row, Col, Form, Button } from 'react-bootstrap';
import { Eye, EyeOff } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { authApi } from '../../../api/authApi';
import { AuthContext } from '../../../context/AuthContext';
import '../styles/auth.css';

export default function AuthPage() {
  const navigate = useNavigate();
  const { loginSuccess } = useContext(AuthContext);

  const [isLoginView, setIsLoginView] = useState(true);

  // Sign In State
  const [loginData, setLoginData] = useState({ email: '', password: '' });
  const [showLoginPass, setShowLoginPass] = useState(false);
  const [loginError, setLoginError] = useState('');
  const [isLoggingIn, setIsLoggingIn] = useState(false);

  // Register State
  const [registerData, setRegisterData] = useState({ 
    firstName: '', 
    lastName: '', 
    email: '', 
    password: '', 
    phoneNumber: '' 
  });
  const [showRegPass, setShowRegPass] = useState(false);
  const [regError, setRegError] = useState('');
  const [isRegistering, setIsRegistering] = useState(false);

  const handleLoginSubmit = async (e) => {
    e.preventDefault();
    setLoginError('');
    setIsLoggingIn(true);
    try {
      await authApi.login({
        email: loginData.email,
        password: loginData.password
      });
      // OTP sent, redirect to verify
      navigate('/verify-email', { state: { email: loginData.email, type: 'login' } });
    } catch (err) {
      setLoginError(err.message || 'Login failed. Please try again.');
    } finally {
      setIsLoggingIn(false);
    }
  };

  const handleRegisterSubmit = async (e) => {
    e.preventDefault();
    setRegError('');
    setIsRegistering(true);
    try {
      await authApi.register({
        ...registerData
      });
      // OTP sent, redirect to verify
      navigate('/verify-email', { state: { email: registerData.email, type: 'register' } });
    } catch (err) {
      setRegError(err.message || 'Registration failed. Please try again.');
    } finally {
      setIsRegistering(false);
    }
  };

  return (
    <>
      <section className="auth-hero">
        <Container>
          <h1 className="auth-hero-title">{isLoginView ? 'Sign In' : 'Register'}</h1>
        </Container>
      </section>

      <section className="auth-form-container bg-white">
        <Container>
          <Row className="justify-content-center">
            {isLoginView ? (
              <Col md={6} lg={5}>
                <h3 className="auth-form-title">Sign In</h3>
                {loginError && <div className="alert alert-danger p-2 small">{loginError}</div>}
                
                <Form onSubmit={handleLoginSubmit}>
                  <Form.Group className="mb-4 position-relative">
                    <Form.Label className="auth-label">Username or email address</Form.Label>
                    <Form.Control
                      type="email"
                      className="auth-input"
                      value={loginData.email}
                      onChange={(e) => setLoginData({...loginData, email: e.target.value})}
                      required
                    />
                  </Form.Group>

                  <Form.Group className="mb-4 position-relative">
                    <Form.Label className="auth-label">Password</Form.Label>
                    <Form.Control
                      type={showLoginPass ? "text" : "password"}
                      className="auth-input"
                      value={loginData.password}
                      onChange={(e) => setLoginData({...loginData, password: e.target.value})}
                      required
                    />
                    <button 
                      type="button" 
                      className="password-toggle"
                      onClick={() => setShowLoginPass(!showLoginPass)}
                    >
                      {showLoginPass ? <EyeOff size={18} /> : <Eye size={18} />}
                    </button>
                  </Form.Group>

                  <div className="d-flex justify-content-between align-items-center mb-4">
                    <Form.Check 
                      type="checkbox"
                      id="remember-me"
                      label="Remember me"
                      className="small fw-semibold"
                    />
                    <a href="#forgot" className="small-link">Lost your password?</a>
                  </div>

                  <div className="mb-4">
                    <Button type="submit" className="auth-btn-outline w-100" disabled={isLoggingIn}>
                      {isLoggingIn ? 'Wait...' : 'Sign in'}
                    </Button>
                  </div>
                </Form>

                <div className="text-center mb-4">
                  <p className="small-text fw-medium">
                    Don't have an account? <span className="text-primary" style={{cursor: 'pointer'}} onClick={() => setIsLoginView(false)}>Register here</span>
                  </p>
                </div>

                <div className="mt-4">
                  <p className="small-text fw-medium text-center">Use a social account for faster login</p>
                  <button className="social-btn facebook w-100 mb-2">
                    <span className="fw-bold fs-5 px-1">f</span> Login with Facebook
                  </button>
                  <button className="social-btn google w-100">
                    <span className="fw-bold fs-5 px-1">G</span> Login with Google
                  </button>
                </div>
              </Col>
            ) : (
              <Col md={6} lg={5}>
                <h3 className="auth-form-title">Register</h3>
                {regError && <div className="alert alert-danger p-2 small">{regError}</div>}
                
                <Form onSubmit={handleRegisterSubmit}>
                  <Row>
                    <Col md={6}>
                      <Form.Group className="mb-4 position-relative">
                        <Form.Label className="auth-label">First Name</Form.Label>
                        <Form.Control
                          type="text"
                          className="auth-input"
                          value={registerData.firstName}
                          onChange={(e) => setRegisterData({...registerData, firstName: e.target.value})}
                          required
                        />
                      </Form.Group>
                    </Col>
                    <Col md={6}>
                      <Form.Group className="mb-4 position-relative">
                        <Form.Label className="auth-label">Last Name</Form.Label>
                        <Form.Control
                          type="text"
                          className="auth-input"
                          value={registerData.lastName}
                          onChange={(e) => setRegisterData({...registerData, lastName: e.target.value})}
                          required
                        />
                      </Form.Group>
                    </Col>
                  </Row>

                  <Form.Group className="mb-4 position-relative">
                    <Form.Label className="auth-label">Email address</Form.Label>
                    <Form.Control
                      type="email"
                      className="auth-input"
                      value={registerData.email}
                      onChange={(e) => setRegisterData({...registerData, email: e.target.value})}
                      required
                    />
                  </Form.Group>

                  <Form.Group className="mb-4 position-relative">
                    <Form.Label className="auth-label">Phone Number</Form.Label>
                    <Form.Control
                      type="text"
                      className="auth-input"
                      value={registerData.phoneNumber}
                      onChange={(e) => setRegisterData({...registerData, phoneNumber: e.target.value})}
                    />
                  </Form.Group>

                  <Form.Group className="mb-4 position-relative">
                    <Form.Label className="auth-label">Password</Form.Label>
                    <Form.Control
                      type={showRegPass ? "text" : "password"}
                      className="auth-input"
                      value={registerData.password}
                      onChange={(e) => setRegisterData({...registerData, password: e.target.value})}
                      required
                    />
                    <button 
                      type="button" 
                      className="password-toggle"
                      onClick={() => setShowRegPass(!showRegPass)}
                    >
                      {showRegPass ? <EyeOff size={18} /> : <Eye size={18} />}
                    </button>
                  </Form.Group>

                  <p className="small-text mb-4">
                    Your personal data will be used to support your experience throughout this website, 
                    to manage access to your account, and for other purposes described in our privacy policy.
                  </p>

                  <Button type="submit" className="auth-btn-outline w-100 mb-4" disabled={isRegistering}>
                    {isRegistering ? 'Wait...' : 'Register'}
                  </Button>

                  <div className="text-center mb-4">
                    <p className="small-text fw-medium">
                      Already have an account? <span className="text-primary" style={{cursor: 'pointer'}} onClick={() => setIsLoginView(true)}>Sign In here</span>
                    </p>
                  </div>

                  <div>
                    <button type="button" className="social-btn facebook w-100 mb-2">
                      <span className="fw-bold fs-5 px-1">f</span> Register with Facebook
                    </button>
                    <button type="button" className="social-btn google w-100">
                      <span className="fw-bold fs-5 px-1">G</span> Register with Google
                    </button>
                  </div>
                </Form>
              </Col>
            )}
          </Row>
        </Container>
      </section>
    </>
  );
}
