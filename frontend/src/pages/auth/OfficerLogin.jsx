import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../services/api';

export default function OfficerLogin({ setSession }) {
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    employeeId: '',
    password: '',
  });

  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (error) setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!formData.employeeId.trim() || !formData.password) {
      setError('Please enter Employee ID and password');
      return;
    }

    setLoading(true);

    try {
      const response = await api.loginOfficer({
        employeeId: formData.employeeId.trim(),
        password: formData.password,
      });

      if (response.success) {
        setSession({
          authenticated: true,
          userType: 'OFFICER',
          userId: response.data.officerId,
          userName: response.data.fullName,
          details: response.data,
        });

        navigate('/officer/dashboard');
      } else {
        setError('Invalid Employee ID or password');
      }
    } catch (err) {
      setError('Invalid Employee ID or password');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="container" style={{ display: 'flex', justifyContent: 'center' }}>
        <div className="auth-card">
          <div className="auth-header">
            <span className="auth-badge auth-badge-officer">Administrative Access</span>
            <h2 className="auth-title">Officer Login</h2>
            <p className="auth-subtitle">
              Authorized portal for administrative personnel and departmental officers
            </p>
          </div>

          <div className="auth-body">
            {error && (
              <div className="alert alert-error">
                <svg width="18" height="18" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd"/>
                </svg>
                <span>{error}</span>
              </div>
            )}

            <form onSubmit={handleSubmit} noValidate>
              <div className="form-group">
                <label className="form-label" htmlFor="employeeId">
                  Employee ID / Officer Code
                </label>
                <input
                  type="text"
                  id="employeeId"
                  name="employeeId"
                  className="form-control"
                  placeholder="e.g. EMP1001"
                  value={formData.employeeId}
                  onChange={handleChange}
                  required
                  disabled={loading}
                />
                <span className="field-hint">Use administration provisioned ID (e.g. EMP1001)</span>
              </div>

              <div className="form-group">
                <label className="form-label" htmlFor="password">
                  Security Password
                </label>
                <input
                  type="password"
                  id="password"
                  name="password"
                  className="form-control"
                  placeholder="Enter administrative password"
                  value={formData.password}
                  onChange={handleChange}
                  required
                  disabled={loading}
                />
              </div>

              <div style={{ marginTop: '1.5rem' }}>
                <button
                  type="submit"
                  className="btn btn-secondary btn-block btn-lg"
                  disabled={loading}
                >
                  {loading ? (
                    <>
                      <div className="spinner"></div>
                      <span>Authenticating Officer...</span>
                    </>
                  ) : (
                    'Sign In as Officer'
                  )}
                </button>
              </div>
            </form>

            <div className="auth-note-box">
              <strong>Administrative Policy Notice:</strong> Officer accounts are provisioned exclusively by administrative authorities. Self-registration is restricted in accordance with governance security guidelines.
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
