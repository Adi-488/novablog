import React from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { User, Mail, Shield, Building2, LogOut } from 'lucide-react';

export default function ProfilePage() {
  const { user, tenant, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate('/');
  }

  const fields = [
    { icon: User, label: 'Display Name', value: user?.displayName || '—' },
    { icon: Mail, label: 'Email', value: user?.email || '—' },
    { icon: Shield, label: 'Role', value: user?.role || '—' },
    { icon: Building2, label: 'Tenant', value: tenant ? `${tenant}.novablog.dev` : '—' },
  ];

  return (
    <div className="max-w-xl mx-auto">
      <h1 className="text-4xl font-black tracking-tighter mb-8">Profile</h1>

      {/* Avatar Card */}
      <div className="bg-white rounded-[2.5rem] p-8 shadow-sm mb-6 flex items-center gap-6">
        <div className="w-20 h-20 bg-gray-200 rounded-full overflow-hidden shrink-0">
          <img
            src={user?.avatarUrl || `https://ui-avatars.com/api/?name=${encodeURIComponent(user?.displayName || 'U')}&background=000&color=fff&bold=true&size=160`}
            alt="Avatar"
            className="w-full h-full object-cover"
          />
        </div>
        <div>
          <h2 className="text-2xl font-black">{user?.displayName || 'User'}</h2>
          <p className="text-gray-500 font-semibold text-sm">{user?.email}</p>
          <span className="inline-block mt-2 bg-[#dcfce7] text-green-700 text-[10px] font-bold uppercase px-3 py-1 rounded-full">
            {user?.role || 'MEMBER'}
          </span>
        </div>
      </div>

      {/* Info Card */}
      <div className="bg-white rounded-[2.5rem] p-6 shadow-sm mb-6">
        {fields.map((field, i) => {
          const Icon = field.icon;
          return (
            <div
              key={field.label}
              className={`flex items-center gap-4 py-4 ${i < fields.length - 1 ? 'border-b border-gray-50' : ''}`}
            >
              <div className="w-10 h-10 bg-gray-100 rounded-full flex items-center justify-center shrink-0">
                <Icon size={16} className="text-gray-500" />
              </div>
              <div className="flex-1">
                <div className="text-[10px] font-bold uppercase text-gray-400 tracking-wider">{field.label}</div>
                <div className="text-sm font-bold">{field.value}</div>
              </div>
            </div>
          );
        })}
      </div>

      {/* Logout */}
      <button
        onClick={handleLogout}
        className="flex items-center justify-center gap-2 w-full py-4 bg-red-50 text-red-600 rounded-full font-bold text-sm hover:bg-red-100 transition-colors"
      >
        <LogOut size={16} /> Sign Out
      </button>
    </div>
  );
}
