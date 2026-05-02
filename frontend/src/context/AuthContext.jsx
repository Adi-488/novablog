import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { authApi, getStoredUser, getAccessToken, clearTokens, usersApi, setStoredUser, getStoredTenant, setStoredTenant } from '../api/client';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(getStoredUser);
  const [tenant, setTenant] = useState(getStoredTenant);
  const [loading, setLoading] = useState(true);

  const isAuthenticated = !!getAccessToken() && !!user;

  // On mount, verify the stored token is still valid
  useEffect(() => {
    async function verify() {
      const token = getAccessToken();
      if (!token) {
        setLoading(false);
        return;
      }
      try {
        const me = await usersApi.getMe();
        setUser(me);
        setStoredUser(me);
      } catch {
        // Token invalid or expired (refresh already attempted by client)
        clearTokens();
        setUser(null);
      }
      setLoading(false);
    }
    verify();
  }, []);

  const login = useCallback((provider) => {
    if (!tenant) {
      throw new Error('Tenant must be set before logging in');
    }
    // Redirect to backend OAuth2 initiation
    window.location.href = authApi.getLoginUrl(provider, tenant);
  }, [tenant]);

  const logout = useCallback(() => {
    authApi.logout();
    setUser(null);
  }, []);

  const switchTenant = useCallback((subdomain) => {
    setTenant(subdomain);
    setStoredTenant(subdomain);
  }, []);

  const value = {
    user,
    setUser,
    tenant,
    switchTenant,
    isAuthenticated,
    loading,
    login,
    logout,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within <AuthProvider>');
  return ctx;
}
