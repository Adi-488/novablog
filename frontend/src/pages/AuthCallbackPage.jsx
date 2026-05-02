import React, { useEffect, useState } from 'react';
import { useParams, useSearchParams, useNavigate } from 'react-router-dom';
import { authApi, setStoredUser } from '../api/client';
import { useAuth } from '../context/AuthContext';

/**
 * OAuth2 callback handler.
 * URL: /auth/callback/:provider?code=xxx&tenant=xxx
 * 
 * Exchanges the authorization code from the OAuth2 provider
 * for NovaBlog JWT tokens, then redirects to the dashboard.
 */
export default function AuthCallbackPage() {
  const { provider } = useParams();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { setUser, switchTenant } = useAuth();
  const [error, setError] = useState('');

  useEffect(() => {
    async function handleCallback() {
      const code = searchParams.get('code');
      const tenant = searchParams.get('tenant') || searchParams.get('state');

      if (!code) {
        setError('No authorization code received from provider.');
        return;
      }

      if (!tenant) {
        setError('No tenant context. Please try logging in again.');
        return;
      }

      try {
        switchTenant(tenant);
        const redirectUri = `${window.location.origin}/auth/callback/${provider}`;
        const data = await authApi.exchangeCode(provider, code, tenant, redirectUri);

        if (data.user) {
          setUser(data.user);
          setStoredUser(data.user);
        }

        // Redirect to dashboard
        navigate('/dashboard', { replace: true });
      } catch (err) {
        console.error('Auth callback failed:', err);
        setError(err.message || 'Authentication failed. Please try again.');
      }
    }

    handleCallback();
  }, [provider, searchParams, navigate, setUser, switchTenant]);

  if (error) {
    return (
      <div className="min-h-[70vh] flex items-center justify-center">
        <div className="bg-white rounded-[2.5rem] p-10 shadow-sm max-w-md w-full text-center">
          <div className="text-5xl mb-4">⚠️</div>
          <h1 className="text-2xl font-black mb-2">Authentication Failed</h1>
          <p className="text-gray-500 font-semibold text-sm mb-6">{error}</p>
          <button
            onClick={() => navigate('/login')}
            className="bg-black text-white px-6 py-3 rounded-full text-sm font-bold hover:bg-gray-800 transition-colors"
          >
            Back to Login
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-[70vh] flex items-center justify-center">
      <div className="bg-white rounded-[2.5rem] p-10 shadow-sm max-w-md w-full text-center">
        <div className="w-10 h-10 border-4 border-black border-t-transparent rounded-full animate-spin mx-auto mb-6"></div>
        <h1 className="text-xl font-black mb-2">Signing you in...</h1>
        <p className="text-gray-500 font-semibold text-sm">
          Exchanging credentials with {provider}
        </p>
      </div>
    </div>
  );
}
