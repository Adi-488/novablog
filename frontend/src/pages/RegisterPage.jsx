import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { tenantApi } from '../api/client';
import { useAuth } from '../context/AuthContext';
import { Check, ArrowRight, Loader2 } from 'lucide-react';

export default function RegisterPage() {
  const navigate = useNavigate();
  const { switchTenant } = useAuth();

  const [name, setName] = useState('');
  const [subdomain, setSubdomain] = useState('');
  const [email, setEmail] = useState('');
  const [available, setAvailable] = useState(null);
  const [checking, setChecking] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  async function checkAvailability(value) {
    const slug = value.toLowerCase().replace(/[^a-z0-9-]/g, '');
    setSubdomain(slug);
    if (slug.length < 3) { setAvailable(null); return; }

    setChecking(true);
    try {
      const res = await tenantApi.checkAvailability(slug);
      setAvailable(res.available !== false);
    } catch {
      setAvailable(null);
    }
    setChecking(false);
  }

  async function handleSubmit(e) {
    e.preventDefault();
    if (!name || !subdomain || !email) {
      setError('All fields are required.');
      return;
    }
    if (available === false) {
      setError('That subdomain is taken.');
      return;
    }

    setSubmitting(true);
    setError('');
    try {
      await tenantApi.register(name, subdomain, email);
      switchTenant(subdomain);
      setSuccess(true);
    } catch (err) {
      setError(err.message || 'Registration failed.');
    }
    setSubmitting(false);
  }

  if (success) {
    return (
      <div className="min-h-[70vh] flex items-center justify-center">
        <div className="bg-white rounded-[2.5rem] p-10 shadow-sm max-w-md w-full text-center">
          <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-6">
            <Check size={32} className="text-green-600" />
          </div>
          <h1 className="text-3xl font-black mb-2">You're all set!</h1>
          <p className="text-gray-500 font-semibold text-sm mb-2">
            <strong>{name}</strong> has been created at
          </p>
          <p className="text-lg font-black text-black mb-6">
            {subdomain}.novablog.dev
          </p>
          <button
            onClick={() => navigate('/login')}
            className="inline-flex items-center gap-2 bg-black text-white px-6 py-3 rounded-full text-sm font-bold hover:bg-gray-800 transition-colors"
          >
            Sign In Now <ArrowRight size={14} />
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-[70vh] flex items-center justify-center">
      <div className="bg-white rounded-[2.5rem] p-10 shadow-sm max-w-lg w-full">
        <div className="flex items-center gap-2.5 mb-8">
          <div className="w-10 h-10 bg-black rounded-full flex items-center justify-center">
            <div className="w-4 h-4 border-2 border-white rounded-sm rotate-45"></div>
          </div>
          <span className="font-black text-2xl tracking-tighter">NOVA</span>
        </div>

        <h1 className="text-3xl font-black tracking-tight mb-2">Create Your Blog</h1>
        <p className="text-gray-500 font-semibold text-sm mb-8">
          Set up a new organization on Nova in seconds.
        </p>

        <form onSubmit={handleSubmit} className="space-y-5">
          {/* Org Name */}
          <div>
            <label className="block text-xs font-bold uppercase text-gray-500 mb-2 tracking-wider">
              Organization Name
            </label>
            <input
              type="text"
              placeholder="Acme Corp"
              value={name}
              onChange={e => setName(e.target.value)}
              className="w-full px-5 py-4 bg-[#f4f5f1] rounded-2xl text-sm font-bold outline-none focus:ring-2 focus:ring-black/10"
            />
          </div>

          {/* Subdomain */}
          <div>
            <label className="block text-xs font-bold uppercase text-gray-500 mb-2 tracking-wider">
              Subdomain
            </label>
            <div className="flex items-center bg-[#f4f5f1] rounded-2xl overflow-hidden">
              <input
                type="text"
                placeholder="acme"
                value={subdomain}
                onChange={e => checkAvailability(e.target.value)}
                className="flex-1 px-5 py-4 bg-transparent text-sm font-bold outline-none"
              />
              <span className="pr-4 text-sm font-bold text-gray-400">.novablog.dev</span>
              {checking && <Loader2 size={16} className="animate-spin mr-4 text-gray-400" />}
              {!checking && available === true && <Check size={16} className="mr-4 text-green-500" />}
              {!checking && available === false && <span className="mr-4 text-red-500 text-xs font-bold">Taken</span>}
            </div>
          </div>

          {/* Email */}
          <div>
            <label className="block text-xs font-bold uppercase text-gray-500 mb-2 tracking-wider">
              Admin Email
            </label>
            <input
              type="email"
              placeholder="admin@acme.com"
              value={email}
              onChange={e => setEmail(e.target.value)}
              className="w-full px-5 py-4 bg-[#f4f5f1] rounded-2xl text-sm font-bold outline-none focus:ring-2 focus:ring-black/10"
            />
          </div>

          {error && <p className="text-red-500 text-xs font-bold">{error}</p>}

          <button
            type="submit"
            disabled={submitting}
            className="w-full py-4 bg-black text-white rounded-full font-bold text-sm hover:bg-gray-800 transition-colors disabled:opacity-50 flex items-center justify-center gap-2"
          >
            {submitting ? (
              <><Loader2 size={16} className="animate-spin" /> Creating...</>
            ) : (
              <>Create Organization <ArrowRight size={14} /></>
            )}
          </button>
        </form>

        <p className="text-xs text-gray-400 font-semibold text-center mt-6">
          Already have an organization?{' '}
          <button onClick={() => navigate('/login')} className="text-black font-bold hover:underline">
            Sign in
          </button>
        </p>
      </div>
    </div>
  );
}
