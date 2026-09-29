import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../../services/api';

const isResolved = (status) => {
  if (!status) return false;
  const s = String(status).trim().toUpperCase();
  return s === 'COMPLAINT_RESOLVED' || s === 'RESOLVED';
};

const getStatusLabel = (status) => {
  if (!status) return 'Applied';
  const s = String(status).trim().toUpperCase();
  if (s === 'COMPLAINT_RESOLVED' || s === 'RESOLVED') return 'Resolved';
  if (s === 'ACTION_TO_BE_TAKEN' || s === 'IN_PROGRESS') return 'In Progress';
  if (s === 'COMPLAINT_VIEWED' || s === 'VIEWED') return 'Viewed';
  if (s === 'REOPENED') return 'Reopened';
  if (s === 'DUPLICATE') return 'Duplicate';
  if (s === 'REJECTED') return 'Rejected';
  return status;
};

const getStatusTone = (status) => {
  if (!status) return 'warning';
  const s = String(status).trim().toUpperCase();
  if (s === 'COMPLAINT_RESOLVED' || s === 'RESOLVED') return 'success';
  if (s === 'ACTION_TO_BE_TAKEN' || s === 'IN_PROGRESS') return 'primary';
  if (s === 'COMPLAINT_VIEWED' || s === 'VIEWED') return 'info';
  if (s === 'REOPENED') return 'danger';
  if (s === 'DUPLICATE' || s === 'REJECTED') return 'danger';
  return 'warning';
};

