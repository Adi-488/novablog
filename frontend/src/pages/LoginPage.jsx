import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

/**
 * Login page — tenant selection + OAuth2 social login.
 */
export default function LoginPage() {
  const { login, switchTenant, tenant } = useAuth();
  const [subdomain, setSubdomain] = useState(tenant || '');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  function handleLogin(provider) {
    if (!subdomain.trim()) {
      setError('Please enter your organization subdomain first.');
      return;
    }
    switchTenant(subdomain.trim());
    login(provider);
  }

  return (
    <div className="min-h-[70vh] flex items-center justify-center">
      <div className="bg-white rounded-[2.5rem] p-10 shadow-sm max-w-md w-full">
        {/* Logo */}
        <div className="flex items-center gap-2.5 mb-8">
          <div className="w-10 h-10 bg-black rounded-full flex items-center justify-center">
            <div className="w-4 h-4 border-2 border-white rounded-sm rotate-45"></div>
          </div>
          <span className="font-black text-2xl tracking-tighter">NOVA</span>
        </div>

        <h1 className="text-3xl font-black tracking-tight mb-2">Welcome Back</h1>
        <p className="text-gray-500 font-semibold text-sm mb-8">
          Sign in to your Nova workspace to continue.
        </p>

        {/* Tenant Input */}
        <div className="mb-6">
          <label className="block text-xs font-bold uppercase text-gray-500 mb-2 tracking-wider">
            Organization Subdomain
          </label>
          <div className="flex items-center bg-[#f4f5f1] rounded-2xl overflow-hidden">
            <input
              type="text"
              placeholder="mycompany"
              value={subdomain}
              onChange={e => { setSubdomain(e.target.value); setError(''); }}
              className="flex-1 px-5 py-4 bg-transparent text-sm font-bold outline-none"
            />
            <span className="pr-5 text-sm font-bold text-gray-400">.novablog.dev</span>
          </div>
          {error && (
            <p className="text-red-500 text-xs font-bold mt-2">{error}</p>
          )}
        </div>

        {/* OAuth Buttons */}
        <div className="flex flex-col gap-3">
          <button
            onClick={() => handleLogin('google')}
            className="flex items-center justify-center gap-3 w-full py-4 bg-black text-white rounded-full font-bold text-sm hover:bg-gray-800 transition-colors"
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
              <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 0 1-2.2 3.32v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.1z" fill="#4285F4"/>
              <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
              <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/>
              <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
            </svg>
            Continue with Google
          </button>

          <button
            onClick={() => handleLogin('github')}
            className="flex items-center justify-center gap-3 w-full py-4 bg-gray-100 text-black rounded-full font-bold text-sm hover:bg-gray-200 transition-colors"
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23A11.509 11.509 0 0 1 12 5.803c1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576C20.566 21.797 24 17.3 24 12c0-6.627-5.373-12-12-12z"/>
            </svg>
            Continue with GitHub
          </button>
        </div>

        <div className="text-center mt-8">
          <p className="text-xs text-gray-400 font-semibold">
            Don't have an organization?{' '}
            <button
              onClick={() => navigate('/register')}
              className="text-black font-bold hover:underline"
            >
              Create one
            </button>
          </p>
        </div>
      </div>
    </div>
  );
}
