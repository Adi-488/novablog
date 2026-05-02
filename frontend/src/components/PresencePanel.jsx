import React from 'react';
import { Wifi, WifiOff } from 'lucide-react';

/**
 * Presence panel showing connected collaborators on a post.
 * Sprint 4: US-008 — See live collaborators on a post.
 */
export default function PresencePanel({ presence, connected }) {
  if (!presence || presence.length === 0) {
    return null;
  }

  return (
    <div className="flex items-center gap-2">
      {/* Connection indicator */}
      <div className={`flex items-center gap-1 text-[10px] font-bold px-2 py-1 rounded-full ${
        connected 
          ? 'bg-green-100 text-green-700' 
          : 'bg-red-100 text-red-600'
      }`}>
        {connected ? <Wifi size={10} /> : <WifiOff size={10} />}
        {connected ? 'Live' : 'Offline'}
      </div>

      {/* Avatars */}
      <div className="flex -space-x-2">
        {presence.map((user, i) => (
          <div
            key={user.userId}
            className="relative group"
          >
            <div
              className="w-7 h-7 rounded-full border-2 border-white flex items-center justify-center text-[10px] font-bold text-white cursor-default"
              style={{ backgroundColor: user.color || '#3B82F6', zIndex: presence.length - i }}
              title={user.userName}
            >
              {user.avatarUrl ? (
                <img src={user.avatarUrl} alt={user.userName} className="w-full h-full rounded-full object-cover" />
              ) : (
                (user.userName || '?')[0].toUpperCase()
              )}
            </div>
            
            {/* Tooltip */}
            <div className="absolute bottom-full left-1/2 -translate-x-1/2 mb-1 opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none">
              <div className="bg-gray-900 text-white text-[10px] font-bold px-2 py-1 rounded-lg whitespace-nowrap">
                {user.userName}
              </div>
            </div>
          </div>
        ))}
      </div>

      {presence.length > 0 && (
        <span className="text-[10px] font-bold text-gray-400">
          {presence.length} editing
        </span>
      )}
    </div>
  );
}
