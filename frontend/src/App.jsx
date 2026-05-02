import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import Navbar from './components/Navbar';
import ProtectedRoute from './components/ProtectedRoute';
import HomePage from './pages/HomePage';
import BlogPage from './pages/BlogPage';
import PostPage from './pages/PostPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import AuthCallbackPage from './pages/AuthCallbackPage';
import DashboardPage from './pages/DashboardPage';
import EditorPage from './pages/EditorPage';
import VersionHistoryPage from './pages/VersionHistoryPage';
import ProfilePage from './pages/ProfilePage';

function AppLayout({ children }) {
  return (
    <div className="min-h-screen bg-[#f4f5f1] p-4 md:p-8 font-sans text-[#1a1a1a]">
      <div className="max-w-[1400px] mx-auto">
        <Navbar />
        {children}
      </div>
    </div>
  );
}

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppLayout>
          <Routes>
            {/* Public Routes */}
            <Route path="/" element={<HomePage />} />
            <Route path="/blog" element={<BlogPage />} />
            <Route path="/post/:slugOrId" element={<PostPage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/auth/callback/:provider" element={<AuthCallbackPage />} />
            <Route path="/categories" element={<BlogPage />} />
            <Route path="/about" element={
              <div className="text-center py-20">
                <h1 className="text-5xl font-black tracking-tighter mb-4">About Nova</h1>
                <p className="text-gray-500 font-semibold max-w-lg mx-auto leading-relaxed">
                  Nova is a modern multi-tenant SaaS blogging platform.
                  Each organization gets its own isolated workspace with full
                  content management, role-based access, and scheduled publishing.
                </p>
              </div>
            } />

            {/* Protected Routes — require authentication */}
            <Route path="/dashboard" element={
              <ProtectedRoute><DashboardPage /></ProtectedRoute>
            } />
            <Route path="/editor" element={
              <ProtectedRoute><EditorPage /></ProtectedRoute>
            } />
            <Route path="/editor/:postId" element={
              <ProtectedRoute><EditorPage /></ProtectedRoute>
            } />
            <Route path="/editor/:postId/versions" element={
              <ProtectedRoute><VersionHistoryPage /></ProtectedRoute>
            } />
            <Route path="/profile" element={
              <ProtectedRoute><ProfilePage /></ProtectedRoute>
            } />

            {/* 404 */}
            <Route path="*" element={
              <div className="text-center py-20">
                <div className="text-7xl mb-4">🔭</div>
                <h1 className="text-4xl font-black mb-2">404</h1>
                <p className="text-gray-500 font-semibold">Page not found</p>
              </div>
            } />
          </Routes>
        </AppLayout>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
