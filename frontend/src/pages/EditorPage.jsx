import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Save, ArrowLeft, Send, Eye, Clock, Tag, X, Loader2, ChevronDown, History, Upload, Image } from 'lucide-react';
import { postsApi, mediaApi } from '../api/client';
import { StatusBadge } from '../components/PostCards';
import PresencePanel from '../components/PresencePanel';
import useCollaboration from '../hooks/useCollaboration';

export default function EditorPage() {
  const { postId } = useParams();
  const navigate = useNavigate();
  const isEditing = !!postId;
  const bodyRef = useRef(null);
  const fileInputRef = useRef(null);

  const [title, setTitle] = useState('');
  const [body, setBody] = useState('');
  const [slug, setSlug] = useState('');
  const [seoTitle, setSeoTitle] = useState('');
  const [seoDescription, setSeoDescription] = useState('');
  const [tags, setTags] = useState([]);
  const [tagInput, setTagInput] = useState('');
  const [status, setStatus] = useState('DRAFT');

  const [saving, setSaving] = useState(false);
  const [loading, setLoading] = useState(isEditing);
  const [error, setError] = useState('');
  const [saved, setSaved] = useState(false);
  const [showSeo, setShowSeo] = useState(false);
  const [showActions, setShowActions] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState('');
  const [remoteEditToast, setRemoteEditToast] = useState('');
  const [scheduleDate, setScheduleDate] = useState('');
  const [showSchedule, setShowSchedule] = useState(false);

  // Sprint 4: Real-time collaboration
  const handleRemoteEdit = useCallback((content, userId, userName) => {
    setBody(content);
    setRemoteEditToast(`${userName} made an edit`);
    setTimeout(() => setRemoteEditToast(''), 3000);
  }, []);

  const handleRemoteTitleEdit = useCallback((newTitle, userId, userName) => {
    setTitle(newTitle);
    setRemoteEditToast(`${userName} edited the title`);
    setTimeout(() => setRemoteEditToast(''), 3000);
  }, []);

  const { presence, sendEdit, sendTitleEdit, sendCursor, connected } = useCollaboration(
    postId,
    handleRemoteEdit,
    handleRemoteTitleEdit
  );

  // Load existing post for editing
  useEffect(() => {
    if (!postId) return;
    async function load() {
      try {
        const post = await postsApi.getById(postId);
        setTitle(post.title || '');
        setBody(post.body || '');
        setSlug(post.slug || '');
        setSeoTitle(post.seoTitle || '');
        setSeoDescription(post.seoDescription || '');
        setTags(post.tags ? [...post.tags] : []);
        setStatus(post.status || 'DRAFT');
      } catch (err) {
        setError('Could not load post: ' + err.message);
      }
      setLoading(false);
    }
    load();
  }, [postId]);

  // Auto-resize body textarea
  useEffect(() => {
    if (bodyRef.current) {
      bodyRef.current.style.height = 'auto';
      bodyRef.current.style.height = bodyRef.current.scrollHeight + 'px';
    }
  }, [body]);

  // Auto-save every 30 seconds (Sprint 4 requirement)
  useEffect(() => {
    if (!postId || status !== 'DRAFT') return;
    const interval = setInterval(() => {
      if (title.trim()) {
        handleSave(true); // silent save
      }
    }, 30000);
    return () => clearInterval(interval);
  }, [postId, title, body, status]);

  function handleTitleChange(e) {
    const newTitle = e.target.value;
    setTitle(newTitle);
    if (isEditing) sendTitleEdit(newTitle);
  }

  function handleBodyChange(e) {
    const newBody = e.target.value;
    setBody(newBody);
    if (isEditing) sendEdit(newBody);
  }

  function addTag(e) {
    if (e.key === 'Enter' || e.key === ',') {
      e.preventDefault();
      const t = tagInput.trim().toLowerCase().replace(',', '');
      if (t && !tags.includes(t)) {
        setTags([...tags, t]);
      }
      setTagInput('');
    }
  }

  function removeTag(tag) {
    setTags(tags.filter(t => t !== tag));
  }

  async function handleSave(silent = false) {
    if (!title.trim()) {
      if (!silent) setError('Title is required');
      return;
    }

    setSaving(true);
    if (!silent) {
      setError('');
      setSaved(false);
    }

    try {
      const payload = {
        title: title.trim(),
        body,
        seoTitle: seoTitle || undefined,
        seoDescription: seoDescription || undefined,
        tags: tags.length > 0 ? tags : undefined,
      };

      if (isEditing) {
        await postsApi.update(postId, payload);
      } else {
        payload.slug = slug || undefined;
        const created = await postsApi.create(payload);
        navigate(`/editor/${created.id}`, { replace: true });
      }
      setSaved(true);
      setTimeout(() => setSaved(false), 3000);
    } catch (err) {
      if (!silent) setError(err.message);
    }
    setSaving(false);
  }

  async function handleStatusChange(newStatus) {
    if (!postId) {
      setError('Save the post first before changing status.');
      return;
    }
    try {
      const scheduledAt = newStatus === 'SCHEDULED' && scheduleDate ? scheduleDate : undefined;
      const result = await postsApi.changeStatus(postId, newStatus, scheduledAt);
      setStatus(result.status);
      setShowActions(false);
      setShowSchedule(false);
    } catch (err) {
      setError(err.message);
    }
  }

  // Sprint 6: Media upload
  async function handleFileUpload(e) {
    const file = e.target.files?.[0];
    if (!file) return;

    setUploading(true);
    setUploadProgress(`Uploading ${file.name}...`);
    setError('');

    try {
      const result = await mediaApi.upload(file, postId, '');
      // Insert the media URL into the body at cursor position
      const url = result.url;
      const insertText = file.type.startsWith('image/')
        ? `\n![${file.name}](${url})\n`
        : `\n[${file.name}](${url})\n`;

      if (bodyRef.current) {
        const pos = bodyRef.current.selectionStart || body.length;
        const newBody = body.slice(0, pos) + insertText + body.slice(pos);
        setBody(newBody);
        if (isEditing) sendEdit(newBody);
      } else {
        setBody(body + insertText);
      }

      setUploadProgress(`✓ ${file.name} uploaded`);
      setTimeout(() => setUploadProgress(''), 3000);
    } catch (err) {
      setError(`Upload failed: ${err.message}`);
      setUploadProgress('');
    }
    setUploading(false);
    // Reset file input
    if (fileInputRef.current) fileInputRef.current.value = '';
  }

  // Handle drag-and-drop
  function handleDrop(e) {
    e.preventDefault();
    const file = e.dataTransfer.files?.[0];
    if (file) {
      // Simulate file input change
      const dt = new DataTransfer();
      dt.items.add(file);
      if (fileInputRef.current) {
        fileInputRef.current.files = dt.files;
        handleFileUpload({ target: fileInputRef.current });
      }
    }
  }

  function handleDragOver(e) {
    e.preventDefault();
  }

  // Keyboard shortcut: Ctrl+S to save
  useEffect(() => {
    function onKeyDown(e) {
      if ((e.ctrlKey || e.metaKey) && e.key === 's') {
        e.preventDefault();
        handleSave();
      }
    }
    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, [title, body, seoTitle, seoDescription, tags]);

  if (loading) {
    return (
      <div className="max-w-3xl mx-auto">
        <div className="bg-white rounded-[2.5rem] p-10 animate-pulse">
          <div className="h-12 bg-gray-100 rounded-2xl w-2/3 mb-6"></div>
          <div className="space-y-3">
            {[...Array(6)].map((_, i) => (
              <div key={i} className="h-4 bg-gray-100 rounded-full" style={{ width: `${50 + Math.random() * 50}%` }}></div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto">
      {/* Top Bar */}
      <div className="flex items-center justify-between mb-6">
        <button
          onClick={() => navigate('/dashboard')}
          className="flex items-center gap-2 text-gray-500 font-bold text-sm hover:text-black transition-colors"
        >
          <ArrowLeft size={16} /> Dashboard
        </button>

        <div className="flex items-center gap-3">
          {/* Sprint 4: Presence Panel */}
          {isEditing && <PresencePanel presence={presence} connected={connected} />}

          {isEditing && <StatusBadge status={status} />}

          {saved && (
            <span className="text-xs font-bold text-green-600 bg-green-100 px-3 py-1.5 rounded-full">
              ✓ Saved
            </span>
          )}

          {/* Save Button */}
          <button
            onClick={() => handleSave()}
            disabled={saving}
            className="flex items-center gap-2 bg-black text-white px-5 py-2.5 rounded-full text-sm font-bold hover:bg-gray-800 transition-colors disabled:opacity-50"
          >
            {saving ? <Loader2 size={14} className="animate-spin" /> : <Save size={14} />}
            {saving ? 'Saving...' : 'Save'}
          </button>

          {/* Actions Dropdown */}
          {isEditing && (
            <div className="relative">
              <button
                onClick={() => setShowActions(!showActions)}
                className="flex items-center gap-1 bg-gray-100 px-4 py-2.5 rounded-full text-sm font-bold hover:bg-gray-200 transition-colors"
              >
                Actions <ChevronDown size={14} />
              </button>

              {showActions && (
                <div className="absolute right-0 top-12 bg-white rounded-2xl shadow-xl border border-gray-100 py-2 z-50 w-56">
                  <button
                    onClick={() => navigate(`/post/${postId}`)}
                    className="flex items-center gap-3 w-full px-4 py-2.5 text-sm font-semibold hover:bg-gray-50"
                  >
                    <Eye size={14} /> Preview
                  </button>
                  <button
                    onClick={() => navigate(`/editor/${postId}/versions`)}
                    className="flex items-center gap-3 w-full px-4 py-2.5 text-sm font-semibold hover:bg-gray-50"
                  >
                    <History size={14} /> Version History
                  </button>

                  <div className="border-t border-gray-100 my-1"></div>

                  {status === 'DRAFT' && (
                    <button
                      onClick={() => handleStatusChange('UNDER_REVIEW')}
                      className="flex items-center gap-3 w-full px-4 py-2.5 text-sm font-semibold hover:bg-gray-50 text-blue-600"
                    >
                      <Send size={14} /> Submit for Review
                    </button>
                  )}
                  {status === 'UNDER_REVIEW' && (
                    <>
                      <button
                        onClick={() => handleStatusChange('PUBLISHED')}
                        className="flex items-center gap-3 w-full px-4 py-2.5 text-sm font-semibold hover:bg-gray-50 text-green-600"
                      >
                        <Eye size={14} /> Publish Now
                      </button>
                      <button
                        onClick={() => setShowSchedule(!showSchedule)}
                        className="flex items-center gap-3 w-full px-4 py-2.5 text-sm font-semibold hover:bg-gray-50 text-purple-600"
                      >
                        <Clock size={14} /> Schedule Publish
                      </button>
                      {showSchedule && (
                        <div className="px-4 py-2">
                          <input
                            type="datetime-local"
                            value={scheduleDate}
                            onChange={e => setScheduleDate(e.target.value)}
                            className="w-full text-xs font-semibold bg-gray-50 rounded-xl px-3 py-2 outline-none mb-2"
                            min={new Date().toISOString().slice(0, 16)}
                          />
                          <button
                            onClick={() => handleStatusChange('SCHEDULED')}
                            disabled={!scheduleDate}
                            className="w-full bg-purple-600 text-white text-xs font-bold py-2 rounded-xl disabled:opacity-50"
                          >
                            Confirm Schedule
                          </button>
                        </div>
                      )}
                      <button
                        onClick={() => handleStatusChange('DRAFT')}
                        className="flex items-center gap-3 w-full px-4 py-2.5 text-sm font-semibold hover:bg-gray-50 text-orange-600"
                      >
                        <ArrowLeft size={14} /> Back to Draft
                      </button>
                    </>
                  )}
                  {status === 'PUBLISHED' && (
                    <button
                      onClick={() => handleStatusChange('ARCHIVED')}
                      className="flex items-center gap-3 w-full px-4 py-2.5 text-sm font-semibold hover:bg-gray-50 text-red-500"
                    >
                      <Clock size={14} /> Archive
                    </button>
                  )}
                </div>
              )}
            </div>
          )}
        </div>
      </div>

      {/* Remote Edit Toast */}
      {remoteEditToast && (
        <div className="bg-blue-50 text-blue-700 text-sm font-bold px-5 py-3 rounded-2xl mb-4 animate-pulse">
          ✨ {remoteEditToast}
        </div>
      )}

      {error && (
        <div className="bg-red-50 text-red-600 text-sm font-bold px-5 py-3 rounded-2xl mb-6">
          {error}
        </div>
      )}

      {/* Upload Progress */}
      {uploadProgress && (
        <div className="bg-green-50 text-green-700 text-sm font-bold px-5 py-3 rounded-2xl mb-4 flex items-center gap-2">
          {uploading && <Loader2 size={14} className="animate-spin" />}
          {uploadProgress}
        </div>
      )}

      {/* Editor Card */}
      <div
        className="bg-white rounded-[2.5rem] p-8 lg:p-12 shadow-sm mb-6"
        onDrop={handleDrop}
        onDragOver={handleDragOver}
      >
        {/* Title */}
        <input
          type="text"
          placeholder="Post title..."
          value={title}
          onChange={handleTitleChange}
          className="w-full text-4xl lg:text-5xl font-black tracking-tight outline-none placeholder:text-gray-300 mb-6"
        />

        {/* Tags */}
        <div className="flex items-center gap-2 flex-wrap mb-6">
          <Tag size={14} className="text-gray-400" />
          {tags.map(tag => (
            <span
              key={tag}
              className="flex items-center gap-1 bg-gray-100 px-3 py-1 rounded-full text-xs font-bold"
            >
              {tag}
              <button onClick={() => removeTag(tag)} className="hover:text-red-500">
                <X size={12} />
              </button>
            </span>
          ))}
          <input
            type="text"
            placeholder="Add tag..."
            value={tagInput}
            onChange={e => setTagInput(e.target.value)}
            onKeyDown={addTag}
            className="text-sm font-semibold outline-none placeholder:text-gray-300 w-28"
          />
        </div>

        {/* Media Upload Button (Sprint 6) */}
        <div className="flex items-center gap-2 mb-4">
          <input
            ref={fileInputRef}
            type="file"
            accept="image/jpeg,image/png,image/gif,image/webp,application/pdf,video/mp4"
            onChange={handleFileUpload}
            className="hidden"
            id="media-upload"
          />
          <button
            onClick={() => fileInputRef.current?.click()}
            disabled={uploading}
            className="flex items-center gap-2 text-sm font-bold text-gray-400 hover:text-black transition-colors disabled:opacity-50"
          >
            <Image size={16} />
            {uploading ? 'Uploading...' : 'Insert media'}
          </button>
          <span className="text-[10px] text-gray-300 font-semibold">
            or drag & drop images onto the editor
          </span>
        </div>

        {/* Body */}
        <textarea
          ref={bodyRef}
          placeholder="Start writing your story..."
          value={body}
          onChange={handleBodyChange}
          onSelect={() => {
            if (bodyRef.current && isEditing) {
              sendCursor(bodyRef.current.selectionStart);
            }
          }}
          className="w-full min-h-[400px] text-lg font-medium leading-relaxed outline-none placeholder:text-gray-300 resize-none"
        />
      </div>

      {/* SEO Panel */}
      <div className="bg-white rounded-[2.5rem] shadow-sm overflow-hidden mb-6">
        <button
          onClick={() => setShowSeo(!showSeo)}
          className="flex items-center justify-between w-full px-8 py-5 text-sm font-bold text-gray-500 hover:bg-gray-50 transition-colors"
        >
          <span>SEO Settings</span>
          <ChevronDown size={16} className={`transition-transform ${showSeo ? 'rotate-180' : ''}`} />
        </button>

        {showSeo && (
          <div className="px-8 pb-8 space-y-4">
            {!isEditing && (
              <div>
                <label className="block text-xs font-bold uppercase text-gray-400 mb-1.5 tracking-wider">
                  Custom Slug
                </label>
                <input
                  type="text"
                  placeholder="auto-generated-from-title"
                  value={slug}
                  onChange={e => setSlug(e.target.value)}
                  className="w-full px-5 py-3 bg-[#f4f5f1] rounded-2xl text-sm font-semibold outline-none"
                />
              </div>
            )}
            <div>
              <label className="block text-xs font-bold uppercase text-gray-400 mb-1.5 tracking-wider">
                SEO Title
              </label>
              <input
                type="text"
                placeholder="Custom title for search engines"
                value={seoTitle}
                onChange={e => setSeoTitle(e.target.value)}
                className="w-full px-5 py-3 bg-[#f4f5f1] rounded-2xl text-sm font-semibold outline-none"
                maxLength={200}
              />
              <div className="text-right text-[10px] font-bold text-gray-300 mt-1">{seoTitle.length}/200</div>
            </div>
            <div>
              <label className="block text-xs font-bold uppercase text-gray-400 mb-1.5 tracking-wider">
                SEO Description
              </label>
              <textarea
                placeholder="Short description for search results"
                value={seoDescription}
                onChange={e => setSeoDescription(e.target.value)}
                rows={3}
                className="w-full px-5 py-3 bg-[#f4f5f1] rounded-2xl text-sm font-semibold outline-none resize-none"
                maxLength={500}
              />
              <div className="text-right text-[10px] font-bold text-gray-300 mt-1">{seoDescription.length}/500</div>
            </div>
          </div>
        )}
      </div>

      {/* Keyboard shortcut hint */}
      <p className="text-center text-xs text-gray-300 font-semibold">
        Press <kbd className="bg-gray-100 px-1.5 py-0.5 rounded text-[10px] font-bold">Ctrl+S</kbd> to save
        {isEditing && connected && (
          <span className="ml-2">· <span className="text-green-500">●</span> Live collaboration active</span>
        )}
      </p>
    </div>
  );
}
