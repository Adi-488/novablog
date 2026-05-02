/**
 * NovaBlog API Client
 * 
 * Central HTTP client that handles:
 * - JWT token management (attach, refresh, store)
 * - Tenant header injection (X-Tenant-Subdomain)
 * - Standardized error handling
 */

const API_BASE = '/api/v1';

// ----------------------------------------------------------------
// Token Management
// ----------------------------------------------------------------

export function getAccessToken() {
  return localStorage.getItem('nova_access_token');
}

export function getRefreshToken() {
  return localStorage.getItem('nova_refresh_token');
}

export function setTokens(accessToken, refreshToken) {
  localStorage.setItem('nova_access_token', accessToken);
  localStorage.setItem('nova_refresh_token', refreshToken);
}

export function clearTokens() {
  localStorage.removeItem('nova_access_token');
  localStorage.removeItem('nova_refresh_token');
  localStorage.removeItem('nova_user');
  localStorage.removeItem('nova_tenant');
}

export function getStoredUser() {
  const raw = localStorage.getItem('nova_user');
  return raw ? JSON.parse(raw) : null;
}

export function setStoredUser(user) {
  localStorage.setItem('nova_user', JSON.stringify(user));
}

export function getStoredTenant() {
  return localStorage.getItem('nova_tenant') || '';
}

export function setStoredTenant(tenant) {
  localStorage.setItem('nova_tenant', tenant);
}

// ----------------------------------------------------------------
// Core Fetch Wrapper
// ----------------------------------------------------------------

async function apiFetch(endpoint, options = {}) {
  const url = `${API_BASE}${endpoint}`;
  const token = getAccessToken();
  const tenant = getStoredTenant();

  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  if (tenant) {
    headers['X-Tenant-ID'] = tenant;
  }

  const response = await fetch(url, {
    ...options,
    headers,
  });

  // If 401 and we have a refresh token, try to refresh
  if (response.status === 401 && getRefreshToken()) {
    const refreshed = await refreshAccessToken();
    if (refreshed) {
      // Retry original request with new token
      headers['Authorization'] = `Bearer ${getAccessToken()}`;
      const retryResponse = await fetch(url, { ...options, headers });
      if (!retryResponse.ok) {
        throw await createApiError(retryResponse);
      }
      return retryResponse;
    } else {
      // Refresh failed — force logout
      clearTokens();
      window.location.href = '/login';
      throw new Error('Session expired. Please log in again.');
    }
  }

  if (!response.ok) {
    throw await createApiError(response);
  }

  return response;
}

async function createApiError(response) {
  let message = `API Error: ${response.status}`;
  try {
    const body = await response.json();
    message = body.message || body.error || message;
  } catch {
    // response body wasn't JSON
  }
  const error = new Error(message);
  error.status = response.status;
  return error;
}

async function refreshAccessToken() {
  try {
    const response = await fetch(`${API_BASE}/auth/refresh`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken: getRefreshToken() }),
    });

    if (!response.ok) return false;

    const data = await response.json();
    setTokens(data.accessToken, data.refreshToken);
    return true;
  } catch {
    return false;
  }
}

// ----------------------------------------------------------------
// Auth API
// ----------------------------------------------------------------

export const authApi = {
  /**
   * Get the OAuth2 login URL for a provider.
   */
  getLoginUrl(provider, tenant) {
    const redirectUri = window.location.origin + `/auth/callback/${provider}`;
    return `${API_BASE}/auth/login/${provider}?tenant=${encodeURIComponent(tenant)}&redirectUri=${encodeURIComponent(redirectUri)}`;
  },

  /**
   * Exchange an OAuth2 authorization code for JWT tokens.
   */
  async exchangeCode(provider, code, tenant, redirectUri) {
    const response = await fetch(`${API_BASE}/auth/callback/${provider}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ code, tenant, redirectUri }),
    });

    if (!response.ok) throw await createApiError(response);

    const data = await response.json();
    setTokens(data.accessToken, data.refreshToken);
    
    // AuthResponse is flat, so we map it into a user object
    const user = {
      id: data.userId,
      email: data.email,
      role: data.role
    };
    
    setStoredUser(user);
    setStoredTenant(tenant);
    return { ...data, user };
  },

  logout() {
    clearTokens();
  },
};

// ----------------------------------------------------------------
// Tenant API
// ----------------------------------------------------------------

export const tenantApi = {
  async register(name, subdomain, ownerEmail) {
    const res = await fetch(`${API_BASE}/tenants/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ orgName: name, subdomain, adminEmail: ownerEmail }),
    });
    if (!res.ok) throw await createApiError(res);
    return res.json();
  },

  async checkAvailability(subdomain) {
    const res = await fetch(`${API_BASE}/tenants/check?subdomain=${encodeURIComponent(subdomain)}`);
    if (!res.ok) throw await createApiError(res);
    return res.json();
  },
};

