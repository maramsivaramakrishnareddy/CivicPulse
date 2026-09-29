import React, { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import api from '../../services/api';

const labels = {
  APPLIED: 'Applied',
  COMPLAINT_VIEWED: 'Viewed',
  ACTION_TO_BE_TAKEN: 'In Progress',
  COMPLAINT_RESOLVED: 'Resolved',
  RESOLVED: 'Resolved',
  REOPENED: 'Reopened',
  DUPLICATE: 'Duplicate',
  REJECTED: 'Rejected',
};

const pill = {
  APPLIED: 'officer-status-pending',
  COMPLAINT_VIEWED: 'officer-status-pending',
  ACTION_TO_BE_TAKEN: 'officer-status-progress',
  COMPLAINT_RESOLVED: 'officer-status-resolved',
  RESOLVED: 'officer-status-resolved',
  REOPENED: 'officer-status-reopened',
  DUPLICATE: 'officer-status-duplicate',
  REJECTED: 'officer-status-rejected',
};

// REOPENED is intentionally excluded: reopening a complaint is a Citizen-only
// action. Officers must never be able to set a complaint's status to REOPENED
// from this dropdown.
const all = [
  ['APPLIED', 'Applied'],
  ['COMPLAINT_VIEWED', 'Viewed'],
  ['ACTION_TO_BE_TAKEN', 'In Progress'],
  ['COMPLAINT_RESOLVED', 'Resolved'],
  ['REJECTED', 'Rejected'],
];

const Field = ({ label, children }) => (
  <div className="officer-review-field">
    <div className="officer-review-field-label">{label}</div>
    <div className="officer-review-field-value">{children || '—'}</div>
  </div>
);

function openEvidenceImage(dataUrl) {
  // Always render the evidence inside a new tab instead of navigating directly
  // to the file URL. This keeps the action as "view" and avoids triggering a
  // download when the server marks the underlying resource as an attachment.
  // Open a real blank tab first, then write an HTML document into it.
  // Do not use the noopener/noreferrer window feature here: some browsers
  // return a null WindowProxy for that combination, which leaves a blank tab
  // that our script cannot populate.
  const newTab = window.open('', '_blank');

  if (!newTab) {
    window.alert('Please allow pop-ups for this site to view the image in a new tab.');
    return;
  }

  const safeSrc = String(dataUrl || '').replace(/&/g, '&amp;').replace(/"/g, '&quot;');
  newTab.document.open();
  newTab.document.write(`<!doctype html>
<html>
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width,initial-scale=1">
  <title>Complaint Evidence</title>
  <style>
    html,body{margin:0;width:100%;height:100%;background:#111;overflow:auto;}
    body{display:flex;align-items:center;justify-content:center;}
    img{display:block;max-width:100%;max-height:100%;width:auto;height:auto;object-fit:contain;}
  </style>
</head>
<body>
  <img src="${safeSrc}" alt="Complaint evidence">
</body>
</html>`);
  newTab.document.close();
}

export default function ComplaintReview() {
  const { complaintId } = useParams();
  const nav = useNavigate();
  const [c, setC] = useState(null);
  const [history, setHistory] = useState([]);
  const [status, setStatus] = useState('');
  const [remarks, setRemarks] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Escalation State
  const [escalateOpen, setEscalateOpen] = useState(false);
  const [escReason, setEscReason] = useState('Requires Higher Authority');
  const [escRemarks, setEscRemarks] = useState('');
  const [escTarget, setEscTarget] = useState('');
  const [escLevel, setEscLevel] = useState(1);
  const [escSaving, setEscSaving] = useState(false);

  // Load the complaint once when the review page opens. We intentionally do
  // not poll in the background because polling can overwrite an officer's
  // selected status/remarks while they are working on the form.
  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const [a, b] = await Promise.all([
        api.getMyComplaintById(complaintId),
        api.getMyComplaintHistory(complaintId),
      ]);
      setC(a.data);
      setStatus(a.data?.status || '');
      setHistory(b.data || []);
    } catch (e) {
      setError(e.message || 'Unable to load complaint.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!complaintId) return;
    load();
  }, [complaintId]);

  const save = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    setSuccess('');
    try {
      // The PUT endpoint returns the updated complaint detail. Use that
      // response directly so the page updates immediately without a full
      // complaint reload or a loading/refresh flash.
      const response = await api.updateMyComplaintStatus(complaintId, { status, remarks });
      const updatedComplaint = response?.data;

      if (updatedComplaint) {
        setC((current) => ({ ...current, ...updatedComplaint }));
        setStatus(updatedComplaint.status || status);
      } else {
        setC((current) => ({ ...current, status }));
        setStatus(status);
      }

      setRemarks('');
      setSuccess('Complaint status updated successfully.');

      // Refresh only the audit trail because a new history entry was created
      // by the PUT request. This does not reload the complaint or reset the form.
      try {
        const historyResponse = await api.getMyComplaintHistory(complaintId);
        setHistory(historyResponse?.data || []);
      } catch (_) {
        // The status update already succeeded; keep the new status visible even
        // if the optional history refresh fails.
      }
    } catch (e) {
      setError(e.message || 'Unable to update complaint.');
    } finally {
      setSaving(false);
    }
  };

  const escalate = async (e) => {
    e.preventDefault();
    setEscSaving(true);
    setError('');
    try {
      const response = await api.escalateOfficerComplaint(complaintId, {
        reason: escReason,
        remarks: escRemarks,
        target: escTarget || 'Higher Authority / Head Office',
        escalationLevel: Number(escLevel) || 1,
      });
      if (response?.data) setC((current) => ({ ...current, ...response.data }));
      setSuccess('Complaint escalated successfully.');
      setEscalateOpen(false);
      setEscRemarks('');
    } catch (e) {
      setError(e.message || 'Unable to escalate complaint.');
    } finally {
      setEscSaving(false);
    }
  };

  if (loading) return <div className="dashboard-card"><p>Loading complaint...</p></div>;
  if (error && !c) {
    return (
      <>
        <div className="alert alert-error">{error}</div>
        <Link to="/officer/complaints" className="secondary-button" style={{ textDecoration: 'none' }}>
          Back to Complaints
        </Link>
      </>
    );
  }

  const meta = labels[c?.status] || c?.status;

  return (
    <>
      <div className="dashboard-header dashboard-header-officer">
        <div className="dashboard-welcome">
          <span className="dashboard-role-badge">Complaint Review</span>
          <h1 className="dashboard-title">{c?.complaintId}</h1>
          <p className="dashboard-subtitle">{c?.title}</p>
        </div>
        <Link to="/officer/complaints" className="secondary-button" style={{ textDecoration: 'none' }}>
          Back to Complaints
        </Link>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      {/* PROMINENT ESCALATION ALERT IF ESCALATED */}
      {c.latestEscalation && (
        <div className="dashboard-card" style={{ borderLeft: '5px solid #DC2626', marginBottom: '1.25rem', background: '#FEF2F2' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '0.5rem' }}>
            <h2 className="dashboard-card-title" style={{ margin: 0, color: '#991B1B' }}>
              ⚠️ ACTIVE ESCALATION (Level {c.latestEscalation.escalationLevel || 1})
            </h2>
            <span className={`officer-status-pill ${c.latestEscalation.status === 'RESOLVED' ? 'officer-status-resolved' : 'officer-status-rejected'}`}>
              Status: {c.latestEscalation.status || 'PENDING'}
            </span>
          </div>
          <div className="officer-review-grid" style={{ marginTop: '1rem' }}>
            <Field label="Escalated By">{c.latestEscalation.officerName || 'Citizen / Officer'}</Field>
            <Field label="Escalated On">{c.latestEscalation.createdAt ? new Date(c.latestEscalation.createdAt).toLocaleString() : '—'}</Field>
            <Field label="Reason">{c.latestEscalation.reason}</Field>
            <Field label="Escalated To">{c.latestEscalation.target || 'Higher authority'}</Field>
            <Field label="Remarks">{c.latestEscalation.remarks || '—'}</Field>
            <Field label="Level">{c.latestEscalation.escalationLevel || 1}</Field>
          </div>
          {c.latestEscalation.status !== 'RESOLVED' && (
            <div style={{ marginTop: '1.25rem', paddingTop: '1rem', borderTop: '1px solid #FECACA' }}>
              <button
                type="button"
                className="primary-button"
                style={{ background: '#16A34A', borderColor: '#16A34A' }}
                onClick={async () => {
                  try {
                    await api.updateEscalationStatus(c.latestEscalation.escalationId, {
                      status: 'RESOLVED',
                      remarks: 'Escalation resolved by reviewing officer.',
                    });
                    setC((current) => ({
                      ...current,
                      latestEscalation: {
                        ...current.latestEscalation,
                        status: 'RESOLVED',
                        remarks: 'Escalation resolved by reviewing officer.',
                      },
                    }));
                    setSuccess('Escalation marked as resolved successfully.');
                  } catch (err) {
                    setError(err.message || 'Failed to resolve escalation.');
                  }
                }}
              >
                ✓ Mark Escalation as Resolved
              </button>
            </div>
          )}
        </div>
      )}

      <div className="officer-review-grid">
        <div className="dashboard-card">
          <h2 className="dashboard-card-title">Complaint Information</h2>
          <Field label="Complaint ID">{c.complaintId}</Field>
          <Field label="Title">{c.title}</Field>
          <Field label="Description">{c.description}</Field>
          <Field label="Department">{c.departmentName}</Field>
          <Field label="Department ID">{c.departmentId}</Field>
          <Field label="Location">📍 {c.location}</Field>
          <Field label="Latitude / Longitude">
            {c.latitude != null && c.longitude != null ? `${c.latitude}, ${c.longitude}` : 'Not captured'}
          </Field>
          <Field label="Priority / Type">
            <span className={`officer-priority ${c.complaintType === 'EMERGENCY' ? 'emergency' : ''}`}>
              {c.complaintType === 'EMERGENCY' ? 'Emergency' : 'Normal'}
            </span>
          </Field>
          {c.aiPriority && (
            <Field label="AI-Detected Priority">
              <span className={`officer-priority ${c.aiPriority === 'HIGH' ? 'emergency' : ''}`}>
                🤖 {c.aiPriority}
              </span>
            </Field>
          )}
          <Field label="Current Status">
            <span className={`officer-status-pill ${pill[c.status] || 'officer-status-pending'}`}>{meta}</span>
          </Field>
          <Field label="Submitted Date / Time">{c.createdAt ? new Date(c.createdAt).toLocaleString() : '—'}</Field>
        </div>

        <div className="dashboard-card">
          <h2 className="dashboard-card-title">Citizen Information</h2>
          <Field label="Citizen Name">{c.citizenName}</Field>
          <Field label="Citizen ID">{c.citizenId}</Field>
          <Field label="Email">{c.citizenEmail}</Field>
          <Field label="Contact">{c.citizenPhone}</Field>

          <h2 className="dashboard-card-title officer-subcard-title">Officer Action</h2>
          <form className="officer-review-form" onSubmit={save}>
            <label>Status</label>
            <select value={status} onChange={(e) => setStatus(e.target.value)} disabled={c.status === 'DUPLICATE'}>
              {all.map((o) => (
                <option key={o[0]} value={o[0]}>{o[1]}</option>
              ))}
            </select>
            <label>Resolution / Action Remarks</label>
            <textarea
              rows={4}
              value={remarks}
              onChange={(e) => setRemarks(e.target.value)}
              placeholder="Enter action taken or resolution notes..."
            />
            <button className="primary-button" disabled={saving || c.status === 'DUPLICATE'}>
              {saving ? 'Saving Update...' : 'Save Update'}
            </button>
          </form>

          <div style={{ marginTop: '1rem', paddingTop: '1rem', borderTop: '1px solid #E2E8F0' }}>
            <button
              className="secondary-button officer-full-button"
              type="button"
              onClick={() => setEscalateOpen((v) => !v)}
              disabled={c.status === 'DUPLICATE'}
              style={{ borderColor: '#D97706', color: '#B45309', background: '#FFFBEB', fontWeight: 600 }}
            >
              {escalateOpen ? 'Close Escalation Form' : '⚠️ Escalate Complaint'}
            </button>
          </div>

          {/* INLINE OFFICER ESCALATION FORM */}
          {escalateOpen && (
            <div style={{ marginTop: '1rem', padding: '1rem', background: '#FEF3C7', borderRadius: '8px', border: '1px solid #FCD34D' }}>
              <h3 style={{ margin: '0 0 0.75rem 0', color: '#92400E', fontSize: '0.95rem', fontWeight: 700 }}>
                Escalate Complaint to Higher Authority
              </h3>
              <form className="officer-review-form" onSubmit={escalate}>
                <label style={{ color: '#78350F', fontWeight: 600 }}>Escalation Level</label>
                <select value={escLevel} onChange={(e) => setEscLevel(e.target.value)}>
                  <option value={1}>Level 1 - Senior Officer / Supervisor</option>
                  <option value={2}>Level 2 - Department Head / Zonal Officer</option>
                  <option value={3}>Level 3 - Municipal Commissioner / Ministry</option>
                </select>

                <label style={{ color: '#78350F', fontWeight: 600 }}>Reason</label>
                <select value={escReason} onChange={(e) => setEscReason(e.target.value)}>
                  <option>Requires Higher Authority</option>
                  <option>Outside Officer's Authority</option>
                  <option>Requires Additional Departmental Action</option>
                  <option>Serious/Emergency Issue</option>
                  <option>Resource/Approval Required</option>
                  <option>Other</option>
                </select>

                <label style={{ color: '#78350F', fontWeight: 600 }}>Escalate To (Authority / Department)</label>
                <input
                  value={escTarget}
                  onChange={(e) => setEscTarget(e.target.value)}
                  placeholder="e.g. Municipal Commissioner, Zonal Head"
                />

                <label style={{ color: '#78350F', fontWeight: 600 }}>Escalation Remarks *</label>
                <textarea
                  required
                  rows={3}
                  value={escRemarks}
                  onChange={(e) => setEscRemarks(e.target.value)}
                  placeholder="Specify why this complaint requires escalation..."
                />

                <button
                  className="primary-button"
                  disabled={escSaving}
                  style={{ background: '#B45309', borderColor: '#B45309', color: '#FFF' }}
                >
                  {escSaving ? 'Confirming...' : 'Confirm Escalation'}
                </button>
              </form>
            </div>
          )}
        </div>
      </div>

      <div className="dashboard-card officer-evidence-card">
        <h2 className="dashboard-card-title">Attachments / Evidence</h2>
        <div className="officer-evidence-grid">
          <div>
            <h3>Photo Evidence</h3>
            {c.imageUrl ? (
              <>
                <img src={c.imageUrl} alt="Citizen complaint evidence" className="officer-evidence-image" />
                <button type="button" onClick={() => openEvidenceImage(c.imageUrl)} className="secondary-button">
                  View Full Image
                </button>
                {c.aiPhotoMismatchSuspected && (
                  <div className="alert alert-error" style={{ marginTop: '10px' }}>
                    ⚠️ AI flagged this photo as not matching the description
                    {c.aiPhotoMismatchReason ? `: ${c.aiPhotoMismatchReason}` : ''}
                  </div>
                )}
              </>
            ) : (
              <p>No photo evidence submitted.</p>
            )}
          </div>
          <div>
            <h3>Video Evidence</h3>
            {c.videoUrl ? (
              <video controls src={c.videoUrl} className="officer-evidence-video" />
            ) : (
              <p>No video evidence submitted.</p>
            )}
          </div>
        </div>
      </div>

      {c.feedback && (
        <div className="dashboard-card" style={{ borderLeft: '4px solid #16A34A', background: '#F0FDF4' }}>
          <h2 className="dashboard-card-title" style={{ color: '#166534' }}>Citizen Feedback & Rating</h2>
          <div className="officer-review-grid">
            <Field label="Rating">
              <span style={{ fontSize: '1.2rem', color: '#F59E0B' }}>
                {'★'.repeat(c.feedback.rating)}{'☆'.repeat(5 - c.feedback.rating)}
              </span>{' '}
              ({c.feedback.rating}/5)
            </Field>
            <Field label="Citizen Name">{c.feedback.citizenName || c.citizenName}</Field>
            <Field label="Submitted On">
              {c.feedback.createdAt ? new Date(c.feedback.createdAt).toLocaleString() : '—'}
            </Field>
            <Field label="Comments">{c.feedback.comment || 'No written comment'}</Field>
            {c.feedback.location && <Field label="Location">📍 {c.feedback.location}</Field>}
          </div>
          {(c.feedback.imageUrl || c.feedback.videoUrl) && (
            <div style={{ marginTop: '1rem', display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
              {c.feedback.imageUrl && (
                <div>
                  <p className="officer-review-field-label">Feedback Photo</p>
                  <img
                    src={c.feedback.imageUrl}
                    alt="Feedback evidence"
                    style={{ maxWidth: '200px', maxHeight: '160px', borderRadius: '8px', border: '1px solid #E2E8F0' }}
                  />
                  <button
                    type="button"
                    onClick={() => openEvidenceImage(c.feedback.imageUrl)}
                    className="secondary-button"
                    style={{ display: 'block', marginTop: '4px', fontSize: '0.75rem' }}
                  >
                    View Full
                  </button>
                </div>
              )}
              {c.feedback.videoUrl && (
                <div>
                  <p className="officer-review-field-label">Feedback Video</p>
                  <video
                    controls
                    src={c.feedback.videoUrl}
                    style={{ maxWidth: '240px', maxHeight: '160px', borderRadius: '8px', border: '1px solid #E2E8F0' }}
                  />
                </div>
              )}
            </div>
          )}
        </div>
      )}

      {c.reopenRequest && (
        <div className="dashboard-card officer-reopen-card">
          <h2 className="dashboard-card-title">REOPEN REQUEST</h2>
          <Field label="Reopen Requested By">
            {c.reopenRequest.citizenName} {c.reopenRequest.citizenId ? `(ID: ${c.reopenRequest.citizenId})` : ''}
          </Field>
          <Field label="Requested On">
            {c.reopenRequest.createdAt ? new Date(c.reopenRequest.createdAt).toLocaleString() : '—'}
          </Field>
          <Field label="Reason">{c.reopenRequest.reason}</Field>
          {(c.reopenRequest.imageUrl || c.reopenRequest.videoUrl) && (
            <div className="officer-evidence-grid" style={{ marginTop: '1rem' }}>
              <div>
                <h3>Reopen Photo Evidence</h3>
                {c.reopenRequest.imageUrl ? (
                  <>
                    <img src={c.reopenRequest.imageUrl} alt="Reopen evidence" className="officer-evidence-image" />
                    <button type="button" onClick={() => openEvidenceImage(c.reopenRequest.imageUrl)} className="secondary-button">
                      View Full Image
                    </button>
                  </>
                ) : (
                  <p>No photo evidence submitted with reopen request.</p>
                )}
              </div>
              <div>
                <h3>Reopen Video Evidence</h3>
                {c.reopenRequest.videoUrl ? (
                  <video controls src={c.reopenRequest.videoUrl} className="officer-evidence-video" />
                ) : (
                  <p>No video evidence submitted with reopen request.</p>
                )}
              </div>
            </div>
          )}
        </div>
      )}

      {c.possibleDuplicateComplaintId && (
        <div className="dashboard-card officer-duplicate-notice">
          <h2 className="dashboard-card-title">⚠️ Possible Duplicate</h2>
          <p>
            <strong>{c.textSimilarity}% similarity</strong> with complaint <strong>{c.possibleDuplicateComplaintId}</strong>. Verify before confirming.
          </p>
          <Link to={`/officer/duplicates/${c.complaintId}`} className="secondary-button" style={{ textDecoration: 'none' }}>
            Review Possible Duplicate
          </Link>
        </div>
      )}

      {c.duplicateOfComplaintId && (
        <div className="dashboard-card officer-duplicate-notice">
          <h2 className="dashboard-card-title">Duplicate Complaint</h2>
          <p>This complaint was linked to original complaint <strong>{c.duplicateOfComplaintId}</strong>.</p>
        </div>
      )}

      <h2 className="officer-section-title">Complaint History & Audit Trail</h2>
      <div className="dashboard-card">
        {history.length === 0 ? (
          <p>No history recorded yet.</p>
        ) : (
          <ul className="officer-history-timeline">
            {history.map((e) => (
              <li key={e.historyId} className="officer-history-item">
                <div className="officer-history-status">
                  {e.previousStatus ? `${labels[e.previousStatus] || e.previousStatus} → ` : ''}
                  {labels[e.newStatus] || e.newStatus}
                </div>
                <div className="officer-history-meta">
                  {e.officerName ? `By ${e.officerName} · ` : ''}
                  {e.timestamp ? new Date(e.timestamp).toLocaleString() : ''}
                </div>
                <div className="officer-history-remarks">{e.remarks}</div>
              </li>
            ))}
          </ul>
        )}
      </div>
    </>
  );
}

