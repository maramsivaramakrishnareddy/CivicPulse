import React, { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../../services/api';

export default function DuplicateComplaints() {
  const [items, setItems] = useState([]);
  const [confirmedCount, setConfirmedCount] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [q, setQ] = useState('');

  const load = async () => {
    setLoading(true);
    try {
      const [r, d] = await Promise.all([api.getOfficerDuplicates(), api.getOfficerDashboard()]);
      setItems(r.data || []);
      setConfirmedCount(d.data?.duplicateCount || 0);
    } catch (e) {
      setError(e.message || 'Unable to load duplicate candidates.');
    } finally {
      setLoading(false);
    }
  };

  // Load once on mount. No automatic polling/setInterval: a timer-driven
  // refresh here would repeatedly flash the table back to a loading state
  // while the officer is reviewing duplicates.
  useEffect(() => { load(); }, []);

  const pendingCount = useMemo(
    () => items.filter((x) => x.duplicateStatus === 'PENDING_REVIEW').length,
    [items]
  );

  const filtered = useMemo(
    () => items.filter((x) => !q || `${x.complaintId} ${x.title} ${x.location}`.toLowerCase().includes(q.toLowerCase())),
    [items, q]
  );

  return (
    <>
      <div className="dashboard-header dashboard-header-officer">
        <div className="dashboard-welcome">
          <span className="dashboard-role-badge">Duplicate Verification</span>
          <h1 className="dashboard-title">Duplicate Complaints</h1>
          <p className="dashboard-subtitle">Rule-based possible duplicate detection. Officer verification is required.</p>
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      <div className="officer-stats-grid">
        <div className="officer-stat-card">
          <div className="officer-stat-label">Total Duplicate Complaints</div>
          <div className="officer-stat-value">{items.length}</div>
        </div>
        <div className="officer-stat-card officer-stat-pending">
          <div className="officer-stat-label">Detected / Pending Verification</div>
          <div className="officer-stat-value">{pendingCount}</div>
        </div>
        <div className="officer-stat-card officer-stat-duplicate">
          <div className="officer-stat-label">Confirmed Duplicates</div>
          <div className="officer-stat-value">{confirmedCount}</div>
        </div>
      </div>

      <div className="dashboard-card officer-complaints">
        <div className="officer-filter-row">
          <input value={q} onChange={(e) => setQ(e.target.value)} placeholder="Search possible duplicates..." />
        </div>

        {loading ? (
          <p>Loading duplicate candidates...</p>
        ) : filtered.length === 0 ? (
          <p>No duplicate complaints require verification.</p>
        ) : (
          <div className="table-wrapper">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Complaint ID</th>
                  <th>Title</th>
                  <th>Location</th>
                  <th>Match</th>
                  <th>Status</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((x) => (
                  <tr key={x.complaintId}>
                    <td>{x.complaintId}</td>
                    <td>{x.title}</td>
                    <td>{x.location || '—'}</td>
                    <td>{x.similarity != null ? `${x.similarity}%` : '—'}</td>
                    <td>{x.duplicateStatus === 'PENDING_REVIEW' ? 'Pending Verification' : x.duplicateStatus === 'DUPLICATE' ? 'Confirmed Duplicate' : x.duplicateStatus}</td>
                    <td>
                      <Link to={`/officer/duplicates/${x.complaintId}`} className="secondary-button" style={{ textDecoration: 'none' }}>
                        Review
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </>
  );
}