// ----------------------------------------------------------------
// Posts API
// ----------------------------------------------------------------

export const postsApi = {
  async list(status, page = 0, size = 20) {
    let endpoint = `/posts?page=${page}&size=${size}&sort=createdAt,desc`;
    if (status) endpoint += `&status=${status}`;
    const res = await apiFetch(endpoint);
    return res.json();
  },

  async getById(postId) {
    const res = await apiFetch(`/posts/${postId}`);
    return res.json();
  },

  async getBySlug(slug) {
    const res = await apiFetch(`/posts/slug/${slug}`);
    return res.json();
  },

  async create(data) {
    const res = await apiFetch('/posts', {
      method: 'POST',
      body: JSON.stringify(data),
    });
    return res.json();
  },

  async update(postId, data) {
    const res = await apiFetch(`/posts/${postId}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    });
    return res.json();
  },

  async changeStatus(postId, status, scheduledAt) {
    const body = { status };
    if (scheduledAt) body.scheduledAt = scheduledAt;
    const res = await apiFetch(`/posts/${postId}/status`, {
      method: 'PATCH',
      body: JSON.stringify(body),
    });
    return res.json();
  },

  async delete(postId) {
    await apiFetch(`/posts/${postId}`, { method: 'DELETE' });
  },

  async search(query, page = 0) {
    const res = await apiFetch(`/posts/search?q=${encodeURIComponent(query)}&page=${page}`);
    return res.json();
  },

  async getByTag(tagName, page = 0) {
    const res = await apiFetch(`/posts/tag/${tagName}?page=${page}`);
    return res.json();
  },

  async getVersions(postId) {
    const res = await apiFetch(`/posts/${postId}/versions`);
    return res.json();
  },

  async listMine(status, page = 0) {
    let endpoint = `/posts/mine?page=${page}&sort=createdAt,desc`;
    if (status) endpoint += `&status=${status}`;
    const res = await apiFetch(endpoint);
    return res.json();
  },
};

// ----------------------------------------------------------------
// Users API
// ----------------------------------------------------------------

export const usersApi = {
  async getMe() {
    const res = await apiFetch('/users/me');
    return res.json();
  },

  async listAll() {
    const res = await apiFetch('/users');
    return res.json();
  },
};

// ----------------------------------------------------------------
// Notifications API (Sprint 5)
// ----------------------------------------------------------------

export const notificationsApi = {
  async list(page = 0, size = 20, unreadOnly = false) {
    const res = await apiFetch(`/notifications?page=${page}&size=${size}&unreadOnly=${unreadOnly}`);
    return res.json();
  },

  async getUnreadCount() {
    const res = await apiFetch('/notifications/count');
    return res.json();
  },

  async markAsRead(notificationId) {
    await apiFetch(`/notifications/${notificationId}/read`, { method: 'PATCH' });
  },

  async markAllAsRead() {
    const res = await apiFetch('/notifications/read-all', { method: 'PATCH' });
    return res.json();
  },
};

// ----------------------------------------------------------------
// Media API (Sprint 6)
// ----------------------------------------------------------------

export const mediaApi = {
  async upload(file, postId, altText) {
    const token = getAccessToken();
    const tenant = getStoredTenant();
    const formData = new FormData();
    formData.append('file', file);
    if (postId) formData.append('postId', postId);
    if (altText) formData.append('altText', altText);

    const headers = {};
    if (token) headers['Authorization'] = `Bearer ${token}`;
    if (tenant) headers['X-Tenant-ID'] = tenant;
    // Note: Do NOT set Content-Type — browser sets multipart boundary automatically

    const res = await fetch(`${API_BASE}/media`, {
      method: 'POST',
      headers,
      body: formData,
    });

    if (!res.ok) throw await createApiError(res);
    return res.json();
  },

  async list(page = 0, size = 20) {
    const res = await apiFetch(`/media?page=${page}&size=${size}`);
    return res.json();
  },

  async delete(fileId) {
    await apiFetch(`/media/${fileId}`, { method: 'DELETE' });
  },
};
