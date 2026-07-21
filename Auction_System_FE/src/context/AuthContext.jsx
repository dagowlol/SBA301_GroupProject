import { createContext, useState, useEffect, useCallback } from 'react';
import { authApi } from '../api/authApi';

export const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [accessToken, setAccessToken] = useState(null);
  const [isAuthModalOpen, setIsAuthModalOpen] = useState(false);

  const openAuthModal = () => setIsAuthModalOpen(true);
  const closeAuthModal = () => setIsAuthModalOpen(false);

  const fetchCurrentUser = useCallback(async () => {
    try {
      const userData = await authApi.getMe();
      setUser(userData);
      return userData;
    } catch (error) {
      console.error('Failed to fetch current user:', error);
      setUser(null);
      setAccessToken(null);
      setIsAuthenticated(false);
      sessionStorage.removeItem('accessToken');
      return null;
    }
  }, []);

  useEffect(() => {
    const storedToken = sessionStorage.getItem('accessToken');
    if (storedToken) {
      setAccessToken(storedToken);
      setIsAuthenticated(true);
      fetchCurrentUser();
    }
  }, [fetchCurrentUser]);

  const loginSuccess = async (token) => {
    setAccessToken(token);
    setIsAuthenticated(true);
    sessionStorage.setItem('accessToken', token);
    await fetchCurrentUser();
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
