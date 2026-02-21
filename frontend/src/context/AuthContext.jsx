import { createContext, useContext, useState, useCallback, useEffect } from 'react';
import axios from 'axios';
import { getProfile } from '../api/user';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  const fetchUserProfile = useCallback(async () => {
    try {
      const { data } = await getProfile();
      setUser((prev) => ({ ...prev, ...data, authenticated: true }));
    } catch {
      // Profile fetch failed — keep basic auth state
    }
  }, []);

  useEffect(() => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      setUser({ authenticated: true });
      fetchUserProfile();
    }
    setLoading(false);
  }, [fetchUserProfile]);

  // Listen for forced signout from Axios interceptor
  useEffect(() => {
    const handleSignout = () => {
      setUser(null);
    };
    window.addEventListener('auth:signout', handleSignout);
    return () => window.removeEventListener('auth:signout', handleSignout);
  }, []);

  const login = useCallback(async (email, password) => {
    const { data } = await axios.post('/api/v1/auth/login', { email, password });
    localStorage.setItem('accessToken', data.accessToken);
    localStorage.setItem('refreshToken', data.refreshToken);
    setUser({ authenticated: true, email });
    // Fetch full profile after login
    try {
      const profile = await getProfile();
      setUser((prev) => ({ ...prev, ...profile.data }));
    } catch {
      // Continue with basic state
    }
    return data;
  }, []);

  const register = useCallback(async (email, password) => {
    const { data } = await axios.post('/api/v1/auth/register', { email, password });
    localStorage.setItem('accessToken', data.accessToken);
    localStorage.setItem('refreshToken', data.refreshToken);
    setUser({ authenticated: true, email });
    try {
      const profile = await getProfile();
      setUser((prev) => ({ ...prev, ...profile.data }));
    } catch {
      // Continue with basic state
    }
    return data;
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    setUser(null);
  }, []);

  const isAuthenticated = !!user?.authenticated;

  return (
    <AuthContext.Provider value={{ user, login, register, logout, isAuthenticated, loading }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
};
