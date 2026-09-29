import React, { useState, useEffect } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import api from '../services/api';

export default function Navbar({ session, setSession }) {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [unreadCount, setUnreadCount] = useState(0);
  const navigate = useNavigate();

  useEffect(() => {
    if (session?.authenticated && session.userType === 'CITIZEN') {
      const fetchUnread = async () => {
        try {
          const res = await api.getUnreadNotificationCount();
          if (res?.success && typeof res.data === 'number') {
            setUnreadCount(res.data);
          }
        } catch (err) {
          // ignore background polling error
        }
      };
      fetchUnread();
      const interval = setInterval(fetchUnread, 15000);
      return () => clearInterval(interval);
    } else {
      setUnreadCount(0);
    }
  }, [session]);

  const handleLogout = async () => {
    try {
      await api.logout();
    } catch (err) {
      console.error('Logout error:', err);
    } finally {
      setSession({ authenticated: false, userType: null, userId: null, userName: null, details: null });
      navigate('/');
    }
  };

  const closeMenu = () => setMobileMenuOpen(false);

  return (
    <header className="site-header">
      {/* Top Gov Banner */}
      <div className="top-bar">
        <div className="container top-bar-content">
          <div className="top-bar-tagline">
            <span className="top-bar-badge">Official Portal</span>
            <span>Government Public Service & Administrative Portal</span>
          </div>
          <div className="top-bar-tagline">
            <span>Secure Digital Administration</span>
          </div>
        </div>
      </div>

      {/* Main Navbar */}
      <div className="nav-container">
        <Link to="/" className="brand-link" onClick={closeMenu}>
          <div className="brand-logo-icon">
            <svg viewBox="0 0 24 24">
              <path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm0 10.99h7c-.53 4.12-3.28 7.79-7 8.94V12H5V6.3l7-3.11v8.8z"/>
            </svg>
          </div>
          <div className="brand-text">
            <span className="brand-title">Smart Governance Platform</span>
            <span className="brand-subtitle">Administrative Operations & Citizen Assistance</span>
          </div>
        </Link>

        <button 
          className="mobile-toggle"
          onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
          aria-label="Toggle navigation"
        >
          ☰
        </button>

        <nav className={`nav-menu ${mobileMenuOpen ? 'open' : ''}`}>
          <NavLink to="/" className={({ isActive }) => `nav-item-link ${isActive ? 'active' : ''}`} onClick={closeMenu}>
            Home
          </NavLink>

          {session?.authenticated ? (
            <>
              {session.userType === 'CITIZEN' && (
                <>
                  <NavLink to="/citizen/dashboard" className={({ isActive }) => `nav-item-link ${isActive ? 'active' : ''}`} onClick={closeMenu}>
                    Citizen Dashboard
                  </NavLink>
                  <NavLink to="/citizen/notifications" className={({ isActive }) => `nav-item-link notification-nav-link ${isActive ? 'active' : ''}`} onClick={closeMenu}>
                    <span className="nav-bell-icon">🔔</span>
                    <span>Notifications</span>
                    {unreadCount > 0 && <span className="nav-unread-badge">{unreadCount}</span>}
                  </NavLink>
                </>
              )}

              {session.userType === 'OFFICER' && (
                <NavLink to="/officer/dashboard" className={({ isActive }) => `nav-item-link ${isActive ? 'active' : ''}`} onClick={closeMenu}>
                  Officer Dashboard
                </NavLink>
              )}

              <div className="nav-actions">
                <div className={`user-badge ${session.userType === 'OFFICER' ? 'user-badge-officer' : ''}`}>
                  <span>●</span>
                  <span>{session.userName || (session.userType === 'CITIZEN' ? 'Citizen' : 'Officer')}</span>
                  <span>({session.userType})</span>
                </div>
                <button className="btn btn-outline btn-sm" onClick={() => { closeMenu(); handleLogout(); }}>
                  Logout
                </button>
              </div>
            </>
          ) : (
            <div className="nav-actions">
              <Link to="/auth/citizen/register" className="btn btn-outline btn-sm" onClick={closeMenu}>
                Citizen Register
              </Link>
              <Link to="/auth/citizen/login" className="btn btn-primary btn-sm" onClick={closeMenu}>
                Citizen Login
              </Link>
              <Link to="/auth/officer/login" className="btn btn-secondary btn-sm" onClick={closeMenu}>
                Officer Login
              </Link>
            </div>
          )}
        </nav>
      </div>
    </header>
  );
}
