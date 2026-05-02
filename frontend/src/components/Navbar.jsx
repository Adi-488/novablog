import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { Search, Menu, X, PenLine } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import NotificationBell from './NotificationBell';

export default function Navbar() {
  const { user, isAuthenticated, logout } = useAuth();
  const [mobileOpen, setMobileOpen] = useState(false);
  const navigate = useNavigate();

  const location = useLocation();

  const publicLinks = [
    { label: 'Home', to: '/' },
    { label: 'Blog', to: '/blog' },
    { label: 'Categories', to: '/categories' },
    { label: 'About', to: '/about' },
  ];

  const authLinks = [
    { label: 'Home', to: '/' },
    { label: 'Dashboard', to: '/dashboard' },
    { label: 'Blog', to: '/blog' },
    { label: 'About', to: '/about' },
  ];

  const navLinks = isAuthenticated ? authLinks : publicLinks;

  return (
    <nav className="flex items-center justify-between bg-white rounded-full px-6 py-4 shadow-sm mb-10">
      {/* Logo */}
      <Link to="/" className="flex items-center gap-2.5 group">
        <div className="w-9 h-9 bg-black rounded-full flex items-center justify-center group-hover:scale-110 transition-transform">
          <div className="w-3.5 h-3.5 border-2 border-white rounded-sm rotate-45"></div>
        </div>
        <span className="font-black text-xl tracking-tighter">NOVA</span>
      </Link>

      {/* Desktop Nav Links */}
      <div className="hidden lg:flex items-center gap-8 text-sm font-bold text-gray-400">
        {navLinks.map((link, i) => (
          <React.Fragment key={link.to}>
            {i > 0 && <span className="w-1 h-1 bg-gray-300 rounded-full"></span>}
            <Link
              to={link.to}
              className={`hover:text-black transition-colors ${location.pathname === link.to ? 'text-black' : ''}`}
            >
              {link.label}
            </Link>
          </React.Fragment>
        ))}
      </div>

      {/* Right Side Actions */}
      <div className="flex items-center gap-3">
        <button
          onClick={() => navigate('/blog?search=true')}
          className="w-10 h-10 flex items-center justify-center rounded-full hover:bg-gray-100 transition-colors"
        >
          <Search size={18} strokeWidth={2.5} />
        </button>

        {isAuthenticated && (
          <>
            <NotificationBell />

            <Link
              to="/editor"
              className="hidden md:flex items-center gap-2 bg-black text-white px-5 py-2.5 rounded-full text-sm font-bold hover:bg-gray-800 transition-colors"
            >
              <PenLine size={14} strokeWidth={2.5} />
              Write
            </Link>
          </>
        )}

        <div className="flex items-center gap-3 pl-3 border-l border-gray-200">
          {isAuthenticated ? (
            <div className="flex items-center gap-3">
              <Link to="/profile" className="w-9 h-9 bg-gray-200 rounded-full overflow-hidden hover:ring-2 hover:ring-black/20 transition-all">
                <img
                  src={user?.avatarUrl || `https://ui-avatars.com/api/?name=${encodeURIComponent(user?.displayName || 'U')}&background=000&color=fff&bold=true&size=64`}
                  alt="Profile"
                  className="w-full h-full object-cover"
                />
              </Link>
              <button
                onClick={() => { logout(); navigate('/'); }}
                className="font-bold text-sm text-gray-500 hover:text-black transition-colors"
              >
                Log Out
              </button>
            </div>
          ) : (
            <Link
              to="/login"
              className="font-bold text-sm text-gray-500 hover:text-black transition-colors"
            >
              Log In
            </Link>
          )}
        </div>

        {/* Mobile Menu Toggle */}
        <button
          className="lg:hidden w-10 h-10 flex items-center justify-center rounded-full hover:bg-gray-100 transition-colors"
          onClick={() => setMobileOpen(!mobileOpen)}
        >
          {mobileOpen ? <X size={20} /> : <Menu size={20} />}
        </button>
      </div>

      {/* Mobile Menu Dropdown */}
      {mobileOpen && (
        <div className="absolute top-20 left-4 right-4 bg-white rounded-3xl shadow-xl p-6 z-50 lg:hidden">
          <div className="flex flex-col gap-4">
            {navLinks.map(link => (
              <Link
                key={link.to}
                to={link.to}
                className="font-bold text-lg text-gray-700 hover:text-black"
                onClick={() => setMobileOpen(false)}
              >
                {link.label}
              </Link>
            ))}
          </div>
        </div>
      )}
    </nav>
  );
}
