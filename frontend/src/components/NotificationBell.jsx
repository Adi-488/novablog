import React, { useState, useEffect, useRef } from 'react';
import { Bell, Check, ExternalLink } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';

const API_BASE = '/api/v1';

/**
 * Notification bell component for the Navbar.
 * 
 * Sprint 5: Polls /api/v1/notifications/count every 30 seconds
 * and displays a dropdown with unread notifications.
 */
export default function NotificationBell() {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [unreadCount, setUnreadCount] = useState(0);
  const [notifications, setNotifications] = useState([]);
  const [showDropdown, setShowDropdown] = useState(false);
  const [loading, setLoading] = useState(false);
  const dropdownRef = useRef(null);

  // Poll unread count every 30 seconds
  useEffect(() => {
    if (!isAuthenticated) return;
    
    async function fetchCount() {
      try {
        const token = localStorage.getItem('nova_access_token');
        const tenant = localStorage.getItem('nova_tenant');
        const res = await fetch(`${API_BASE}/notifications/count`, {
          headers: {
            'Authorization': `Bearer ${token}`,
            'X-Tenant-ID': tenant || '',
          },
        });
        if (res.ok) {
          const data = await res.json();
          setUnreadCount(data.unreadCount || 0);
        }
      } catch { /* ignore polling errors */ }
    }

    fetchCount();
    const interval = setInterval(fetchCount, 30000);
    return () => clearInterval(interval);
  }, [isAuthenticated]);

  // Close dropdown on outside click
  useEffect(() => {
    function handleClick(e) {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setShowDropdown(false);
      }
    }
    document.addEventListener('mousedown', handleClick);
    return () => document.removeEventListener('mousedown', handleClick);
  }, []);

  async function fetchNotifications() {
    setLoading(true);
    try {
      const token = localStorage.getItem('nova_access_token');
      const tenant = localStorage.getItem('nova_tenant');
      const res = await fetch(`${API_BASE}/notifications?size=10`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'X-Tenant-ID': tenant || '',
        },
      });
      if (res.ok) {
        const data = await res.json();
        setNotifications(data.content || []);
      }
    } catch { /* ignore */ }
    setLoading(false);
  }

  async function markAllRead() {
    try {
      const token = localStorage.getItem('nova_access_token');
      const tenant = localStorage.getItem('nova_tenant');
      await fetch(`${API_BASE}/notifications/read-all`, {
        method: 'PATCH',
        headers: {
          'Authorization': `Bearer ${token}`,
          'X-Tenant-ID': tenant || '',
        },
      });
      setUnreadCount(0);
      setNotifications(n => n.map(x => ({ ...x, read: true })));
    } catch { /* ignore */ }
  }

  function toggleDropdown() {
    if (!showDropdown) {
      fetchNotifications();
    }
    setShowDropdown(!showDropdown);
  }

  function handleNotificationClick(notification) {
    if (notification.link) {
      navigate(notification.link);
      setShowDropdown(false);
    }
  }

  if (!isAuthenticated) return null;

  const typeIcons = {
    POST_SUBMITTED: '📝',
    POST_APPROVED: '✅',
    POST_REJECTED: '↩️',
    POST_PUBLISHED: '🚀',
    POST_SCHEDULED: '⏰',
    ROLE_CHANGED: '🔑',
    SYSTEM: '⚙️',
  };

  function timeAgo(dateStr) {
    const diff = Date.now() - new Date(dateStr).getTime();
    const mins = Math.floor(diff / 60000);
    if (mins < 1) return 'just now';
    if (mins < 60) return `${mins}m ago`;
    const hrs = Math.floor(mins / 60);
    if (hrs < 24) return `${hrs}h ago`;
    return `${Math.floor(hrs / 24)}d ago`;
  }

  return (
    <div className="relative" ref={dropdownRef}>
      <button
        onClick={toggleDropdown}
        className="relative p-2 rounded-full hover:bg-gray-100 transition-colors"
        aria-label="Notifications"
      >
        <Bell size={18} />
        {unreadCount > 0 && (
          <span className="absolute -top-0.5 -right-0.5 bg-red-500 text-white text-[10px] font-bold w-5 h-5 rounded-full flex items-center justify-center">
            {unreadCount > 9 ? '9+' : unreadCount}
          </span>
        )}
      </button>

      {showDropdown && (
        <div className="absolute right-0 top-11 bg-white rounded-2xl shadow-xl border border-gray-100 w-80 z-50 overflow-hidden">
          {/* Header */}
          <div className="flex items-center justify-between px-4 py-3 border-b border-gray-100">
            <h3 className="text-sm font-black">Notifications</h3>
            {unreadCount > 0 && (
              <button
                onClick={markAllRead}
                className="flex items-center gap-1 text-xs font-bold text-blue-600 hover:text-blue-800"
              >
                <Check size={12} /> Mark all read
              </button>
            )}
          </div>

          {/* List */}
          <div className="max-h-80 overflow-y-auto">
            {loading ? (
              <div className="p-6 text-center text-gray-400 text-sm font-semibold">Loading...</div>
            ) : notifications.length === 0 ? (
              <div className="p-6 text-center">
                <div className="text-3xl mb-2">🔔</div>
                <p className="text-gray-400 text-sm font-semibold">No notifications yet</p>
              </div>
            ) : (
              notifications.map((n) => (
                <button
                  key={n.id}
                  onClick={() => handleNotificationClick(n)}
                  className={`w-full text-left px-4 py-3 hover:bg-gray-50 transition-colors border-b border-gray-50 ${!n.read ? 'bg-blue-50/40' : ''}`}
                >
                  <div className="flex items-start gap-3">
                    <span className="text-lg mt-0.5">{typeIcons[n.type] || '📌'}</span>
                    <div className="flex-1 min-w-0">
                      <p className={`text-sm ${!n.read ? 'font-bold' : 'font-semibold'} truncate`}>
                        {n.title}
                      </p>
                      {n.message && (
                        <p className="text-xs text-gray-500 font-medium mt-0.5 line-clamp-2">
                          {n.message}
                        </p>
                      )}
                      <p className="text-[10px] text-gray-400 font-bold mt-1">
                        {timeAgo(n.createdAt)}
                      </p>
                    </div>
                    {n.link && <ExternalLink size={12} className="text-gray-300 mt-1 flex-shrink-0" />}
                  </div>
                </button>
              ))
            )}
          </div>
        </div>
      )}
    </div>
  );
}
