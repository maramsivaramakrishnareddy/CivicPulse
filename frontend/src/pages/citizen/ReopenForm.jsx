import React, { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import api from '../../services/api';
import LiveEvidenceCapture from '../../components/LiveEvidenceCapture';

export default function ReopenForm() {
  const { complaintId } = useParams();
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    reason: '',
    imageUrl: '',
    videoUrl: '',
    latitude: '',
    longitude: '',
    location: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.imageUrl || !formData.videoUrl || !formData.latitude || !formData.longitude) {
      setError('Live geotagged photo and live video are required before submitting.');
      return;
    }
    setLoading(true);
    setError('');

    try {
      await api.requestReopen(complaintId, {
        reason: formData.reason,
        imageUrl: formData.imageUrl,
        videoUrl: formData.videoUrl,
        latitude: formData.latitude ? Number(formData.latitude) : null,
        longitude: formData.longitude ? Number(formData.longitude) : null,
        location: formData.location,
      });
      navigate(`/citizen/complaints/${complaintId}`);
    } catch (err) {
      setError(err.message || 'Unable to submit reopen request.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="dashboard-page">
      <div className="container" style={{ maxWidth: '800px' }}>
        <div className="dashboard-header">
          <div className="dashboard-welcome">
            <span className="dashboard-role-badge">Reopen Request</span>
            <h1 className="dashboard-title">Request a Complaint Review</h1>
            <p className="dashboard-subtitle">Provide evidence and your current location so the complaint can be reviewed again.</p>
          </div>
        </div>

        <div className="dashboard-card">
          <form onSubmit={handleSubmit} className="auth-form">
            {error && <div className="alert alert-error">{error}</div>}

            <div className="form-group">
              <label>Reason for Reopen</label>
              <textarea name="reason" value={formData.reason} onChange={handleChange} rows={5} placeholder="Explain why the issue resurfaced and why it needs another review." required />
            </div>

            <div className="form-group">
              <label>Current Location</label>
              <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
                <input type="text" name="location" value={formData.location} onChange={handleChange} readOnly={Boolean(formData.latitude && formData.longitude)} placeholder="Captured address will appear here" style={{ flex: 1 }} required />
              </div>
              {(formData.latitude || formData.longitude) && (
                <small className="help-text">Coordinates: {formData.latitude}, {formData.longitude}</small>
              )}
            </div>

            <LiveEvidenceCapture value={formData} onChange={(evidence) => setFormData((prev) => ({ ...prev, ...evidence }))} />

            <div style={{ display: 'flex', gap: '10px' }}>
              <button type="submit" className="primary-button" disabled={loading}>
                {loading ? 'Submitting...' : 'Submit Reopen Request'}
              </button>
              <button type="button" onClick={() => navigate(`/citizen/complaints/${complaintId}`)} className="secondary-button">
                Cancel
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
