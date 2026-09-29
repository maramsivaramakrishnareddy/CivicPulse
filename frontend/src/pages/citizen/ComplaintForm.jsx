import React, { useEffect, useRef, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../../services/api';
import LivePhotoCapture from '../../components/LivePhotoCapture';
import LiveEvidenceCapture from '../../components/LiveEvidenceCapture';
import './ComplaintForm.css';

export default function ComplaintForm({ session }) {
  const navigate = useNavigate();
  const videoRef = useRef(null);
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    departmentName: 'Sanitation Department',
    location: '',
    complaintType: 'NORMAL',
    imageUrl: '',
    videoUrl: '',
    latitude: '',
    longitude: ''
  });
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [suggestingCategory, setSuggestingCategory] = useState(false);
  const [validationErrors, setValidationErrors] = useState({});
  const [cameraStream, setCameraStream] = useState(null);
  const [mediaRecorder, setMediaRecorder] = useState(null);
  const [recording, setRecording] = useState(false);
  const [cameraReady, setCameraReady] = useState(false);
  const [recordedVideoUrl, setRecordedVideoUrl] = useState('');
  const [recordedVideoBlob, setRecordedVideoBlob] = useState(null);

  useEffect(() => {
    return () => {
      if (cameraStream) {
        cameraStream.getTracks().forEach(track => track.stop());
      }
      if (recordedVideoUrl) {
        URL.revokeObjectURL(recordedVideoUrl);
      }
    };
  }, [cameraStream, recordedVideoUrl]);

  useEffect(() => {
    if (!cameraReady || !videoRef.current || !cameraStream) return;
    videoRef.current.srcObject = cameraStream;
    videoRef.current.play().catch(() => setError('Unable to start the video preview.'));
  }, [cameraReady, cameraStream]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (validationErrors[name]) {
      setValidationErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  const handleDescriptionBlur = async () => {
    if (!formData.description || formData.description.trim().length < 10) return;
    setSuggestingCategory(true);
    try {
      const response = await api.suggestComplaintCategory(formData.description.trim());
      if (response.success && response.data?.departmentName) {
        setFormData((prev) => ({ ...prev, departmentName: response.data.departmentName }));
      }
    } catch (err) {
      // Non-blocking: if AI suggestion fails, the citizen just keeps the manually selected department.
    } finally {
      setSuggestingCategory(false);
    }
  };

  const handleFileInput = (e, field) => {
    const file = e.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = () => {
        setFormData(prev => ({
          ...prev,
          [field]: reader.result
        }));
      };
      reader.readAsDataURL(file);
    }
  };

  const openCamera = async () => {
    if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
      setError('This browser does not support live video recording.');
      return;
    }

    try {
      setError('');
      const stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: 'user' },
        audio: true,
      });

      setCameraStream(stream);
      setCameraReady(true);

      const recorder = new MediaRecorder(stream);
      const chunks = [];

      recorder.ondataavailable = (event) => {
        if (event.data.size > 0) {
          chunks.push(event.data);
        }
      };

      recorder.onstop = () => {
        const blob = new Blob(chunks, { type: recorder.mimeType || 'video/webm' });
        const previewUrl = URL.createObjectURL(blob);
        setRecordedVideoBlob(blob);
        setRecordedVideoUrl(previewUrl);
        setFormData(prev => ({ ...prev, videoUrl: previewUrl }));
      };

      setMediaRecorder(recorder);
    } catch (cameraError) {
      setError('Camera access was denied or unavailable. Please allow camera and microphone permissions.');
      setCameraReady(false);
    }
  };

  const startRecording = () => {
    if (!mediaRecorder || mediaRecorder.state === 'recording') return;

    mediaRecorder.start();
    setRecording(true);
  };

  const stopRecording = () => {
    if (!mediaRecorder || mediaRecorder.state !== 'recording') return;

    mediaRecorder.stop();
    setRecording(false);
  };

  const retakeVideo = () => {
    if (recordedVideoUrl) {
      URL.revokeObjectURL(recordedVideoUrl);
    }
    setRecordedVideoBlob(null);
    setRecordedVideoUrl('');
    setFormData(prev => ({ ...prev, videoUrl: '' }));
    if (cameraStream) {
      cameraStream.getTracks().forEach(track => track.stop());
    }
    setCameraStream(null);
    setMediaRecorder(null);
    setCameraReady(false);
    setRecording(false);
    openCamera();
  };

  const validateForm = () => {
    const errors = {};
    if (!formData.title.trim()) errors.title = 'Title is required';
    if (!formData.description.trim()) errors.description = 'Description is required';
    if (!formData.location.trim()) errors.location = 'Location is required';
    // Live photo is mandatory (also guarantees GPS is captured); video remains optional.
    if (!formData.imageUrl) errors.imageUrl = 'A live camera photo is required to file a complaint.';
    return errors;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const errors = validateForm();
    if (Object.keys(errors).length > 0) {
      setValidationErrors(errors);
      return;
    }

    setSubmitting(true);
    setError('');
    setMessage('');

    try {
      const payload = { ...formData };

      const response = await api.submitComplaint(payload);
      setMessage(response.message || 'Complaint submitted successfully!');
      setFormData({
        title: '',
        description: '',
        departmentName: 'Sanitation Department',
        location: '',
        complaintType: 'NORMAL',
        imageUrl: '',
        videoUrl: '',
        latitude: '',
        longitude: ''
      });
      if (cameraStream) {
        cameraStream.getTracks().forEach(track => track.stop());
      }
      if (recordedVideoUrl) {
        URL.revokeObjectURL(recordedVideoUrl);
      }
      setCameraStream(null);
      setMediaRecorder(null);
      setRecordedVideoBlob(null);
      setRecordedVideoUrl('');
      setCameraReady(false);
      setRecording(false);
      setTimeout(() => navigate('/citizen/dashboard'), 2000);
    } catch (err) {
      setError(err.message || 'Unable to submit complaint.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="dashboard-page">
      <div className="container" style={{ maxWidth: '900px' }}>
        <div className="dashboard-header">
          <div className="dashboard-welcome">
            <span className="dashboard-role-badge">📋 Citizen Service</span>
            <h1 className="dashboard-title">File a Complaint</h1>
            <p className="dashboard-subtitle">Report civic issues for review by the relevant department.</p>
            <Link to="/citizen/complaints/guidelines" className="guidelines-link">
              📖 View Guidelines
            </Link>
          </div>
        </div>

        <div className="dashboard-card">
          <form onSubmit={handleSubmit} className="auth-form">
            {error && <div className="alert alert-error">{error}</div>}
            {message && <div className="alert alert-success">{message}</div>}

            <div className="form-group">
              <label>Complaint Title *</label>
              <input
                type="text"
                name="title"
                value={formData.title}
                onChange={handleChange}
                placeholder="Brief description of the issue (max 200 chars)"
                maxLength="200"
                className={validationErrors.title ? 'error-input' : ''}
              />
              {validationErrors.title && <span className="error-text">{validationErrors.title}</span>}
              <small className="char-count">{formData.title.length}/200</small>
            </div>

            <div className="form-group">
              <label>Description *</label>
              <textarea
                name="description"
                value={formData.description}
                onChange={handleChange}
                onBlur={handleDescriptionBlur}
                placeholder="Explain what happened, when, and why you believe it's a problem"
                rows={5}
                className={validationErrors.description ? 'error-input' : ''}
              />
              {validationErrors.description && <span className="error-text">{validationErrors.description}</span>}
              {suggestingCategory && <small>🤖 AI is suggesting a department based on your description...</small>}
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
              <div className="form-group">
                <label>Department {suggestingCategory && <span style={{ fontWeight: 400, fontSize: '0.85em' }}>(🤖 AI suggesting...)</span>}</label>
                <select name="departmentName" value={formData.departmentName} onChange={handleChange}>
                  <option value="Sanitation Department">Sanitation Department</option>
                  <option value="Water Supply Department">Water Supply Department</option>
                  <option value="Road Maintenance Department">Road Maintenance Department</option>
                  <option value="Public Health Department">Public Health Department</option>
                  <option value="Electricity Department">Electricity Department</option>
                  <option value="Traffic & Transport Department">Traffic & Transport Department</option>
                  <option value="Parks & Horticulture Department">Parks & Horticulture Department</option>
                  <option value="Public Safety Department">Public Safety Department</option>
                  <option value="Drainage & Sewerage Department">Drainage & Sewerage Department</option>
                  <option value="Street Lighting Department">Street Lighting Department</option>
                  <option value="Building & Construction Department">Building & Construction Department</option>
                  <option value="Animal Control Department">Animal Control Department</option>
                </select>
                <small>Auto-suggested from your description — change it if it's wrong.</small>
              </div>

              <div className="form-group">
                <label>Complaint Type</label>
                <select name="complaintType" value={formData.complaintType} onChange={handleChange}>
                  <option value="NORMAL">Normal</option>
                  <option value="EMERGENCY">⚠️ Emergency</option>
                </select>
                <small>Mark as Emergency only if immediate action is required</small>
              </div>
            </div>

            <div className="form-group">
              <label>Location *</label>
              <input
                type="text"
                name="location"
                value={formData.location}
                onChange={handleChange}
                readOnly={Boolean(formData.latitude && formData.longitude)}
                placeholder="Captured automatically when you take a live evidence photo, or type it manually"
                className={validationErrors.location ? 'error-input' : ''}
              />
              {validationErrors.location && <span className="error-text">{validationErrors.location}</span>}
              <small>Location is captured automatically along with your live evidence photo below.</small>
            </div>

            {(formData.latitude || formData.longitude) && (
              <div style={{
                backgroundColor: '#ecf0f1',
                padding: '12px',
                borderRadius: '5px',
                marginBottom: '15px',
                fontSize: '0.9rem'
              }}>
                📍 <strong>Coordinates:</strong> {formData.latitude}, {formData.longitude}
              </div>
            )}

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
              <div className="form-group geotag-image-field">
                <LivePhotoCapture value={formData} onChange={(evidence) => setFormData((prev) => ({ ...prev, ...evidence }))} required />
                {validationErrors.imageUrl && <span className="error-text">{validationErrors.imageUrl}</span>}
              </div>

              <div className="form-group">
                <LiveEvidenceCapture value={formData} onChange={(evidence) => setFormData((prev) => ({ ...prev, ...evidence }))} includePhoto={false} />
                {validationErrors.videoUrl && <span className="error-text">{validationErrors.videoUrl}</span>}
              </div>
            </div>

            <div style={{ display: 'flex', gap: '10px', marginTop: '25px' }}>
              <button type="submit" className="primary-button" disabled={submitting}>
                {submitting ? '⏳ Submitting...' : '✓ Submit Complaint'}
              </button>
              <button
                type="button"
                onClick={() => navigate('/citizen/dashboard')}
                style={{
                  padding: '10px 20px',
                  backgroundColor: '#95a5a6',
                  color: 'white',
                  border: 'none',
                  borderRadius: '5px',
                  cursor: 'pointer'
                }}
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

