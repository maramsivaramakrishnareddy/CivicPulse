import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import '../../styles/notifications.css';

const STORAGE_KEY = 'civicpulse_notification_preferences';

const DEFAULT_PREFERENCES = {
  statusUpdates: true,
  escalationAlerts: true,
  feedbackReminders: true,
  duplicateAlerts: true,
  emailNotifications: true,
  browserAlerts: false,
};

export default function NotificationPreferences() {
  const [preferences, setPreferences] = useState(DEFAULT_PREFERENCES);
  const [savedSuccess, setSavedSuccess] = useState(false);

  useEffect(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEY);
      if (saved) {
        setPreferences(JSON.parse(saved));
      }
    } catch (e) {
      console.error('Failed to load notification preferences from localStorage:', e);
    }
  }, []);

  const handleToggle = (key) => {
    setPreferences(prev => ({
      ...prev,
      [key]: !prev[key]
    }));
  };

  const handleSave = (e) => {
    e.preventDefault();
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(preferences));
      setSavedSuccess(true);
      setTimeout(() => setSavedSuccess(false), 3000);
    } catch (err) {
      console.error('Failed to save preferences:', err);
    }
  };

  return (
    <div className="dashboard-page">
      <div className="notifications-container">
        {/* Header */}
        <div className="notifications-header">
          <div className="notifications-title-area">
            <h1>Notification Preferences</h1>
            <p className="notifications-subtitle">
              Manage how and when you receive automated service notices from CivicPulse.
            </p>
          </div>

          <div className="notifications-header-actions">
            <Link to="/citizen/notifications" className="btn btn-outline btn-sm">
              ← Back to Notifications
            </Link>
          </div>
        </div>

        {savedSuccess && (
          <div style={{
            background: '#DCFCE7',
            color: '#15803D',
            padding: '0.75rem 1rem',
            borderRadius: '8px',
            marginBottom: '1.5rem',
            fontSize: '0.92rem',
            border: '1px solid #86EFAC',
            fontWeight: '600'
          }}>
            ✓ Notification preferences saved successfully.
          </div>
        )}

        <form onSubmit={handleSave} className="preferences-card">
          {/* Complaint Events Group */}
          <div className="preference-group">
            <h2 className="preference-group-title">Complaint Lifecycle Events</h2>

            <div className="preference-item">
              <div className="preference-label">
                <span className="preference-name">Status Updates</span>
                <span className="preference-desc">Receive notifications when your complaint is viewed, under action, or resolved.</span>
              </div>
              <label className="toggle-switch">
                <input
                  type="checkbox"
                  checked={preferences.statusUpdates}
                  onChange={() => handleToggle('statusUpdates')}
                />
                <span className="toggle-slider"></span>
              </label>
            </div>

            <div className="preference-item">
              <div className="preference-label">
                <span className="preference-name">Escalation Notices</span>
                <span className="preference-desc">Alerts when your complaint is escalated to higher supervisory officers or authorities.</span>
              </div>
              <label className="toggle-switch">
                <input
                  type="checkbox"
                  checked={preferences.escalationAlerts}
                  onChange={() => handleToggle('escalationAlerts')}
                />
                <span className="toggle-slider"></span>
              </label>
            </div>

            <div className="preference-item">
              <div className="preference-label">
                <span className="preference-name">Feedback & Ratings</span>
                <span className="preference-desc">Confirmation when your citizen satisfaction feedback is submitted and recorded.</span>
              </div>
              <label className="toggle-switch">
                <input
                  type="checkbox"
                  checked={preferences.feedbackReminders}
                  onChange={() => handleToggle('feedbackReminders')}
                />
                <span className="toggle-slider"></span>
              </label>
            </div>

            <div className="preference-item">
              <div className="preference-label">
                <span className="preference-name">Duplicate Verification Alerts</span>
                <span className="preference-desc">Notices if an officer verifies your submission as a duplicate of an existing ticket.</span>
              </div>
              <label className="toggle-switch">
                <input
                  type="checkbox"
                  checked={preferences.duplicateAlerts}
                  onChange={() => handleToggle('duplicateAlerts')}
                />
                <span className="toggle-slider"></span>
              </label>
            </div>
          </div>

          {/* Delivery Channels */}
          <div className="preference-group">
            <h2 className="preference-group-title">Delivery Channels</h2>

            <div className="preference-item">
              <div className="preference-label">
                <span className="preference-name">Email Copies</span>
                <span className="preference-desc">Send automated email notifications to your verified account email address.</span>
              </div>
              <label className="toggle-switch">
                <input
                  type="checkbox"
                  checked={preferences.emailNotifications}
                  onChange={() => handleToggle('emailNotifications')}
                />
                <span className="toggle-slider"></span>
              </label>
            </div>

            <div className="preference-item">
              <div className="preference-label">
                <span className="preference-name">Browser Desktop Alerts</span>
                <span className="preference-desc">Show immediate pop-up alerts while the CivicPulse web application is open.</span>
              </div>
              <label className="toggle-switch">
                <input
                  type="checkbox"
                  checked={preferences.browserAlerts}
                  onChange={() => handleToggle('browserAlerts')}
                />
                <span className="toggle-slider"></span>
              </label>
            </div>
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1.5rem' }}>
            <Link to="/citizen/notifications" className="btn btn-outline">
              Cancel
            </Link>
            <button type="submit" className="btn btn-primary">
              Save Preferences
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
