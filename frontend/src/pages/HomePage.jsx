import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';

export default function HomePage({ session }) {
  const [serviceStatus, setServiceStatus] = useState('checking');

  useEffect(() => {
    let active = true;
    api.checkHealth()
      .then((response) => {
        if (active) setServiceStatus(response.success ? 'online' : 'offline');
      })
      .catch(() => {
        if (active) setServiceStatus('offline');
      });

    return () => { active = false; };
  }, []);

  const serviceStatusLabel = {
    checking: 'Checking platform services',
    online: 'Platform services operational',
    offline: 'Platform services unavailable',
  }[serviceStatus];

  return (
    <div className="home-page">
      {/* Hero Section */}
      <section className="hero-section">
        <div className="container">
          <div className="hero-pill">
            <svg width="14" height="14" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M10 1.944A11.954 11.954 0 012.166 5C2.056 5.649 2 6.319 2 7c0 5.225 3.34 9.67 8 11.317C14.66 16.67 18 12.225 18 7c0-.682-.057-1.35-.166-2.001A11.954 11.954 0 0110 1.944zM11 14a1 1 0 11-2 0 1 1 0 012 0zm0-7a1 1 0 10-2 0v3a1 1 0 102 0V7z" clipRule="evenodd"/>
            </svg>
            <span>Centralized Digital Administration</span>
          </div>

          <h1 className="hero-title">
            Smart Governance Platform for Administrative Operations with Citizen Service Assistance
          </h1>

          <p className="hero-description">
            The platform provides citizen service assistance and supports administrative operations through a centralized digital platform. Designed for enhanced transparency, robust identity verification, and efficient governance workflows.
          </p>

          {session?.authenticated ? (
            <div className="hero-cta-group">
              {session.userType === 'CITIZEN' ? (
                <Link to="/citizen/dashboard" className="btn btn-primary btn-lg">
                  Go to Citizen Dashboard →
                </Link>
              ) : (
                <Link to="/officer/dashboard" className="btn btn-secondary btn-lg">
                  Go to Officer Dashboard →
                </Link>
              )}
            </div>
          ) : (
            <>
              <div className="hero-cta-group">
                <Link to="/auth/citizen/register" className="btn btn-primary btn-lg">
                  Register as Citizen
                </Link>
                <Link to="/auth/citizen/login" className="btn btn-outline btn-lg">
                  Citizen Sign In
                </Link>
              </div>

              <div className="hero-officer-box">
                <span>Administrative Personnel:</span>
                <Link to="/auth/officer/login" className="btn btn-secondary btn-sm">
                  Authorized Officer Portal Access
                </Link>
              </div>
            </>
          )}

          <div className={`platform-status platform-status-${serviceStatus}`} role="status">
            <span className="platform-status-dot" aria-hidden="true" />
            <span>{serviceStatusLabel}</span>
            <span className="platform-status-divider" aria-hidden="true" />
            <span>Identity, complaints, and tracking</span>
          </div>
        </div>
      </section>

      <section className="quick-services-section">
        <div className="container">
          <div className="quick-services-heading">
            <div>
              <span className="section-tag">Start here</span>
              <h2 className="section-title">Your next civic action</h2>
            </div>
            <span className="quick-services-note">Simple steps. Visible progress.</span>
          </div>

          <div className="quick-services-grid">
            <Link to="/auth/citizen/register" className="quick-service-card quick-service-citizen">
              <span className="quick-service-number">01</span>
              <span className="quick-service-icon">+</span>
              <span className="quick-service-title">Create a citizen account</span>
              <span className="quick-service-text">Verify your identity and access complaint tracking.</span>
              <span className="quick-service-link">Register now <span aria-hidden="true">&#8594;</span></span>
            </Link>
            <Link to="/auth/citizen/login" className="quick-service-card quick-service-track">
              <span className="quick-service-number">02</span>
              <span className="quick-service-icon">&#9673;</span>
              <span className="quick-service-title">Track a civic complaint</span>
              <span className="quick-service-text">See updates, officer action, and your complaint timeline.</span>
              <span className="quick-service-link">Open citizen portal <span aria-hidden="true">&#8594;</span></span>
            </Link>
            <Link to="/auth/officer/login" className="quick-service-card quick-service-officer">
              <span className="quick-service-number">03</span>
              <span className="quick-service-icon">&#10003;</span>
              <span className="quick-service-title">Administrative access</span>
              <span className="quick-service-text">Manage department work with accountable status updates.</span>
              <span className="quick-service-link">Officer sign in <span aria-hidden="true">&#8594;</span></span>
            </Link>
          </div>
        </div>
      </section>

      {/* Introduction & Highlights Section */}
      <section className="intro-section">
        <div className="container">
          <div className="section-header">
            <span className="section-tag">Platform Foundation</span>
            <h2 className="section-title">Integrated Administrative & Citizen Services</h2>
            <p className="section-desc">
              Built on modern enterprise standards to deliver secure identity management, structured administrative hierarchies, and accessible public service touchpoints.
            </p>
          </div>

          <div className="features-grid">
            {/* Feature 1 */}
            <div className="feature-card">
              <div className="feature-icon-wrapper">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                  <circle cx="8.5" cy="7.5" r="4"></circle>
                  <line x1="20" y1="8" x2="20" y2="14"></line>
                  <line x1="23" y1="11" x2="17" y2="11"></line>
                </svg>
              </div>
              <h3 className="feature-title">Verified Citizen Enrollment</h3>
              <p className="feature-text">
                Streamlined registration with mandatory 12-digit Aadhaar verification, phone validation, and strong credential protection for authentic citizen accounts.
              </p>
            </div>

            {/* Feature 2 */}
            <div className="feature-card">
              <div className="feature-icon-wrapper">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect>
                  <line x1="8" y1="21" x2="16" y2="21"></line>
                  <line x1="12" y1="17" x2="12" y2="21"></line>
                </svg>
              </div>
              <h3 className="feature-title">Administrative Officer Portal</h3>
              <p className="feature-text">
                Secure portal for department officials authenticated with verified Employee IDs, enabling streamlined operational workflows and administrative governance.
              </p>
            </div>

            {/* Feature 3 */}
            <div className="feature-card">
              <div className="feature-icon-wrapper">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                  <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
                </svg>
              </div>
              <h3 className="feature-title">Enterprise Cryptographic Security</h3>
              <p className="feature-text">
                Industry-standard BCrypt salted password hashing, isolated HTTP session management, and server-side input validation to safeguard user credentials.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Security and Trust Banner */}
      <section className="trust-section">
        <div className="container">
          <div className="trust-banner">
            <div className="trust-banner-content">
              <h2 className="trust-banner-title">Security & Governance Compliance</h2>
              <p className="trust-banner-text">
                The platform adheres to strict data integrity standards, ensuring distinct session lifecycles for citizens and administrative personnel without third-party tracking.
              </p>
            </div>

            <div className="trust-banner-badges">
              <div className="trust-pill">
                <svg width="18" height="18" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd"/>
                </svg>
                <span>BCrypt Encrypted Storage</span>
              </div>
              <div className="trust-pill">
                <svg width="18" height="18" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd"/>
                </svg>
                <span>HttpOnly Isolated Sessions</span>
              </div>
              <div className="trust-pill">
                <svg width="18" height="18" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd"/>
                </svg>
                <span>Aadhaar Identity Checks</span>
              </div>
              <div className="trust-pill">
                <svg width="18" height="18" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd"/>
                </svg>
                <span>Department Isolation</span>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
