import React, { useEffect, useState } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import api from '../../services/api';
import LiveEvidenceCapture from '../../components/LiveEvidenceCapture';

const isResolved = (status) => status === 'COMPLAINT_RESOLVED' || status === 'RESOLVED';

export default function FeedbackForm() {
  const { complaintId } = useParams();
  const navigate = useNavigate();
  const [complaint, setComplaint] = useState(null);
  const [checkingComplaint, setCheckingComplaint] = useState(true);
  const [formData, setFormData] = useState({
    rating: 5,
    comment: '',
    imageUrl: '',
    videoUrl: '',
    latitude: '',
    longitude: '',
    location: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchComplaint = async () => {
      try {
        const res = await api.getComplaintById(complaintId);
        if (res.data) {
          setComplaint(res.data);
          // If already has location on complaint, prefill default location
          if (res.data.location && !formData.location) {
            setFormData((prev) => ({ ...prev, location: res.data.location }));
          }
        }
      } catch (err) {
        setError(err.message || 'Unable to load complaint information.');
      } finally {
        setCheckingComplaint(false);
      }
    };

    if (complaintId) {
      fetchComplaint();
    }
  }, [complaintId]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.rating) {
      setError('Rating is required.');
      return;
    }
    setLoading(true);
    setError('');

    try {
      await api.submitFeedback(complaintId, {
        rating: Number(formData.rating),
        comment: formData.comment || '',
        imageUrl: formData.imageUrl || null,
        videoUrl: formData.videoUrl || null,
        latitude: formData.latitude ? Number(formData.latitude) : null,
        longitude: formData.longitude ? Number(formData.longitude) : null,
        location: formData.location || null,
      });
      navigate(`/citizen/complaints/${complaintId}`);
    } catch (err) {
      setError(err.message || 'Unable to submit feedback.');
    } finally {
      setLoading(false);
    }
  };

  if (checkingComplaint) {
    return (
      <div className="dashboard-page">
        <div className="container" style={{ maxWidth: '800px' }}>
          <div className="dashboard-card">
            <p>Loading complaint details...</p>
          </div>
        </div>
      </div>
    );
  }

  // If complaint is NOT resolved yet
  if (complaint && !isResolved(complaint.status)) {
    return (
      <div className="dashboard-page">
        <div className="container" style={{ maxWidth: '800px' }}>
          <div className="dashboard-card" style={{ borderLeft: '4px solid #F59E0B' }}>
            <h2 className="dashboard-card-title">Feedback Unavailable</h2>
            <p>
              Complaint <strong>{complaint.complaintId}</strong> is currently <strong>{complaint.status}</strong>.
            </p>
            <p style={{ color: '#64748B' }}>
              Feedback can only be provided after an assigned officer has investigated and marked the complaint as <strong>Resolved</strong>.
            </p>
            <div style={{ marginTop: '1.25rem' }}>
              <Link to={`/citizen/complaints/${complaintId}`} className="primary-button" style={{ textDecoration: 'none' }}>
                View Complaint Status
              </Link>
            </div>
          </div>
        </div>
      </div>
    );
  }

  // If feedback has ALREADY been submitted
  if (complaint && complaint.feedback) {
    return (
      <div className="dashboard-page">
        <div className="container" style={{ maxWidth: '800px' }}>
          <div className="dashboard-card" style={{ borderLeft: '4px solid #16A34A' }}>
            <h2 className="dashboard-card-title" style={{ color: '#166534' }}>✓ Feedback Already Submitted</h2>
            <p>
              You have already provided resolution feedback for complaint <strong>{complaint.complaintId}</strong>.
            </p>
            <div style={{
              background: '#F0FDF4',
              border: '1px solid #86EFAC',
              padding: '1rem',
              borderRadius: '8px',
              margin: '1rem 0'
            }}>
              <p style={{ margin: '0 0 0.5rem 0', fontWeight: 600, color: '#14532D' }}>
                Your Rating: {'★'.repeat(complaint.feedback.rating)}{'☆'.repeat(5 - complaint.feedback.rating)} ({complaint.feedback.rating}/5)
              </p>
              <p style={{ margin: 0, color: '#15803D' }}>
                "{complaint.feedback.comment || 'No written comment'}"
              </p>
            </div>
            <Link to={`/citizen/complaints/${complaintId}`} className="primary-button" style={{ textDecoration: 'none' }}>
              Back to Complaint
            </Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="dashboard-page">
      <div className="container" style={{ maxWidth: '800px' }}>
        <div className="dashboard-header">
          <div className="dashboard-welcome">
            <span className="dashboard-role-badge">Resolution Feedback</span>
            <h1 className="dashboard-title">Rate Service & Resolution</h1>
            <p className="dashboard-subtitle">
              {complaint ? `Sharing feedback for complaint: ${complaint.complaintId} (${complaint.title})` : 'Rate the resolution of your complaint.'}
            </p>
          </div>
        </div>

        {complaint && (
          <div className="dashboard-card" style={{ marginBottom: '1.25rem', background: '#F8FAFC' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', flexWrap: 'wrap', gap: '1rem' }}>
              <div>
                <span className="profile-label">Department</span>
                <p className="profile-value" style={{ margin: 0 }}>{complaint.departmentName || 'General'}</p>
              </div>
              <div>
                <span className="profile-label">Resolved Officer</span>
                <p className="profile-value" style={{ margin: 0 }}>{complaint.officerName || 'Staff Officer'}</p>
              </div>
              <div>
                <span className="profile-label">Status</span>
                <p className="profile-value" style={{ margin: 0, color: '#16A34A', fontWeight: 600 }}>Resolved ✓</p>
              </div>
            </div>
          </div>
        )}

        <div className="dashboard-card">
          <form onSubmit={handleSubmit} className="auth-form">
            {error && <div className="alert alert-error">{error}</div>}

            <div className="form-group">
              <label style={{ fontWeight: 600, marginBottom: '0.5rem', display: 'block' }}>Rating *</label>
              <div style={{ display: 'flex', gap: '8px', marginBottom: '0.5rem', fontSize: '1.75rem', cursor: 'pointer' }}>
                {[1, 2, 3, 4, 5].map((star) => (
                  <span
                    key={star}
                    onClick={() => setFormData((prev) => ({ ...prev, rating: star }))}
                    style={{
                      color: star <= formData.rating ? '#EAB308' : '#CBD5E1',
                      userSelect: 'none',
                      transition: 'transform 0.1s ease',
                    }}
                    title={`${star} Star${star > 1 ? 's' : ''}`}
                  >
                    ★
                  </span>
                ))}
              </div>
              <select name="rating" value={formData.rating} onChange={handleChange} style={{ maxWidth: '240px' }}>
                <option value={5}>5 - Excellent Service</option>
                <option value={4}>4 - Good / Satisfied</option>
                <option value={3}>3 - Fair / Average</option>
                <option value={2}>2 - Poor / Incomplete</option>
                <option value={1}>1 - Very Poor / Not Solved</option>
              </select>
            </div>

            <div className="form-group">
              <label style={{ fontWeight: 600 }}>Comments / Remarks</label>
              <textarea
                name="comment"
                value={formData.comment}
                onChange={handleChange}
                rows={4}
                placeholder="Tell us how the issue was handled and whether the resolution met your expectations..."
              />
            </div>

            <div className="form-group">
              <label style={{ fontWeight: 600 }}>Location (Optional)</label>
              <input
                type="text"
                name="location"
                value={formData.location}
                onChange={handleChange}
                readOnly={Boolean(formData.latitude && formData.longitude)}
                placeholder="Captured address will appear here"
              />
              {(formData.latitude || formData.longitude) && (
                <small className="help-text">Coordinates: {formData.latitude}, {formData.longitude}</small>
              )}
            </div>

            <LiveEvidenceCapture
              value={formData}
              onChange={(evidence) => setFormData((prev) => ({ ...prev, ...evidence }))}
            />

            <div style={{ display: 'flex', gap: '10px', marginTop: '1.25rem' }}>
              <button
                type="submit"
                className="primary-button"
                disabled={loading}
                style={{ background: '#16A34A', borderColor: '#16A34A', color: '#FFF', fontWeight: 700 }}
              >
                {loading ? 'Submitting Feedback...' : '⭐ Submit Feedback'}
              </button>
              <button
                type="button"
                onClick={() => navigate(`/citizen/complaints/${complaintId}`)}
                className="secondary-button"
              >
                Cancel
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
