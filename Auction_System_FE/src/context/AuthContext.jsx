import { createContext, useState, useEffect } from 'react';
import { authApi } from '../api/authApi';

export const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [accessToken, setAccessToken] = useState(null);
  const [isAuthModalOpen, setIsAuthModalOpen] = useState(false);

  const openAuthModal = () => setIsAuthModalOpen(true);
  const closeAuthModal = () => setIsAuthModalOpen(false);

  const parseJwt = (token) => {
    if (!token) return null;
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        window
          .atob(base64)
          .split('')
          .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );
      const payload = JSON.parse(jsonPayload);
      return {
        id: payload.userId,
        email: payload.sub,
        roles: payload.roles || []
      };
    } catch (error) {
      console.error('Failed to parse JWT token:', error);
      return null;
    }
  };

  useEffect(() => {
    // Check if there is an access token in session storage or memory
    const storedToken = sessionStorage.getItem('accessToken');
    if (storedToken) {
      setAccessToken(storedToken);
      setIsAuthenticated(true);
      setUser(parseJwt(storedToken));
    }
  }, []);

  const loginSuccess = (token) => {
    setAccessToken(token);
    setIsAuthenticated(true);
    setUser(parseJwt(token));
    sessionStorage.setItem('accessToken', token);
  };

  const logout = async () => {
    try {
      await authApi.logout();
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      setUser(null);
      setAccessToken(null);
      setIsAuthenticated(false);
      sessionStorage.removeItem('accessToken');
    }
  };

  return (
    <AuthContext.Provider value={{
      user,
      isAuthenticated,
      accessToken,
      loginSuccess,
      logout,
      isAuthModalOpen,
      openAuthModal,
      closeAuthModal
    }}>
      {children}
    </AuthContext.Provider>
  );
}
