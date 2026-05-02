import React from 'react';
import { ArrowRight } from 'lucide-react';
import { Link } from 'react-router-dom';

/**
 * Formats an ISO date string to a human-readable short format.
 */
function formatDate(dateStr) {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  return d.toLocaleDateString('en-US', { day: 'numeric', month: 'short' });
}

/**
 * A status badge with color coding.
 */
export function StatusBadge({ status }) {
  const colors = {
    DRAFT: 'bg-gray-200 text-gray-700',
    UNDER_REVIEW: 'bg-yellow-200 text-yellow-800',
    SCHEDULED: 'bg-blue-200 text-blue-800',
    PUBLISHED: 'bg-green-200 text-green-800',
    ARCHIVED: 'bg-red-200 text-red-800',
  };

  return (
    <span className={`px-3 py-1 rounded-full text-[10px] font-bold uppercase ${colors[status] || 'bg-gray-200 text-gray-600'}`}>
      {status?.replace('_', ' ')}
    </span>
  );
}

/**
 * A large featured post card with image background.
 */
export function FeaturedPostCard({ post }) {
  const imageUrl = `https://images.unsplash.com/photo-1499750310107-5fef28a66643?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80`;

  return (
    <Link
      to={`/post/${post.slug || post.id}`}
      className="md:col-span-5 md:row-span-2 bg-white rounded-[2.5rem] relative overflow-hidden group block"
    >
      <img
        src={imageUrl}
        alt={post.title}
        className="absolute inset-0 w-full h-full object-cover transition-transform duration-700 group-hover:scale-105"
      />
      <div className="absolute bottom-0 left-0 w-3/4">
        <div className="bg-white rounded-tr-[2.5rem] pt-6 pr-6 pb-6">
          <div className="flex items-center gap-2 text-xs font-bold text-gray-400 mb-2">
            {post.tags?.length > 0 && (
              <span className="text-black">{post.tags[0]}</span>
            )}
            <span className="w-1 h-1 bg-gray-300 rounded-full"></span>
            <span>{formatDate(post.publishedAt || post.createdAt)}</span>
          </div>
          <h2 className="text-2xl lg:text-3xl font-black tracking-tight leading-none uppercase line-clamp-2">
            {post.title}
          </h2>
        </div>
      </div>
    </Link>
  );
}

/**
 * A standard post card for the bento grid — green accent style.
 */
export function PostCard({ post, color = '#e6faaf' }) {
  return (
    <Link
      to={`/post/${post.slug || post.id}`}
      className="bento-card flex flex-col justify-between p-6 lg:p-8 group relative overflow-hidden"
      style={{ backgroundColor: color }}
    >
      <div className="absolute top-0 right-0 w-64 h-64 bg-white/30 rounded-full blur-3xl -mr-20 -mt-20 pointer-events-none"></div>

      <div className="flex justify-between items-start z-10 relative">
        <div className="flex items-center gap-2 text-xs font-bold text-gray-700">
          {post.tags?.length > 0 && <span>{post.tags[0]}</span>}
          <StatusBadge status={post.status} />
        </div>
      </div>

      <div className="z-10 relative mt-4 flex-1">
        <h3 className="text-2xl lg:text-3xl font-black leading-tight uppercase mb-3 tracking-tight line-clamp-4">
          {post.title}
        </h3>
        {post.seoDescription && (
          <p className="text-xs lg:text-sm font-semibold text-gray-700 line-clamp-2 max-w-[85%]">
            {post.seoDescription}
          </p>
        )}
      </div>

      <div className="flex items-center justify-between mt-4 z-10 relative border-t border-black/10 pt-3">
        <span className="text-xs font-bold text-gray-500">
          by {post.authorName} • {formatDate(post.createdAt)}
        </span>
        <ArrowRight size={16} strokeWidth={2.5} className="text-gray-600" />
      </div>
    </Link>
  );
}

/**
 * A compact post row for list views.
 */
export function PostRow({ post, onClick }) {
  return (
    <button
      onClick={onClick}
      className="flex items-center justify-between w-full border-t border-black/10 pt-3 pb-1 hover:bg-black/[0.02] rounded-lg px-2 -mx-2 transition-colors"
    >
      <div className="flex items-center gap-3 flex-1 min-w-0">
        <StatusBadge status={post.status} />
        <span className="text-sm font-bold uppercase truncate">{post.title}</span>
      </div>
      <ArrowRight size={16} strokeWidth={2.5} className="text-gray-600 shrink-0" />
    </button>
  );
}

/**
 * Tag pill display.
 */
export function TagPill({ name, active, onClick }) {
  const pastelColors = ['#fef08a', '#e0f2fe', '#dcfce7', '#fae8ff', '#fce7f3'];
  const color = active ? '#1a1a1a' : pastelColors[Math.abs(name.charCodeAt(0)) % pastelColors.length];

  return (
    <button
      onClick={onClick}
      className={`px-3 py-1.5 rounded-full text-[10px] font-bold transition-colors ${
        active
          ? 'bg-black text-white'
          : 'hover:opacity-80'
      }`}
      style={!active ? { backgroundColor: color, color: '#1a1a1a' } : {}}
    >
      {name}
    </button>
  );
}