export default function CitizenDashboard({ session }) {
  const citizen = session?.details || {};
  const [complaints, setComplaints] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadComplaints = async () => {
      try {
        const response = await api.getCitizenComplaints();
        setComplaints(response.data || []);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    loadComplaints();
    // Keep the citizen view synchronized with officer updates without requiring
    // a manual page refresh. Both sides read the same Complaint database row.
    const intervalId = setInterval(loadComplaints, 5000);
    return () => clearInterval(intervalId);
  }, []);

  return (
    <div className="dashboard-page">
      <div className="container">
        <div className="dashboard-header">
          <div className="dashboard-welcome">
            <span className="dashboard-role-badge">
              <svg width="14" height="14" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clipRule="evenodd"/>
              </svg>
              Citizen Account
            </span>
            <h1 className="dashboard-title">Welcome, {session?.userName || 'Citizen'}</h1>
            <p className="dashboard-subtitle">
              Smart Governance Platform — Citizen Service Assistance Portal
            </p>
          </div>

          <div>
            <span className="status-badge status-active">
              ● Active Session Authenticated
            </span>
          </div>
        </div>

        <div className="dashboard-grid">
          <div className="dashboard-card">
            <h2 className="dashboard-card-title">
              <svg width="18" height="18" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clipRule="evenodd"/>
              </svg>
              Citizen Identity Profile
            </h2>

            <div className="profile-grid">
              <div className="profile-field">
                <span className="profile-label">Full Name</span>
                <span className="profile-value">{citizen.fullName || session?.userName || '—'}</span>
              </div>

              <div className="profile-field">
                <span className="profile-label">Aadhaar Number</span>
                <span className="profile-value profile-aadhaar">
                  {citizen.aadhaarNumber
                    ? `XXXX-XXXX-${citizen.aadhaarNumber.slice(-4)}`
                    : '—'}
                </span>
              </div>

              <div className="profile-field">
                <span className="profile-label">Email Address</span>
                <span className="profile-value">{citizen.email || '—'}</span>
              </div>

              <div className="profile-field">
                <span className="profile-label">Phone Number</span>
                <span className="profile-value">{citizen.phoneNumber || '—'}</span>
              </div>

              <div className="profile-field full-width">
                <span className="profile-label">Registered Address</span>
                <span className="profile-value">{citizen.address || '—'}</span>
              </div>
            </div>
          </div>

          <div className="dashboard-card">
            <h2 className="dashboard-card-title">
              <svg width="18" height="18" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd"/>
              </svg>
              Account Status
            </h2>

            <div className="profile-grid">
              <div className="profile-field">
                <span className="profile-label">Citizen ID</span>
                <span className="profile-value">{citizen.citizenId || session?.userId || '—'}</span>
              </div>

              <div className="profile-field">
                <span className="profile-label">Registration Date</span>
                <span className="profile-value">
                  {citizen.createdAt
                    ? new Date(citizen.createdAt).toLocaleDateString('en-IN', {
                        year: 'numeric',
                        month: 'long',
                        day: 'numeric',
                      })
                    : '—'}
                </span>
              </div>

              <div className="profile-field full-width">
                <span className="profile-label">Quick Actions</span>
                <div style={{ marginTop: '0.5rem', display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
                  <Link to="/citizen/complaints/guidelines" className="primary-button" style={{ textDecoration: 'none' }}>
                    Register Complaint
                  </Link>
                  <Link to="/citizen/notifications" className="secondary-button" style={{ textDecoration: 'none' }}>
                    Notifications
                  </Link>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="dashboard-card" style={{ marginTop: '1.5rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem', flexWrap: 'wrap', gap: '0.75rem' }}>
            <h2 className="dashboard-card-title" style={{ margin: 0 }}>Your Complaints</h2>
            <Link to="/citizen/complaints/guidelines" className="primary-button" style={{ textDecoration: 'none' }}>File New Complaint</Link>
          </div>

          {loading ? (
            <p>Loading complaints...</p>
          ) : complaints.length === 0 ? (
            <p>No complaints have been filed yet.</p>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.9rem' }}>
              {complaints.map((complaint) => (
                <div key={complaint.complaintId} style={{ border: '1px solid #e2e8f0', borderRadius: '12px', padding: '1.25rem', background: '#f8fafc' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', gap: '1rem', flexWrap: 'wrap' }}>
                    <div>
                      <div style={{ fontWeight: 700, fontSize: '1.05rem', color: '#0f172a' }}>{complaint.title}</div>
                      <div className="profile-label" style={{ marginTop: '0.25rem' }}>
                        {complaint.complaintId} • {complaint.departmentName}
                        {complaint.resolvedAt && (
                          <span style={{ marginLeft: '8px', color: '#059669', fontWeight: 600 }}>
                            • Resolved {new Date(complaint.resolvedAt).toLocaleDateString()}
                          </span>
                        )}
                      </div>
                    </div>
                    <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', flexWrap: 'wrap' }}>
                      {complaint.escalationStatus && (
                        <span style={{
                          background: '#FEF3C7',
                          color: '#B45309',
                          border: '1px solid #FCD34D',
                          borderRadius: '9999px',
                          padding: '0.2rem 0.65rem',
                          fontSize: '0.75rem',
                          fontWeight: 700
                        }}>
                          ⚠️ ESCALATED ({complaint.escalationStatus})
                        </span>
                      )}
                      <span className={`status-badge status-${getStatusTone(complaint.status)}`}>
                        {getStatusLabel(complaint.status)}
                      </span>
                    </div>
                  </div>
                  <p style={{ margin: '0.75rem 0 0.9rem', lineHeight: 1.6, color: '#334155' }}>{complaint.description}</p>
                  {complaint.status === 'DUPLICATE' && (
                    <div className="duplicate-complaint-notice" role="status">
                      <strong>Your complaint was marked as a duplicate.</strong>
                      <span>No further processing can be taken for this complaint.</span>
                    </div>
                  )}
                  <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap', alignItems: 'center' }}>
                    <Link to={`/citizen/complaints/${complaint.complaintId}`} className="secondary-button" style={{ textDecoration: 'none' }}>
                      View Details
                    </Link>
                    {isResolved(complaint.status) ? (
                      <>
                        {complaint.feedback || complaint.hasFeedback ? (
                          <span style={{
                            background: '#ECFDF5',
                            color: '#065F46',
                            border: '1px solid #A7F3D0',
                            borderRadius: '8px',
                            padding: '0.45rem 0.85rem',
                            fontSize: '0.875rem',
                            fontWeight: 600,
                            display: 'inline-flex',
                            alignItems: 'center',
                            gap: '0.35rem'
                          }}>
                            ✓ Feedback Given {complaint.feedback?.rating ? `(${complaint.feedback.rating}★)` : ''}
                          </span>
                        ) : (
                          <Link
                            to={`/citizen/complaints/${complaint.complaintId}/feedback`}
                            className="primary-button"
                            style={{
                              textDecoration: 'none',
                              background: '#059669',
                              borderColor: '#047857',
                              fontWeight: 700,
                              display: 'inline-flex',
                              alignItems: 'center',
                              gap: '0.35rem'
                            }}>
                            ⭐ Give Feedback
                          </Link>
                        )}
                        <Link to={`/citizen/complaints/${complaint.complaintId}/reopen`} className="secondary-button" style={{ textDecoration: 'none' }}>
                          Request Reopen
                        </Link>
                      </>
                    ) : complaint.status !== 'DUPLICATE' ? (
                      <>
                        <span className="action-button-disabled" title="Feedback becomes available after resolution">
                          ⭐ Give Feedback
                        </span>
                        <span className="action-button-disabled" title="Reopen becomes available after resolution">
                          Reopen
                        </span>
                        <span className="action-availability" style={{ fontSize: '0.8rem', color: '#64748b' }}>
                          (Feedback available after resolution)
                        </span>
                      </>
                    ) : (
                      <span className="action-availability">No further action available</span>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
