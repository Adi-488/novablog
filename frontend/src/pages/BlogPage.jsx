import React, { useState, useEffect } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { Search, ArrowRight } from 'lucide-react';
import { postsApi } from '../api/client';
import { useAuth } from '../context/AuthContext';
import { PostCard, StatusBadge, TagPill } from '../components/PostCards';

const PASTEL_COLORS = ['#e6faaf', '#c3d8ea', '#dcbcf6', '#fef08a', '#fce7f3', '#e0f2fe', '#dcfce7'];

export default function BlogPage() {
  const { isAuthenticated } = useAuth();
  const [searchParams, setSearchParams] = useSearchParams();
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [query, setQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('PUBLISHED');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => {
    loadPosts();
  }, [statusFilter, page, isAuthenticated]);

  async function loadPosts() {
    setLoading(true);
    try {
      const data = await postsApi.list(statusFilter, page, 12);
      setPosts(data.content || []);
      setTotalPages(data.totalPages || 0);
    } catch (err) {
      console.log('Could not load posts:', err.message);
      setPosts([]);
    }
    setLoading(false);
  }

  async function handleSearch(e) {
    e.preventDefault();
    if (!query.trim()) {
      loadPosts();
      return;
    }
    setLoading(true);
    try {
      const data = await postsApi.search(query, 0);
      setPosts(data.content || []);
      setTotalPages(data.totalPages || 0);
    } catch (err) {
      console.log('Search failed:', err.message);
    }
    setLoading(false);
  }

  const statuses = ['PUBLISHED', 'DRAFT', 'UNDER_REVIEW', 'SCHEDULED', 'ARCHIVED'];

  return (
    <div>
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-end justify-between mb-8 gap-4">
        <div>
          <h1 className="text-5xl md:text-7xl font-black tracking-tighter uppercase">Blog</h1>
          <p className="text-gray-500 font-semibold mt-2">
            Explore stories, ideas, and insights from Nova writers.
          </p>
        </div>

        {/* Search */}
        <form onSubmit={handleSearch} className="flex items-center gap-2">
          <div className="relative">
            <Search size={16} className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" />
            <input
              type="text"
              placeholder="Search posts..."
              value={query}
              onChange={e => setQuery(e.target.value)}
              className="pl-10 pr-4 py-3 bg-white rounded-full text-sm font-semibold border-0 shadow-sm focus:ring-2 focus:ring-black/10 outline-none w-64"
            />
          </div>
          <button
            type="submit"
            className="bg-black text-white px-5 py-3 rounded-full text-sm font-bold hover:bg-gray-800 transition-colors"
          >
            Search
          </button>
        </form>
      </div>

      {/* Status Filter Tabs */}
      {isAuthenticated && (
        <div className="flex items-center gap-3 mb-8 flex-wrap">
          <span className="text-sm font-bold text-gray-400">Filter:</span>
          {statuses.map(s => (
            <button
              key={s}
              onClick={() => { setStatusFilter(s); setPage(0); }}
              className={`px-4 py-2 rounded-full text-xs font-bold transition-colors ${
                statusFilter === s
                  ? 'bg-black text-white'
                  : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              }`}
            >
              {s.replace('_', ' ')}
            </button>
          ))}
        </div>
      )}

      {/* Posts Grid */}
      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {[...Array(6)].map((_, i) => (
            <div key={i} className="bg-white rounded-[2.5rem] h-72 animate-pulse"></div>
          ))}
        </div>
      ) : posts.length === 0 ? (
        <div className="text-center py-20">
          <div className="text-6xl mb-4">📝</div>
          <h2 className="text-2xl font-black mb-2">No posts yet</h2>
          <p className="text-gray-500 font-semibold mb-6">
            {isAuthenticated
              ? 'Create your first post to see it here.'
              : 'Log in and start writing to see posts here.'}
          </p>
          <Link
            to={isAuthenticated ? '/editor' : '/login'}
            className="inline-flex items-center gap-2 bg-black text-white px-6 py-3 rounded-full text-sm font-bold hover:bg-gray-800 transition-colors"
          >
            {isAuthenticated ? 'Create Post' : 'Get Started'} <ArrowRight size={14} />
          </Link>
        </div>
      ) : (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 auto-rows-[280px]">
            {posts.map((post, i) => (
              <PostCard
                key={post.id}
                post={post}
                color={PASTEL_COLORS[i % PASTEL_COLORS.length]}
              />
            ))}
          </div>

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="flex items-center justify-center gap-3 mt-10">
              <button
                disabled={page === 0}
                onClick={() => setPage(p => p - 1)}
                className="px-5 py-2.5 rounded-full text-sm font-bold bg-gray-100 hover:bg-gray-200 disabled:opacity-40 transition-colors"
              >
                Previous
              </button>
              <span className="text-sm font-bold text-gray-500">
                Page {page + 1} of {totalPages}
              </span>
              <button
                disabled={page >= totalPages - 1}
                onClick={() => setPage(p => p + 1)}
                className="px-5 py-2.5 rounded-full text-sm font-bold bg-gray-100 hover:bg-gray-200 disabled:opacity-40 transition-colors"
              >
                Next
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
