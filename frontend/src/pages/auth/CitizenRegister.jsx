import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../../services/api';

export default function CitizenRegister() {
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    fullName: '',
    aadhaarNumber: '',
    email: '',
    phoneNumber: '',
    password: '',
    confirmPassword: '',
    address: '',
  });

  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [loading, setLoading] = useState(false);

  const validate = () => {
    const newErrors = {};

    // Full Name
    if (!formData.fullName.trim()) {
      newErrors.fullName = 'Full Name is required';
    } else if (formData.fullName.trim().length > 100) {
      newErrors.fullName = 'Full Name cannot exceed 100 characters';
    }

    // Aadhaar Number (12 numeric digits)
    const aadhaarRegex = /^\d{12}$/;
    if (!formData.aadhaarNumber.trim()) {
      newErrors.aadhaarNumber = 'Aadhaar Number is required';
    } else if (!aadhaarRegex.test(formData.aadhaarNumber.trim())) {
      newErrors.aadhaarNumber = 'Aadhaar must be exactly 12 numeric digits';
    }

    // Email
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!formData.email.trim()) {
      newErrors.email = 'Email address is required';
    } else if (!emailRegex.test(formData.email.trim())) {
      newErrors.email = 'Please enter a valid email address';
    } else if (formData.email.trim().length > 100) {
      newErrors.email = 'Email cannot exceed 100 characters';
    }

    // Phone Number (10-digit Indian mobile number)
    const phoneRegex = /^[6-9]\d{9}$/;
    if (!formData.phoneNumber.trim()) {
      newErrors.phoneNumber = 'Phone number is required';
    } else if (!phoneRegex.test(formData.phoneNumber.trim())) {
      newErrors.phoneNumber = 'Enter a valid 10-digit Indian mobile number (e.g. 9876543210)';
    }

    // Password (min 8 characters)
    if (!formData.password) {
      newErrors.password = 'Password is required';
    } else if (formData.password.length < 8) {
      newErrors.password = 'Password must be at least 8 characters long';
    }

    // Confirm Password
    if (!formData.confirmPassword) {
      newErrors.confirmPassword = 'Confirm Password is required';
    } else if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = 'Passwords do not match';
    }

    // Address (optional, max 255 chars)
    if (formData.address && formData.address.length > 255) {
      newErrors.address = 'Address cannot exceed 255 characters';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    // Clear specific error on change
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: '' }));
    }
    if (serverError) setServerError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setServerError('');
    setSuccessMessage('');

    if (!validate()) {
      return;
    }

    setLoading(true);

    try {
      const response = await api.registerCitizen(formData);
      if (response.success) {
        setSuccessMessage('Citizen account registered successfully! Redirecting to login...');
        setTimeout(() => {
          navigate('/auth/citizen/login', {
            state: { message: 'Registration successful! Please login with your credentials.' },
          });
        }, 1800);
      } else {
        setServerError(response.message || 'Registration failed');
      }
    } catch (err) {
      setServerError(err.message || 'Failed to complete registration');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="container" style={{ display: 'flex', justifyContent: 'center' }}>
        <div className="auth-card auth-card-lg">
          <div className="auth-header">
            <span className="auth-badge auth-badge-citizen">Citizen Portal</span>
            <h2 className="auth-title">Citizen Registration</h2>
            <p className="auth-subtitle">
              Create an official citizen account for public administrative assistance
            </p>
          </div>

          <div className="auth-body">
            {successMessage && (
              <div className="alert alert-success">
                <svg width="18" height="18" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd"/>
                </svg>
                <span>{successMessage}</span>
              </div>
            )}

            {serverError && (
              <div className="alert alert-error">
                <svg width="18" height="18" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd"/>
                </svg>
                <span>{serverError}</span>
              </div>
            )}

            <form onSubmit={handleSubmit} noValidate>
              <div className="form-grid">
                {/* Full Name */}
                <div className="form-group form-full">
                  <label className="form-label" htmlFor="fullName">
                    Full Name *
                  </label>
                  <input
                    type="text"
                    id="fullName"
                    name="fullName"
                    className={`form-control ${errors.fullName ? 'is-invalid' : ''}`}
                    placeholder="Enter your legal full name"
                    value={formData.fullName}
                    onChange={handleChange}
                    maxLength={100}
                    disabled={loading}
                  />
                  {errors.fullName && <span className="field-error">{errors.fullName}</span>}
                </div>

                {/* Aadhaar Number */}
                <div className="form-group">
                  <label className="form-label" htmlFor="aadhaarNumber">
                    Aadhaar Number *
                  </label>
                  <input
                    type="text"
                    id="aadhaarNumber"
                    name="aadhaarNumber"
                    className={`form-control ${errors.aadhaarNumber ? 'is-invalid' : ''}`}
                    placeholder="12-digit Aadhaar number"
                    value={formData.aadhaarNumber}
                    onChange={handleChange}
                    maxLength={12}
                    disabled={loading}
                  />
                  {errors.aadhaarNumber && <span className="field-error">{errors.aadhaarNumber}</span>}
                  <span className="field-hint">Exactly 12 numeric digits</span>
                </div>

                {/* Phone Number */}
                <div className="form-group">
                  <label className="form-label" htmlFor="phoneNumber">
                    Phone Number *
                  </label>
                  <input
                    type="tel"
                    id="phoneNumber"
                    name="phoneNumber"
                    className={`form-control ${errors.phoneNumber ? 'is-invalid' : ''}`}
                    placeholder="10-digit Indian Mobile"
                    value={formData.phoneNumber}
                    onChange={handleChange}
                    maxLength={10}
                    disabled={loading}
                  />
                  {errors.phoneNumber && <span className="field-error">{errors.phoneNumber}</span>}
                  <span className="field-hint">Valid Indian number (e.g. 9876543210)</span>
                </div>

                {/* Email Address */}
                <div className="form-group form-full">
                  <label className="form-label" htmlFor="email">
                    Email Address *
                  </label>
                  <input
                    type="email"
                    id="email"
                    name="email"
                    className={`form-control ${errors.email ? 'is-invalid' : ''}`}
                    placeholder="example@domain.com"
                    value={formData.email}
                    onChange={handleChange}
                    maxLength={100}
                    disabled={loading}
                  />
                  {errors.email && <span className="field-error">{errors.email}</span>}
                </div>

                {/* Password */}
                <div className="form-group">
                  <label className="form-label" htmlFor="password">
                    Password *
                  </label>
                  <input
                    type="password"
                    id="password"
                    name="password"
                    className={`form-control ${errors.password ? 'is-invalid' : ''}`}
                    placeholder="Minimum 8 characters"
                    value={formData.password}
                    onChange={handleChange}
                    disabled={loading}
                  />
                  {errors.password && <span className="field-error">{errors.password}</span>}
                </div>

                {/* Confirm Password */}
                <div className="form-group">
                  <label className="form-label" htmlFor="confirmPassword">
                    Confirm Password *
                  </label>
                  <input
                    type="password"
                    id="confirmPassword"
                    name="confirmPassword"
                    className={`form-control ${errors.confirmPassword ? 'is-invalid' : ''}`}
                    placeholder="Re-enter password"
                    value={formData.confirmPassword}
                    onChange={handleChange}
                    disabled={loading}
                  />
                  {errors.confirmPassword && <span className="field-error">{errors.confirmPassword}</span>}
                </div>

                {/* Address (Optional) */}
                <div className="form-group form-full">
                  <label className="form-label" htmlFor="address">
                    <span>Residential Address</span>
                    <span className="form-label-optional">Optional</span>
                  </label>
                  <textarea
                    id="address"
                    name="address"
                    rows="3"
                    className={`form-control ${errors.address ? 'is-invalid' : ''}`}
                    placeholder="Enter residential address (maximum 255 characters)"
                    value={formData.address}
                    onChange={handleChange}
                    maxLength={255}
                    disabled={loading}
                  ></textarea>
                  {errors.address && <span className="field-error">{errors.address}</span>}
                </div>
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
                      <span>Registering Citizen Profile...</span>
                    </>
                  ) : (
                    'Complete Registration'
                  )}
                </button>
              </div>
            </form>

            <div className="auth-footer">
              Already registered? <Link to="/auth/citizen/login">Sign in to Citizen Portal</Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
