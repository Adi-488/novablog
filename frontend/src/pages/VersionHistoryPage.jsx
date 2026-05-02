import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Clock, User, GitBranch } from 'lucide-react';
import { postsApi } from '../api/client';

export default function VersionHistoryPage() {
  const { postId } = useParams();
  const navigate = useNavigate();
  const [versions, setVersions] = useState([]);
  const [selected, setSelected] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function load() {
      try {
        const data = await postsApi.getVersions(postId);
        setVersions(data || []);
        if (data.length > 0) setSelected(data[0]);
      } catch (err) {
        console.error('Failed to load versions:', err);
      }
      setLoading(false);
    }
    load();
  }, [postId]);

  function formatDate(dateStr) {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('en-US', {
      month: 'short', day: 'numeric', year: 'numeric',
      hour: '2-digit', minute: '2-digit',
    });
  }

  if (loading) {
    return (
      <div className="max-w-4xl mx-auto">
        <div className="bg-white rounded-[2.5rem] p-10 animate-pulse">
          <div className="h-8 bg-gray-100 rounded-2xl w-1/3 mb-6"></div>
          <div className="space-y-4">
            {[...Array(4)].map((_, i) => (
              <div key={i} className="h-16 bg-gray-50 rounded-2xl"></div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto">
      <button
        onClick={() => navigate(`/editor/${postId}`)}
        className="flex items-center gap-2 text-gray-500 font-bold text-sm hover:text-black transition-colors mb-6"
      >
        <ArrowLeft size={16} /> Back to Editor
      </button>

      <h1 className="text-3xl font-black tracking-tight mb-6 flex items-center gap-3">
        <GitBranch size={28} /> Version History
      </h1>

      {versions.length === 0 ? (
        <div className="bg-white rounded-[2.5rem] p-12 text-center">
          <div className="text-5xl mb-4">📜</div>
          <h2 className="text-xl font-black mb-2">No versions yet</h2>
          <p className="text-gray-500 font-semibold text-sm">
            Versions are created automatically when you save content changes.
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Version List */}
          <div className="lg:col-span-1 bg-white rounded-[2rem] p-4 shadow-sm h-fit">
            <div className="text-xs font-bold uppercase text-gray-400 tracking-wider px-3 py-2">
              {versions.length} version{versions.length !== 1 ? 's' : ''}
            </div>
            <div className="space-y-1">
              {versions.map(v => (
                <button
                  key={v.id}
                  onClick={() => setSelected(v)}
                  className={`w-full flex items-center gap-3 px-4 py-3 rounded-xl text-left transition-colors ${
                    selected?.id === v.id
                      ? 'bg-black text-white'
                      : 'hover:bg-gray-50'
                  }`}
                >
                  <div className={`w-8 h-8 rounded-full flex items-center justify-center text-xs font-black ${
                    selected?.id === v.id ? 'bg-white text-black' : 'bg-gray-100'
                  }`}>
                    v{v.versionNumber}
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className={`text-sm font-bold truncate ${selected?.id === v.id ? 'text-white' : ''}`}>
                      {v.title || 'Untitled'}
                    </div>
                    <div className={`text-[10px] font-semibold flex items-center gap-1 ${
                      selected?.id === v.id ? 'text-white/60' : 'text-gray-400'
                    }`}>
                      <Clock size={10} /> {formatDate(v.createdAt)}
                    </div>
                  </div>
                </button>
              ))}
            </div>
          </div>

          {/* Version Preview */}
          <div className="lg:col-span-2 bg-white rounded-[2rem] p-8 shadow-sm">
            {selected ? (
              <>
                <div className="flex items-center justify-between mb-6">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 bg-black text-white rounded-full flex items-center justify-center text-sm font-black">
                      v{selected.versionNumber}
                    </div>
                    <div>
                      <div className="text-xs font-bold text-gray-400">{formatDate(selected.createdAt)}</div>
                    </div>
                  </div>
                </div>

                <h2 className="text-2xl font-black tracking-tight mb-6">
                  {selected.title || 'Untitled'}
                </h2>

                <div className="prose prose-sm max-w-none font-medium text-gray-700 leading-relaxed bg-[#f4f5f1] rounded-2xl p-6">
                  {selected.body ? (
                    <div dangerouslySetInnerHTML={{ __html: selected.body }} />
                  ) : (
                    <p className="text-gray-400 italic">No body content in this version.</p>
                  )}
                </div>
              </>
            ) : (
              <p className="text-gray-400 font-semibold text-center py-10">
                Select a version to preview
              </p>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
