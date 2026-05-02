import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight, ArrowUpRight, Play, TrendingUp, Users, FileText } from 'lucide-react';
import { postsApi } from '../api/client';
import { useAuth } from '../context/AuthContext';
import { FeaturedPostCard, PostCard, PostRow, TagPill } from '../components/PostCards';

/**
 * Home page — the main bento grid blog layout.
 * Shows published posts from the backend, or placeholder content when unauthenticated / no posts.
 */
export default function HomePage() {
  const { isAuthenticated } = useAuth();
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadPosts() {
      try {
        const data = await postsApi.list('PUBLISHED', 0, 6);
        setPosts(data.content || []);
      } catch (err) {
        console.log('Posts not available yet:', err.message);
        setPosts([]);
      }
      setLoading(false);
    }

    if (isAuthenticated) {
      loadPosts();
    } else {
      setLoading(false);
    }
  }, [isAuthenticated]);

  // Sample data for when there are no real posts yet
  const samplePosts = [
    { id: '1', title: 'Getting Started with NovaBlog', slug: 'getting-started', status: 'PUBLISHED', tags: ['Tutorial'], authorName: 'Admin', seoDescription: 'Learn how to set up your multi-tenant blog with NovaBlog in minutes.', createdAt: new Date().toISOString() },
    { id: '2', title: 'The Future of Content Management', slug: 'future-cms', status: 'PUBLISHED', tags: ['Technology'], authorName: 'Admin', seoDescription: 'How modern SaaS platforms are reshaping the way we create and publish.', createdAt: new Date().toISOString() },
    { id: '3', title: 'Building Beautiful Blogs', slug: 'beautiful-blogs', status: 'PUBLISHED', tags: ['Design'], authorName: 'Admin', createdAt: new Date().toISOString() },
  ];

  const displayPosts = posts.length > 0 ? posts : samplePosts;
  const featured = displayPosts[0];
  const remaining = displayPosts.slice(1);

  const sampleTags = ['Technology', 'Design', 'Tutorial', 'Lifestyle', 'Product', 'Engineering', 'Culture', 'News'];

  return (
    <div>
      {/* Header */}
      <div className="flex items-end justify-between mb-8">
        <h1 className="text-6xl md:text-8xl font-black tracking-tighter uppercase">BLOG</h1>
        <Link
          to="/blog"
          className="flex items-center gap-2 bg-gray-200 hover:bg-gray-300 transition-colors px-6 py-3 rounded-full font-bold text-sm"
        >
          Read Our Blog <ArrowRight size={16} strokeWidth={3} />
        </Link>
      </div>

      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-12 gap-6 auto-rows-[250px]">
          {[...Array(4)].map((_, i) => (
            <div key={i} className={`${i === 0 ? 'md:col-span-5 md:row-span-2' : 'md:col-span-5'} bg-white rounded-[2.5rem] animate-pulse`}></div>
          ))}
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-12 gap-6 auto-rows-[250px]">
          {/* Left: Featured Post */}
          {featured && <FeaturedPostCard post={featured} />}

          {/* Middle Top: Content Card */}
          <div className="md:col-span-5 md:row-span-1 bg-[#e6faaf] rounded-[2.5rem] p-6 lg:p-8 flex flex-col justify-between group relative overflow-hidden">
            <div className="absolute top-0 right-0 w-64 h-64 bg-white/30 rounded-full blur-3xl -mr-20 -mt-20 pointer-events-none"></div>

            <div className="flex justify-between items-start z-10 relative">
              <div className="text-xs font-bold text-gray-800">Latest Posts</div>
              <Link
                to="/blog"
                className="w-10 h-10 bg-white/50 hover:bg-white rounded-full flex items-center justify-center transition-colors"
              >
                <ArrowUpRight size={20} strokeWidth={2.5} />
              </Link>
            </div>

            <div className="z-10 relative mt-4 flex-1">
              {remaining[0] && (
                <Link to={`/post/${remaining[0].slug || remaining[0].id}`}>
                  <h3 className="text-3xl lg:text-4xl font-black leading-tight uppercase mb-3 tracking-tight line-clamp-4 hover:underline decoration-2 underline-offset-4">
                    {remaining[0].title}
                  </h3>
                </Link>
              )}
              {remaining[0]?.seoDescription && (
                <p className="text-xs lg:text-sm font-semibold text-gray-700 line-clamp-2 max-w-[85%]">
                  {remaining[0].seoDescription}
                </p>
              )}
            </div>

            <div className="mt-6 z-10 relative flex flex-col gap-3">
              {remaining.slice(1, 3).map(post => (
                <PostRow key={post.id} post={post} onClick={() => window.location.href = `/post/${post.slug || post.id}`} />
              ))}
            </div>
          </div>

          {/* Right Top: Stats Card */}
          <div className="md:col-span-2 md:row-span-1 bg-[#c3d8ea] rounded-[2.5rem] p-6 flex flex-col justify-between overflow-hidden">
            <div className="text-xs font-bold text-gray-800 mb-1">Quick Stats</div>
            <div className="flex flex-col gap-4 mt-4 flex-1 justify-center">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-white/50 rounded-full flex items-center justify-center">
                  <FileText size={18} strokeWidth={2.5} />
                </div>
                <div>
                  <div className="text-2xl font-black">{displayPosts.length}</div>
                  <div className="text-[10px] font-bold text-gray-600 uppercase">Posts</div>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-white/50 rounded-full flex items-center justify-center">
                  <TrendingUp size={18} strokeWidth={2.5} />
                </div>
                <div>
                  <div className="text-2xl font-black">∞</div>
                  <div className="text-[10px] font-bold text-gray-600 uppercase">Views</div>
                </div>
              </div>
            </div>
          </div>

          {/* Bottom Left: CTA Card */}
          <div className="md:col-span-5 md:row-span-1 bg-[#d5c9b6] rounded-[2.5rem] relative overflow-hidden group flex flex-col justify-end p-6 lg:p-8">
            <div className="absolute inset-0 bg-gradient-to-t from-black/30 to-transparent"></div>
            <div className="relative z-10">
              {isAuthenticated ? (
                <>
                  <div className="text-xs font-bold text-white/70 mb-1">Your Dashboard</div>
                  <h3 className="text-xl font-black leading-tight uppercase text-white tracking-tight mb-3">
                    CREATE YOUR NEXT MASTERPIECE
                  </h3>
                  <Link
                    to="/editor"
                    className="inline-flex items-center gap-2 bg-white text-black px-5 py-2.5 rounded-full text-sm font-bold hover:bg-gray-100 transition-colors"
                  >
                    Start Writing <ArrowRight size={14} />
                  </Link>
                </>
              ) : (
                <>
                  <div className="text-xs font-bold text-white/70 mb-1">Join Nova</div>
                  <h3 className="text-xl font-black leading-tight uppercase text-white tracking-tight mb-3">
                    START YOUR BLOGGING JOURNEY TODAY
                  </h3>
                  <Link
                    to="/login"
                    className="inline-flex items-center gap-2 bg-white text-black px-5 py-2.5 rounded-full text-sm font-bold hover:bg-gray-100 transition-colors"
                  >
                    Get Started <ArrowRight size={14} />
                  </Link>
                </>
              )}
            </div>
          </div>

          {/* Bottom Right: Tags Card */}
          <div className="md:col-span-2 md:row-span-1 bg-[#dcbcf6] rounded-[2.5rem] p-6 flex flex-col justify-between relative overflow-hidden">
            <div className="flex flex-wrap gap-2">
              {sampleTags.map(tag => (
                <TagPill key={tag} name={tag} onClick={() => {}} />
              ))}
            </div>

            <Link
              to="/categories"
              className="flex items-end justify-between mt-4"
            >
              <span className="text-sm font-bold text-black leading-tight w-2/3">View All Categories</span>
              <div className="w-10 h-10 bg-white rounded-full flex items-center justify-center hover:bg-gray-100 transition-colors shadow-sm">
                <ArrowRight size={16} strokeWidth={3} className="text-black" />
              </div>
            </Link>
          </div>
        </div>
      )}
    </div>
  );
}
