import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../../services/api';
import '../../styles/notifications.css';

export default function NotificationCenter() {
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [unreadOnly, setUnreadOnly] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [actionMessage, setActionMessage] = useState('');

  const loadNotifications = async (filterUnread = unreadOnly) => {
    try {
      setLoading(true);
      setError('');
      const [listRes, countRes] = await Promise.all([
        api.getNotifications(filterUnread),
        api.getUnreadNotificationCount()
      ]);

      if (listRes?.success) {
        setNotifications(listRes.data || []);
      }
      if (countRes?.success && typeof countRes.data === 'number') {
        setUnreadCount(countRes.data);
      }
    } catch (err) {
      console.error('Failed to load notifications:', err);
      setError('Unable to fetch notifications. Please verify network or login status.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadNotifications(unreadOnly);
  }, [unreadOnly]);

  const handleMarkAsRead = async (notificationId) => {
    try {
      const res = await api.markNotificationAsRead(notificationId);
      if (res?.success) {
        setNotifications(prev =>
          prev.map(n => n.notificationId === notificationId ? { ...n, isRead: true } : n)
        );
        setUnreadCount(prev => Math.max(0, prev - 1));
      }
    } catch (err) {
      console.error('Failed to mark notification as read:', err);
    }
  };

  const handleMarkAllAsRead = async () => {
    try {
      const res = await api.markAllNotificationsAsRead();
      if (res?.success) {
        setNotifications(prev => prev.map(n => ({ ...n, isRead: true })));
        setUnreadCount(0);
        showActionFeedback('All notifications marked as read.');
      }
    } catch (err) {
      console.error('Failed to mark all as read:', err);
    }
  };

  const handleDelete = async (notificationId) => {
    try {
      const res = await api.deleteNotification(notificationId);
      if (res?.success) {
        const deleted = notifications.find(n => n.notificationId === notificationId);
        setNotifications(prev => prev.filter(n => n.notificationId !== notificationId));
        if (deleted && !deleted.isRead) {
          setUnreadCount(prev => Math.max(0, prev - 1));
        }
        showActionFeedback('Notification removed.');
      }
    } catch (err) {
      console.error('Failed to delete notification:', err);
    }
  };

  const showActionFeedback = (msg) => {
    setActionMessage(msg);
    setTimeout(() => setActionMessage(''), 3000);
  };

  const getNotificationIcon = (type) => {
    switch (type) {
      case 'COMPLAINT_SUBMITTED':
        return { icon: '📝', className: 'icon-submitted' };
      case 'STATUS_CHANGED':
        return { icon: '🔄', className: 'icon-status' };
      case 'COMPLAINT_RESOLVED':
        return { icon: '✅', className: 'icon-resolved' };
      case 'FEEDBACK_RECEIVED':
        return { icon: '⭐', className: 'icon-feedback' };
      case 'COMPLAINT_ESCALATED':
        return { icon: '⚠️', className: 'icon-escalated' };
      case 'ESCALATION_UPDATED':
        return { icon: '🚀', className: 'icon-escalated' };
      case 'COMPLAINT_REOPENED':
        return { icon: '🔁', className: 'icon-reopened' };
      case 'COMPLAINT_MARKED_DUPLICATE':
        return { icon: '📑', className: 'icon-duplicate' };
      default:
        return { icon: '🔔', className: 'icon-default' };
    }
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return '';
    try {
      const d = new Date(dateStr);
      return d.toLocaleDateString(undefined, {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch {
      return dateStr;
    }
  };

  return (
    <div className="dashboard-page">
      <div className="notifications-container">
        {/* Header */}
        <div className="notifications-header">
          <div className="notifications-title-area">
            <h1>Notification Center</h1>
            <p className="notifications-subtitle">
              Live updates on your registered complaints, department actions, and escalations.
            </p>
          </div>

          <div className="notifications-header-actions">
            {unreadCount > 0 && (
              <button className="btn btn-outline btn-sm" onClick={handleMarkAllAsRead}>
                ✓ Mark All Read
              </button>
            )}
            <Link to="/citizen/notifications/preferences" className="btn btn-outline btn-sm">
              ⚙ Preferences
            </Link>
          </div>
        </div>

        {/* Action toast */}
        {actionMessage && (
          <div style={{
            background: '#EFF6FF',
            color: '#1E40AF',
            padding: '0.65rem 1rem',
            borderRadius: '8px',
            marginBottom: '1rem',
            fontSize: '0.9rem',
            border: '1px solid #BFDBFE'
          }}>
            {actionMessage}
          </div>
        )}

        {/* Error message */}
        {error && (
          <div style={{
            background: '#FEF2F2',
            color: '#DC2626',
            padding: '0.75rem 1rem',
            borderRadius: '8px',
            marginBottom: '1.5rem',
            border: '1px solid #FCA5A5'
          }}>
            {error}
          </div>
        )}

        {/* Filter Tabs */}
        <div className="notifications-tabs">
          <button
            className={`notifications-tab ${!unreadOnly ? 'active' : ''}`}
            onClick={() => setUnreadOnly(false)}
          >
            <span>All Notifications</span>
            <span className="tab-badge">{notifications.length}</span>
          </button>
          <button
            className={`notifications-tab ${unreadOnly ? 'active' : ''}`}
            onClick={() => setUnreadOnly(true)}
          >
            <span>Unread</span>
            {unreadCount > 0 && <span className="tab-badge tab-badge-active">{unreadCount}</span>}
          </button>
        </div>

        {/* Notification Cards */}
        {loading ? (
          <div style={{ textAlign: 'center', padding: '3rem', color: '#64748B' }}>
            Loading notifications...
          </div>
        ) : notifications.length === 0 ? (
          <div className="empty-notifications">
            <div className="empty-icon">📭</div>
            <h3>No notifications yet</h3>
            <p>
              {unreadOnly
                ? "You're all caught up! No unread notifications found."
                : "Important updates regarding your filed complaints will appear here in real-time."}
            </p>
            <div style={{ marginTop: '1.25rem' }}>
              <Link to="/citizen/dashboard" className="btn btn-primary btn-sm">
                Go to Dashboard
              </Link>
            </div>
          </div>
        ) : (
          <div className="notifications-list">
            {notifications.map(n => {
              const { icon, className } = getNotificationIcon(n.type);
              return (
                <div
                  key={n.notificationId}
                  className={`notification-card ${!n.isRead ? 'unread' : ''}`}
                >
                  <div className={`notification-icon-wrap ${className}`}>
                    {icon}
                  </div>

                  <div className="notification-body">
                    <div className="notification-meta">
                      <h3 className="notification-title">{n.title}</h3>
                      <span className="notification-time">{formatDate(n.createdAt)}</span>
                    </div>

                    <p className="notification-message">{n.message}</p>

                    <div className="notification-footer">
                      {n.complaintId ? (
                        <Link
                          to={`/citizen/complaint/${n.complaintId}`}
                          className="complaint-tag-link"
                          onClick={() => !n.isRead && handleMarkAsRead(n.notificationId)}
                        >
                          <span>📁</span>
                          <span>Complaint #{n.complaintId}</span>
                        </Link>
                      ) : <span />}

                      <div className="notification-actions">
                        {!n.isRead && (
                          <button
                            className="btn-icon-subtle"
                            onClick={() => handleMarkAsRead(n.notificationId)}
                            title="Mark as read"
                          >
                            Mark Read
                          </button>
                        )}
                        <button
                          className="btn-icon-subtle btn-delete"
                          onClick={() => handleDelete(n.notificationId)}
                          title="Delete notification"
                        >
                          ✕
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
