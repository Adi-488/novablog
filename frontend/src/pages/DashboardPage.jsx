import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { PenLine, ArrowRight, FileText, Clock, CheckCircle, Archive, Send, Eye, MoreHorizontal, Trash2 } from 'lucide-react';
import { postsApi } from '../api/client';
import { useAuth } from '../context/AuthContext';
import { StatusBadge } from '../components/PostCards';

const STATUS_TABS = [
  { key: null, label: 'All', icon: FileText },
  { key: 'DRAFT', label: 'Drafts', icon: PenLine },
  { key: 'UNDER_REVIEW', label: 'In Review', icon: Send },
  { key: 'PUBLISHED', label: 'Published', icon: CheckCircle },
  { key: 'SCHEDULED', label: 'Scheduled', icon: Clock },
  { key: 'ARCHIVED', label: 'Archived', icon: Archive },
];

export default function DashboardPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [activeMenu, setActiveMenu] = useState(null);

  useEffect(() => {
    loadPosts();
  }, [statusFilter, page]);

  async function loadPosts() {
    setLoading(true);
    try {
      const data = await postsApi.listMine(statusFilter, page);
      setPosts(data.content || []);
      setTotalPages(data.totalPages || 0);
    } catch (err) {
      console.error('Failed to load posts:', err);
      setPosts([]);
    }
    setLoading(false);
  }

  async function handleStatusChange(postId, newStatus) {
    try {
      await postsApi.changeStatus(postId, newStatus);
      loadPosts();
    } catch (err) {
      alert(err.message);
    }
    setActiveMenu(null);
  }

  async function handleDelete(postId) {
    if (!confirm('Are you sure? This will archive the post.')) return;
    try {
      await postsApi.delete(postId);
      loadPosts();
    } catch (err) {
      alert(err.message);
    }
    setActiveMenu(null);
  }

  function formatDate(dateStr) {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('en-US', {
      month: 'short', day: 'numeric', year: 'numeric',
    });
  }

  return (
    <div>
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-end justify-between mb-8 gap-4">
        <div>
          <h1 className="text-4xl md:text-5xl font-black tracking-tighter">
            Welcome back{user?.displayName ? `, ${user.displayName.split(' ')[0]}` : ''}
          </h1>
          <p className="text-gray-500 font-semibold mt-1">Manage your posts and content.</p>
        </div>
        <Link
          to="/editor"
          className="inline-flex items-center gap-2 bg-black text-white px-6 py-3 rounded-full text-sm font-bold hover:bg-gray-800 transition-colors shrink-0"
        >
          <PenLine size={16} /> New Post
        </Link>
      </div>

      {/* Quick Stats */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
        {[
          { label: 'Total Posts', value: posts.length, color: '#e6faaf' },
          { label: 'Published', value: posts.filter(p => p.status === 'PUBLISHED').length, color: '#dcfce7' },
          { label: 'Drafts', value: posts.filter(p => p.status === 'DRAFT').length, color: '#e0f2fe' },
          { label: 'In Review', value: posts.filter(p => p.status === 'UNDER_REVIEW').length, color: '#fef08a' },
        ].map(stat => (
          <div key={stat.label} className="rounded-[2rem] p-5" style={{ backgroundColor: stat.color }}>
            <div className="text-3xl font-black">{stat.value}</div>
            <div className="text-xs font-bold text-gray-600 uppercase mt-1">{stat.label}</div>
          </div>
        ))}
      </div>

      {/* Status Tabs */}
      <div className="flex items-center gap-2 mb-6 overflow-x-auto pb-2">
        {STATUS_TABS.map(tab => {
          const Icon = tab.icon;
          const isActive = statusFilter === tab.key;
          return (
            <button
              key={tab.label}
              onClick={() => { setStatusFilter(tab.key); setPage(0); }}
              className={`flex items-center gap-2 px-4 py-2.5 rounded-full text-xs font-bold transition-colors whitespace-nowrap ${
                isActive ? 'bg-black text-white' : 'bg-white text-gray-500 hover:bg-gray-100'
              }`}
            >
              <Icon size={14} /> {tab.label}
            </button>
          );
        })}
      </div>

      {/* Posts Table */}
      {loading ? (
        <div className="bg-white rounded-[2rem] p-6 space-y-4">
          {[...Array(5)].map((_, i) => (
            <div key={i} className="h-14 bg-gray-50 rounded-2xl animate-pulse"></div>
          ))}
        </div>
      ) : posts.length === 0 ? (
        <div className="bg-white rounded-[2rem] p-12 text-center">
          <div className="text-5xl mb-4">✍️</div>
          <h2 className="text-xl font-black mb-2">No posts yet</h2>
          <p className="text-gray-500 font-semibold text-sm mb-6">
            Start writing your first post to see it here.
          </p>
          <Link
            to="/editor"
            className="inline-flex items-center gap-2 bg-black text-white px-6 py-3 rounded-full text-sm font-bold hover:bg-gray-800 transition-colors"
          >
            <PenLine size={14} /> Create Your First Post
          </Link>
        </div>
      ) : (
        <div className="bg-white rounded-[2rem] overflow-hidden shadow-sm">
          {/* Table Header */}
          <div className="hidden md:grid grid-cols-12 gap-4 px-6 py-3 border-b border-gray-100 text-xs font-bold text-gray-400 uppercase tracking-wider">
            <div className="col-span-5">Title</div>
            <div className="col-span-2">Status</div>
            <div className="col-span-2">Created</div>
            <div className="col-span-2">Published</div>
            <div className="col-span-1"></div>
          </div>

          {/* Rows */}
          {posts.map(post => (
            <div
              key={post.id}
              className="grid grid-cols-1 md:grid-cols-12 gap-2 md:gap-4 px-6 py-4 border-b border-gray-50 hover:bg-gray-50/50 transition-colors items-center relative"
            >
              <div className="md:col-span-5">
                <Link
                  to={`/editor/${post.id}`}
                  className="font-bold text-sm hover:underline decoration-2 underline-offset-4 line-clamp-1"
                >
                  {post.title}
                </Link>
                {post.tags?.length > 0 && (
                  <div className="flex gap-1 mt-1">
                    {post.tags.slice(0, 3).map(t => (
                      <span key={t} className="text-[10px] font-bold text-gray-400 bg-gray-100 px-2 py-0.5 rounded-full">
                        {t}
                      </span>
                    ))}
                  </div>
                )}
              </div>

              <div className="md:col-span-2">
                <StatusBadge status={post.status} />
              </div>

              <div className="md:col-span-2 text-xs font-semibold text-gray-400">
                {formatDate(post.createdAt)}
              </div>

              <div className="md:col-span-2 text-xs font-semibold text-gray-400">
                {formatDate(post.publishedAt)}
              </div>

              <div className="md:col-span-1 flex justify-end relative">
                <button
                  onClick={() => setActiveMenu(activeMenu === post.id ? null : post.id)}
                  className="w-8 h-8 flex items-center justify-center rounded-full hover:bg-gray-100 transition-colors"
                >
                  <MoreHorizontal size={16} />
                </button>

                {activeMenu === post.id && (
                  <div className="absolute right-0 top-10 bg-white rounded-2xl shadow-xl border border-gray-100 py-2 z-50 w-48">
                    <button
                      onClick={() => navigate(`/editor/${post.id}`)}
                      className="flex items-center gap-3 w-full px-4 py-2.5 text-sm font-semibold hover:bg-gray-50 transition-colors"
                    >
                      <PenLine size={14} /> Edit
                    </button>
                    <button
                      onClick={() => navigate(`/post/${post.slug || post.id}`)}
                      className="flex items-center gap-3 w-full px-4 py-2.5 text-sm font-semibold hover:bg-gray-50 transition-colors"
                    >
                      <Eye size={14} /> View
                    </button>

                    {post.status === 'DRAFT' && (
                      <button
                        onClick={() => handleStatusChange(post.id, 'UNDER_REVIEW')}
                        className="flex items-center gap-3 w-full px-4 py-2.5 text-sm font-semibold hover:bg-gray-50 transition-colors text-blue-600"
                      >
                        <Send size={14} /> Submit for Review
                      </button>
                    )}
                    {post.status === 'UNDER_REVIEW' && (
                      <button
                        onClick={() => handleStatusChange(post.id, 'PUBLISHED')}
                        className="flex items-center gap-3 w-full px-4 py-2.5 text-sm font-semibold hover:bg-gray-50 transition-colors text-green-600"
                      >
                        <CheckCircle size={14} /> Publish Now
                      </button>
                    )}

                    <div className="border-t border-gray-100 my-1"></div>
                    <button
                      onClick={() => handleDelete(post.id)}
                      className="flex items-center gap-3 w-full px-4 py-2.5 text-sm font-semibold hover:bg-red-50 transition-colors text-red-500"
                    >
                      <Trash2 size={14} /> Delete
                    </button>
                  </div>
                )}
              </div>
            </div>
          ))}

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="flex items-center justify-center gap-3 px-6 py-4 border-t border-gray-100">
              <button
                disabled={page === 0}
                onClick={() => setPage(p => p - 1)}
                className="px-4 py-2 rounded-full text-xs font-bold bg-gray-100 hover:bg-gray-200 disabled:opacity-40"
              >
                Previous
              </button>
              <span className="text-xs font-bold text-gray-400">
                Page {page + 1} of {totalPages}
              </span>
              <button
                disabled={page >= totalPages - 1}
                onClick={() => setPage(p => p + 1)}
                className="px-4 py-2 rounded-full text-xs font-bold bg-gray-100 hover:bg-gray-200 disabled:opacity-40"
              >
                Next
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
