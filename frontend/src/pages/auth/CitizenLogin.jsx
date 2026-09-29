import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import api from '../../services/api';

export default function CitizenLogin({ setSession }) {
  const navigate = useNavigate();
  const location = useLocation();

  const [formData, setFormData] = useState({
    email: '',
    password: '',
  });

  const [error, setError] = useState('');
  const [success, setSuccess] = useState(location.state?.message || '');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (error) setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!formData.email.trim() || !formData.password) {
      setError('Please enter both email and password');
      return;
    }

    setLoading(true);

    try {
      // Clear any previous citizen/officer session before creating a new citizen session
try {
  await api.logout();
} catch (logoutError) {
  // Ignore logout errors because there may be no existing session
}

const response = await api.loginCitizen({
        email: formData.email.trim(),
        password: formData.password,
      });

      if (response.success) {
        // Update session state
        setSession({
          authenticated: true,
          userType: 'CITIZEN',
          userId: response.data.citizenId,
          userName: response.data.fullName,
          details: response.data,
        });

        navigate('/citizen/dashboard');
      } else {
        setError('Invalid email or password');
      }
    } catch (err) {
      // In accordance with security specification, do not reveal if email exists
      setError('Invalid email or password');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="container" style={{ display: 'flex', justifyContent: 'center' }}>
        <div className="auth-card">
          <div className="auth-header">
            <span className="auth-badge auth-badge-citizen">Citizen Service Portal</span>
            <h2 className="auth-title">Citizen Login</h2>
            <p className="auth-subtitle">
              Sign in to access your citizen service assistance portal
            </p>
          </div>

          <div className="auth-body">
            {success && (
              <div className="alert alert-success">
                <svg width="18" height="18" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd"/>
                </svg>
                <span>{success}</span>
              </div>
            )}

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
                <label className="form-label" htmlFor="email">
                  Registered Email Address
                </label>
                <input
                  type="email"
                  id="email"
                  name="email"
                  className="form-control"
                  placeholder="Enter your registered email"
                  value={formData.email}
                  onChange={handleChange}
                  required
                  disabled={loading}
                />
              </div>

              <div className="form-group">
                <label className="form-label" htmlFor="password">
                  Password
                </label>
                <input
                  type="password"
                  id="password"
                  name="password"
                  className="form-control"
                  placeholder="Enter your password"
                  value={formData.password}
                  onChange={handleChange}
                  required
                  disabled={loading}
                />
              </div>

              <div style={{ marginTop: '1.5rem' }}>
                <button
                  type="submit"
                  className="btn btn-primary btn-block btn-lg"
                  disabled={loading}
                >
                  {loading ? (
                    <>
                      <div className="spinner"></div>
                      <span>Verifying Credentials...</span>
                    </>
                  ) : (
                    'Sign In as Citizen'
                  )}
                </button>
              </div>
            </form>

            <div className="auth-footer">
              New citizen? <Link to="/auth/citizen/register">Register for an account</Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
