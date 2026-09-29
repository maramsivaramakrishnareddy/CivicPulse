import React, { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import api from '../../services/api';

const statusMeta = {
  APPLIED: { label: 'Submitted', tone: 'warning' },
  COMPLAINT_VIEWED: { label: 'Viewed', tone: 'info' },
  ACTION_TO_BE_TAKEN: { label: 'Action / In Progress', tone: 'primary' },
  COMPLAINT_RESOLVED: { label: 'Resolved', tone: 'success' },
  RESOLVED: { label: 'Resolved', tone: 'success' },
  REOPENED: { label: 'Reopened', tone: 'danger' },
  DUPLICATE: { label: 'Terminated as duplicate', tone: 'danger' },
  REJECTED: { label: 'Rejected', tone: 'danger' },
};

const isResolved = (status) => status === 'COMPLAINT_RESOLVED' || status === 'RESOLVED';

export default function ComplaintDetail({ session }) {
  const { complaintId } = useParams();
  const navigate = useNavigate();
  const [complaint, setComplaint] = useState(null);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [actionSuccess, setActionSuccess] = useState('');

  // Citizen Escalation Form State
  const [escalateOpen, setEscalateOpen] = useState(false);
  const [escReason, setEscReason] = useState('No progress after deadline');
  const [escRemarks, setEscRemarks] = useState('');
  const [escTarget, setEscTarget] = useState('');
  const [escLevel, setEscLevel] = useState(1);
  const [escSubmitting, setEscSubmitting] = useState(false);
  const [escError, setEscError] = useState('');

  const loadComplaint = async () => {
    try {
      const [complaintRes, historyRes] = await Promise.all([
        api.getComplaintById(complaintId),
        api.getComplaintHistory(complaintId),
      ]);

      setComplaint(complaintRes.data || null);
      setHistory(Array.isArray(historyRes.data) ? historyRes.data : []);
    } catch (err) {
      setError(err.message || 'Unable to load complaint details.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!complaintId) return;
    loadComplaint();
    const intervalId = setInterval(loadComplaint, 5000);
    return () => clearInterval(intervalId);
  }, [complaintId]);

  const handleCitizenEscalate = async (e) => {
    e.preventDefault();
    setEscSubmitting(true);
    setEscError('');
    setActionSuccess('');

    try {
      await api.escalateCitizenComplaint(complaintId, {
        reason: escReason,
        remarks: escRemarks,
        target: escTarget || 'Higher Departmental Authority',
        escalationLevel: Number(escLevel) || 1,
      });
      setActionSuccess('Complaint escalated successfully. An audit record and notification have been created.');
      setEscalateOpen(false);
      setEscRemarks('');
      await loadComplaint();
    } catch (err) {
      setEscError(err.message || 'Unable to escalate complaint.');
    } finally {
      setEscSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="dashboard-page">
        <div className="container">
          <div className="dashboard-card">
            <p>Loading complaint details...</p>
          </div>
        </div>
      </div>
    );
  }

  if (error || !complaint) {
    return (
      <div className="dashboard-page">
        <div className="container">
          <div className="dashboard-card">
            <p className="error-text">{error || 'Complaint not found.'}</p>
            <Link to="/citizen/dashboard" className="primary-button">Back to dashboard</Link>
          </div>
        </div>
      </div>
    );
  }

  const resolved = isResolved(complaint.status);
  const statusConfig = statusMeta[complaint.status] || { label: complaint.status, tone: 'info' };

  return (
    <div className="dashboard-page">
      <div className="container" style={{ maxWidth: '1100px' }}>
        <div className="dashboard-header">
          <div className="dashboard-welcome">
            <span className="dashboard-role-badge">Complaint Details</span>
            <h1 className="dashboard-title">{complaint.title}</h1>
            <p className="dashboard-subtitle">Tracking and resolution updates for {complaint.complaintId}</p>
          </div>
          <div>
            <span className={`status-badge status-${statusConfig.tone}`}>{statusConfig.label}</span>
          </div>
        </div>

        {actionSuccess && (
          <div className="alert alert-success" style={{ marginBottom: '1.25rem' }}>
            {actionSuccess}
          </div>
        )}

        <div className="dashboard-card" style={{ marginBottom: '1.5rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', gap: '1rem', flexWrap: 'wrap' }}>
            <div>
              <p className="profile-label">Department</p>
              <p className="profile-value">{complaint.departmentName || 'Unassigned'}</p>
            </div>
            <div>
              <p className="profile-label">Complaint Type</p>
              <p className="profile-value">{complaint.complaintType || 'NORMAL'}</p>
            </div>
            <div>
              <p className="profile-label">Assigned Officer</p>
              <p className="profile-value">{complaint.officerName || 'Under Assignment'}</p>
            </div>
            <div>
              <p className="profile-label">Created</p>
              <p className="profile-value">{complaint.createdAt ? new Date(complaint.createdAt).toLocaleString() : '—'}</p>
            </div>
            <div>
              <p className="profile-label">Location</p>
              <p className="profile-value">{complaint.location || 'Not provided'}</p>
            </div>
            <div>
              <p className="profile-label">GPS Coordinates</p>
              <p className="profile-value">
                {complaint.latitude != null && complaint.longitude != null ? `${complaint.latitude}, ${complaint.longitude}` : 'Not captured'}
              </p>
            </div>
          </div>
        </div>

        <div className="dashboard-grid" style={{ gridTemplateColumns: '1.6fr 1fr' }}>
          <div className="dashboard-card">
            <h2 className="dashboard-card-title">Complaint Overview</h2>
            <p style={{ lineHeight: 1.7 }}>{complaint.description || 'No description provided.'}</p>

            {complaint.status === 'DUPLICATE' && (
              <div className="duplicate-complaint-notice" role="status">
                <strong>Your complaint was marked as a duplicate.</strong>
                <span>No further action can be taken for this complaint.</span>
              </div>
            )}

            {complaint.escalationStatus && (
              <div style={{
                marginTop: '1.25rem',
                background: '#FFFBEB',
                border: '1px solid #FCD34D',
                padding: '1rem 1.25rem',
                borderRadius: '10px',
                color: '#92400E'
              }}>
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', fontWeight: '700', marginBottom: '0.35rem' }}>
                  <span>⚠️ Escalation Notice (Level {complaint.escalationLevel || 1} - {complaint.escalationStatus})</span>
                </div>
                <p style={{ margin: 0, fontSize: '0.9rem' }}>
                  This complaint has been escalated
                  {complaint.escalationTarget ? ` to: ${complaint.escalationTarget}` : ''}.
                  {complaint.escalationReason ? ` Reason: ${complaint.escalationReason}` : ''}
                </p>
              </div>
            )}

            {complaint.imageUrl && (
              <div style={{ marginTop: '1rem' }}>
                <p className="profile-label">Evidence Image</p>
                <img src={complaint.imageUrl} alt="Complaint evidence" style={{ maxWidth: '100%', maxHeight: '320px', borderRadius: '12px', border: '1px solid #e2e8f0' }} />
              </div>
            )}

            {complaint.videoUrl && (
              <div style={{ marginTop: '1rem' }}>
                <p className="profile-label">Evidence Video</p>
                <video controls src={complaint.videoUrl} style={{ maxWidth: '100%', maxHeight: '320px', borderRadius: '12px', border: '1px solid #e2e8f0' }} />
              </div>
            )}

            {complaint.actionTaken && (
              <div style={{ marginTop: '1.25rem', background: '#f8fafc', border: '1px solid #e2e8f0', padding: '1rem', borderRadius: '10px' }}>
                <p className="profile-label">Officer Action / Resolution Remarks</p>
                <p className="profile-value" style={{ margin: '0.25rem 0 0 0', fontWeight: 500 }}>{complaint.actionTaken}</p>
              </div>
            )}

            {/* Submitted Citizen Feedback Card */}
            {complaint.feedback && (
              <div style={{ marginTop: '1.25rem', background: '#F0FDF4', border: '1px solid #86EFAC', padding: '1rem 1.25rem', borderRadius: '10px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '0.5rem' }}>
                  <h3 style={{ margin: 0, color: '#166534', fontSize: '1rem', fontWeight: 700 }}>
                    ✓ Your Resolution Feedback
                  </h3>
                  <span style={{ fontSize: '1.25rem', color: '#EAB308' }}>
                    {'★'.repeat(complaint.feedback.rating || 5)}{'☆'.repeat(5 - (complaint.feedback.rating || 5))}
                    <span style={{ fontSize: '0.9rem', color: '#166534', marginLeft: '6px', fontWeight: 600 }}>
                      ({complaint.feedback.rating}/5)
                    </span>
                  </span>
                </div>
                <p style={{ marginTop: '0.5rem', marginBottom: '0.25rem', color: '#14532D', fontSize: '0.95rem' }}>
                  "{complaint.feedback.comment || 'No written comments provided.'}"
                </p>
                <small style={{ color: '#15803D', fontSize: '0.8rem' }}>
                  Submitted on {complaint.feedback.createdAt ? new Date(complaint.feedback.createdAt).toLocaleString() : '—'}
                </small>
              </div>
            )}
          </div>

          <div className="dashboard-card">
            <h2 className="dashboard-card-title">Actions & Next Steps</h2>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.85rem' }}>
              <Link to="/citizen/dashboard" className="secondary-button" style={{ textDecoration: 'none', textAlign: 'center' }}>
                Back to Dashboard
              </Link>

              {/* RESOLVED STATE ACTIONS */}
              {resolved && (
                <>
                  {!complaint.feedback ? (
                    <Link
                      to={`/citizen/complaints/${complaintId}/feedback`}
                      className="primary-button"
                      style={{
                        textDecoration: 'none',
                        textAlign: 'center',
                        background: '#16A34A',
                        borderColor: '#16A34A',
                        color: '#FFFFFF',
                        fontWeight: 700,
                        padding: '0.75rem 1rem'
                      }}
                    >
                      ⭐ Give Resolution Feedback
                    </Link>
                  ) : (
                    <div style={{
                      textAlign: 'center',
                      padding: '0.6rem 0.8rem',
                      background: '#DCFCE7',
                      color: '#15803D',
                      borderRadius: '8px',
                      fontSize: '0.875rem',
                      fontWeight: 600,
                      border: '1px solid #BBF7D0'
                    }}>
                      ✓ Feedback Submitted ({complaint.feedback.rating}★)
                    </div>
                  )}

                  <Link
                    to={`/citizen/complaints/${complaintId}/reopen`}
                    className="secondary-button"
                    style={{ textDecoration: 'none', textAlign: 'center' }}
                  >
                    Request Reopen
                  </Link>
                </>
              )}

              {/* UNRESOLVED STATE FEEDBACK HINT */}
              {!resolved && (
                <div style={{
                  textAlign: 'center',
                  padding: '0.6rem 0.8rem',
                  background: '#F8FAFC',
                  color: '#64748B',
                  borderRadius: '8px',
                  fontSize: '0.825rem',
                  border: '1px solid #E2E8F0'
                }}>
                  Feedback will become available after resolution.
                </div>
              )}

              {/* CITIZEN ESCALATION ACTION */}
              {complaint.status !== 'DUPLICATE' && (
                <div>
                  <button
                    type="button"
                    onClick={() => setEscalateOpen((prev) => !prev)}
                    className="secondary-button"
                    style={{
                      width: '100%',
                      borderColor: '#F59E0B',
                      color: '#B45309',
                      background: '#FFFBEB',
                      fontWeight: 600
                    }}
                  >
                    {escalateOpen ? 'Close Escalation Form' : (complaint.escalationStatus ? '⚠️ Re-escalate Complaint' : '⚠️ Escalate Complaint')}
                  </button>
                </div>
              )}
            </div>

            {/* CITIZEN ESCALATION FORM */}
            {escalateOpen && (
              <div style={{
                marginTop: '1.25rem',
                padding: '1.2rem',
                background: '#FEF3C7',
                border: '1px solid #FCD34D',
                borderRadius: '10px'
              }}>
                <h3 style={{ margin: '0 0 0.75rem 0', color: '#92400E', fontSize: '1rem', fontWeight: 700 }}>
                  Escalate Complaint to Higher Authority
                </h3>

                {escError && (
                  <div className="alert alert-error" style={{ marginBottom: '0.75rem', fontSize: '0.85rem' }}>
                    {escError}
                  </div>
                )}

                <form onSubmit={handleCitizenEscalate} style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                  <div>
                    <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: '#78350F', marginBottom: '0.25rem' }}>
                      Escalation Level
                    </label>
                    <select
                      value={escLevel}
                      onChange={(e) => setEscLevel(e.target.value)}
                      style={{ width: '100%', padding: '0.45rem', borderRadius: '6px', border: '1px solid #D97706' }}
                    >
                      <option value={1}>Level 1 - Senior Officer / Supervisor</option>
                      <option value={2}>Level 2 - Department Head / Zonal Officer</option>
                      <option value={3}>Level 3 - Municipal Commissioner / Ministry</option>
                    </select>
                  </div>

                  <div>
                    <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: '#78350F', marginBottom: '0.25rem' }}>
                      Reason for Escalation
                    </label>
                    <select
                      value={escReason}
                      onChange={(e) => setEscReason(e.target.value)}
                      style={{ width: '100%', padding: '0.45rem', borderRadius: '6px', border: '1px solid #D97706' }}
                    >
                      <option>No progress after deadline</option>
                      <option>Officer not responding</option>
                      <option>Unsatisfactory resolution or action</option>
                      <option>Emergency / Critical issue ignored</option>
                      <option>Requires higher departmental authority</option>
                      <option>Other</option>
                    </select>
                  </div>

                  <div>
                    <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: '#78350F', marginBottom: '0.25rem' }}>
                      Target Authority (optional)
                    </label>
                    <input
                      type="text"
                      value={escTarget}
                      onChange={(e) => setEscTarget(e.target.value)}
                      placeholder="e.g. Zonal Head, Commissioner Office"
                      style={{ width: '100%', padding: '0.45rem', borderRadius: '6px', border: '1px solid #D97706' }}
                    />
                  </div>

                  <div>
                    <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: '#78350F', marginBottom: '0.25rem' }}>
                      Escalation Remarks / Details *
                    </label>
                    <textarea
                      required
                      rows={3}
                      value={escRemarks}
                      onChange={(e) => setEscRemarks(e.target.value)}
                      placeholder="Explain why this complaint requires immediate escalation..."
                      style={{ width: '100%', padding: '0.45rem', borderRadius: '6px', border: '1px solid #D97706' }}
                    />
                  </div>

                  <button
                    type="submit"
                    className="primary-button"
                    disabled={escSubmitting}
                    style={{
                      background: '#B45309',
                      borderColor: '#B45309',
                      color: '#FFF',
                      fontWeight: 700
                    }}
                  >
                    {escSubmitting ? 'Submitting Escalation...' : 'Confirm Escalation'}
                  </button>
                </form>
              </div>
            )}
          </div>
        </div>

        <div className="dashboard-card" style={{ marginTop: '1.5rem' }}>
          <h2 className="dashboard-card-title">Complaint History & Audit Trail</h2>
          {history.length === 0 ? (
            <p>No history available yet.</p>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.9rem' }}>
              {history.map((item) => (
                <div key={item.historyId || `${item.createdAt}-${item.status}`} style={{ borderLeft: '3px solid #3b82f6', background: '#f8fafc', borderRadius: '10px', padding: '0.9rem 1rem' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', gap: '1rem', alignItems: 'center', flexWrap: 'wrap' }}>
                    <strong style={{ color: '#1E293B' }}>{item.status || 'Status Update'}</strong>
                    <span className="profile-label">{item.createdAt ? new Date(item.createdAt).toLocaleString() : '—'}</span>
                  </div>
                  <p style={{ marginTop: '0.45rem', marginBottom: 0, color: '#475569' }}>{item.message || 'Status update recorded.'}</p>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
