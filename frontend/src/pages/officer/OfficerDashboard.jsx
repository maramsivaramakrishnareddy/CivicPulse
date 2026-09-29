import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../../services/api';

const statusMeta = {
  APPLIED: { label: 'Applied', pill: 'officer-status-pending' },
  COMPLAINT_VIEWED: { label: 'Viewed', pill: 'officer-status-pending' },
  ACTION_TO_BE_TAKEN: { label: 'In Progress', pill: 'officer-status-progress' },
  COMPLAINT_RESOLVED: { label: 'Resolved', pill: 'officer-status-resolved' },
  REOPENED: { label: 'Reopened', pill: 'officer-status-reopened' },
  DUPLICATE: { label: 'Duplicate', pill: 'officer-status-duplicate' },
  REJECTED: { label: 'Rejected', pill: 'officer-status-rejected' },
};

const Stat = ({ label, value, className = '' }) => <div className={`officer-stat-card ${className}`}><div className="officer-stat-label">{label}</div><div className="officer-stat-value">{value ?? 0}</div></div>;

export default function OfficerDashboard() {
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = async () => {
    setLoading(true); setError('');
    try { const response = await api.getOfficerDashboard(); setDashboard(response.data || null); }
    catch (err) { setError(err.message || 'Unable to load officer dashboard.'); }
    finally { setLoading(false); }
  };

  // Load once on mount. No automatic polling/setInterval: a timer-driven
  // refresh here would re-show the "Loading..." state and flash the whole
  // dashboard every few seconds.
  useEffect(() => { load(); }, []);

  if (loading) return <div className="dashboard-card"><p>Loading officer dashboard...</p></div>;
  if (error) return <div className="alert alert-error">{error}</div>;

  const recent = dashboard?.recentComplaints || [];
  return <>
    <div className="dashboard-header dashboard-header-officer">
      <div className="dashboard-welcome">
        <span className="dashboard-role-badge">Officer Dashboard</span>
        <h1 className="dashboard-title">Welcome, {dashboard?.officerName || 'Officer'}</h1>
        <p className="dashboard-subtitle">Department: <strong>{dashboard?.department || '—'}</strong> · Employee ID: {dashboard?.employeeId || '—'}</p>
      </div>
    </div>

    <div className="officer-stats-grid">
      <Stat label="Total Complaints" value={dashboard?.totalCount} />
      <Stat label="Pending" value={dashboard?.pendingCount} className="officer-stat-pending" />
      <Stat label="In Progress" value={dashboard?.inProgressCount} className="officer-stat-progress" />
      <Stat label="Resolved" value={dashboard?.resolvedCount} className="officer-stat-resolved" />
      <Stat label="Reopened" value={dashboard?.reopenedCount} className="officer-stat-reopened" />
      <Stat label="Duplicate" value={dashboard?.duplicateCount} className="officer-stat-duplicate" />
      <Stat label="Escalated" value={dashboard?.escalatedCount} className="officer-stat-escalated" />
      <Stat label="Rejected" value={dashboard?.rejectedCount} className="officer-stat-rejected" />
    </div>

    <div className="officer-attention-grid">
      <Link to="/officer/duplicates" className="officer-attention-card">🔍 <strong>{dashboard?.pendingDuplicateCount || 0}</strong> possible duplicates awaiting verification</Link>
      <Link to="/officer/escalated" className="officer-attention-card">⚡ <strong>{dashboard?.escalatedCount || 0}</strong> escalated complaints</Link>
      <div className="officer-attention-card">⚠️ <strong>{dashboard?.attentionCount || 0}</strong> complaints require your attention</div>
      <div className="officer-attention-card">🚨 <strong>{dashboard?.emergencyPendingCount || 0}</strong> emergency complaints pending</div>
    </div>

    <div className="dashboard-card officer-complaints">
      <div className="officer-section-heading-row"><h2 className="dashboard-card-title">Recent Complaints</h2><Link to="/officer/complaints" className="secondary-button" style={{ textDecoration: 'none' }}>View All Complaints</Link></div>
      {recent.length === 0 ? <p>No complaints available for your department yet.</p> : <div className="table-wrapper"><table className="data-table"><thead><tr><th>Complaint ID</th><th>Title</th><th>Priority</th><th>Status</th><th>Submitted</th><th>Action</th></tr></thead><tbody>
        {recent.map(c => { const meta = statusMeta[c.status] || { label: c.status, pill: 'officer-status-pending' }; return <tr key={c.complaintId}>
          <td>{c.complaintId}</td><td>{c.title}</td><td><span className={`officer-priority ${c.complaintType === 'EMERGENCY' ? 'emergency' : ''}`}>{c.complaintType === 'EMERGENCY' ? 'Emergency' : 'Normal'}</span></td>
          <td><span className={`officer-status-pill ${meta.pill}`}>{meta.label}</span></td><td>{c.createdAt ? new Date(c.createdAt).toLocaleString() : '—'}</td>
          <td><Link to={`/officer/complaints/${c.complaintId}`} className="secondary-button" style={{ textDecoration: 'none' }}>View</Link></td>
        </tr>; })}
      </tbody></table></div>}
    </div>
  </>;
}
