import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { ArrowLeft, Clock, User, Tag } from 'lucide-react';
import { postsApi, getStoredTenant } from '../api/client';
import { StatusBadge } from '../components/PostCards';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';

/**
 * Post detail page — displays a single post by slug or ID.
 */
export default function PostPage() {
  const { slugOrId } = useParams();
  const navigate = useNavigate();
  const [post, setPost] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    async function loadPost() {
      setLoading(true);
      try {
        // Try slug first, then ID
        let data;
        try {
          data = await postsApi.getBySlug(slugOrId);
        } catch {
          data = await postsApi.getById(slugOrId);
        }
        setPost(data);
      } catch (err) {
        setError('Post not found');
      }
      setLoading(false);
    }
    loadPost();
  }, [slugOrId]);

  if (loading) {
    return (
      <div className="max-w-3xl mx-auto">
        <div className="bg-white rounded-[2.5rem] p-10 animate-pulse">
          <div className="h-10 bg-gray-100 rounded-full w-2/3 mb-4"></div>
          <div className="h-4 bg-gray-100 rounded-full w-1/3 mb-8"></div>
          <div className="space-y-3">
            {[...Array(8)].map((_, i) => (
              <div key={i} className="h-4 bg-gray-100 rounded-full" style={{ width: `${70 + Math.random() * 30}%` }}></div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  if (error || !post) {
    return (
      <div className="text-center py-20">
        <div className="text-6xl mb-4">🔍</div>
        <h2 className="text-2xl font-black mb-2">Post Not Found</h2>
        <p className="text-gray-500 font-semibold mb-6">{error}</p>
        <button
          onClick={() => navigate(-1)}
          className="inline-flex items-center gap-2 bg-black text-white px-6 py-3 rounded-full text-sm font-bold hover:bg-gray-800 transition-colors"
        >
          <ArrowLeft size={14} /> Go Back
        </button>
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto">
      {/* Back Button */}
      <button
        onClick={() => navigate(-1)}
        className="flex items-center gap-2 text-gray-500 font-bold text-sm mb-6 hover:text-black transition-colors"
      >
        <ArrowLeft size={16} /> Back
      </button>

      {/* Article Card */}
      <article className="bg-white rounded-[2.5rem] p-8 lg:p-12 shadow-sm">
        {/* Meta */}
        <div className="flex items-center gap-3 mb-6 flex-wrap">
          <StatusBadge status={post.status} />
          {post.tags?.map(tag => (
            <Link
              key={tag}
              to={`/blog?tag=${tag}`}
              className="flex items-center gap-1 px-3 py-1 bg-[#e0f2fe] rounded-full text-[10px] font-bold text-blue-700 hover:bg-blue-200 transition-colors"
            >
              <Tag size={10} /> {tag}
            </Link>
          ))}
        </div>

        {/* Title */}
        <h1 className="text-4xl lg:text-5xl font-black tracking-tight leading-tight mb-6">
          {post.title}
        </h1>

        {/* Author & Date */}
        <div className="flex items-center gap-6 text-sm text-gray-500 font-semibold mb-10 pb-8 border-b border-gray-100">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 bg-gray-200 rounded-full flex items-center justify-center">
              <User size={14} />
            </div>
            <span>{post.authorName}</span>
          </div>
          <div className="flex items-center gap-2">
            <Clock size={14} />
            <span>{new Date(post.publishedAt || post.createdAt).toLocaleDateString('en-US', {
              year: 'numeric', month: 'long', day: 'numeric'
            })}</span>
          </div>
        </div>

        {/* Body */}
        <div className="prose prose-lg max-w-none font-medium leading-relaxed text-gray-800">
          {post.body ? (
            <ReactMarkdown 
              remarkPlugins={[remarkGfm]}
              components={{
                img: ({node, ...props}) => {
                  let src = props.src;
                  const currentTenant = getStoredTenant ? getStoredTenant() : localStorage.getItem('nova_tenant');
                  const tenantId = currentTenant ? currentTenant.replace('tenant_', '') : '';
                  if (src && src.includes('/api/v1/media/') && !src.includes('tenantId=') && tenantId) {
                    src = src + (src.includes('?') ? '&' : '?') + 'tenantId=' + tenantId;
                  }
                  return <img className="rounded-2xl shadow-sm border border-gray-100 max-w-full h-auto my-6" {...props} src={src} />;
                },
                a: ({node, ...props}) => <a className="text-blue-600 hover:underline" {...props} />,
                h1: ({node, ...props}) => <h1 className="text-3xl font-black mt-8 mb-4" {...props} />,
                h2: ({node, ...props}) => <h2 className="text-2xl font-bold mt-8 mb-4" {...props} />,
                h3: ({node, ...props}) => <h3 className="text-xl font-bold mt-6 mb-3" {...props} />,
                p: ({node, ...props}) => <p className="mb-4" {...props} />,
                ul: ({node, ...props}) => <ul className="list-disc pl-6 mb-4 space-y-2" {...props} />,
                ol: ({node, ...props}) => <ol className="list-decimal pl-6 mb-4 space-y-2" {...props} />,
                blockquote: ({node, ...props}) => <blockquote className="border-l-4 border-gray-200 pl-4 italic text-gray-500 my-4" {...props} />
              }}
            >
              {post.body}
            </ReactMarkdown>
          ) : (
            <p className="text-gray-400 italic">No content yet.</p>
          )}
        </div>

        {/* SEO Description */}
        {post.seoDescription && (
          <div className="mt-10 pt-8 border-t border-gray-100">
            <p className="text-sm text-gray-400 font-semibold italic">
              {post.seoDescription}
            </p>
          </div>
        )}
      </article>
    </div>
  );
}
