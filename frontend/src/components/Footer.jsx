import React from 'react';
import { Link } from 'react-router-dom';

export default function Footer() {
  return (
    <footer className="site-footer">
      <div className="container footer-top">
        <div className="footer-brand">
          <h3 className="footer-title">
            Smart Governance Platform for Administrative Operations with Citizen Service Assistance
          </h3>
          <p className="footer-desc">
            A unified digital governance ecosystem engineered for administrative efficiency, citizen engagement, and transparent public service operations.
          </p>
        </div>

        <div>
          <h4 className="footer-heading">Citizen Services</h4>
          <ul className="footer-links">
            <li><Link to="/auth/citizen/register">New Citizen Registration</Link></li>
            <li><Link to="/auth/citizen/login">Citizen Login Portal</Link></li>
            <li><Link to="/">Platform Overview</Link></li>
          </ul>
        </div>

        <div>
          <h4 className="footer-heading">Administration</h4>
          <ul className="footer-links">
            <li><Link to="/auth/officer/login">Officer Access Portal</Link></li>
            <li><Link to="/">Administrative Hierarchy</Link></li>
            <li><Link to="/">Security Compliance</Link></li>
          </ul>
        </div>

        <div>
          <h4 className="footer-heading">Security & Trust</h4>
          <div className="trust-badge-card">
            <div className="trust-badge-item">
              <svg width="16" height="16" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M10 1.944A11.954 11.954 0 012.166 5C2.056 5.649 2 6.319 2 7c0 5.225 3.34 9.67 8 11.317C14.66 16.67 18 12.225 18 7c0-.682-.057-1.35-.166-2.001A11.954 11.954 0 0110 1.944zM11 14a1 1 0 11-2 0 1 1 0 012 0zm0-7a1 1 0 10-2 0v3a1 1 0 102 0V7z" clipRule="evenodd"/>
              </svg>
              <span>BCrypt Salted Password Hashing</span>
            </div>
            <div className="trust-badge-item">
              <svg width="16" height="16" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M10 1.944A11.954 11.954 0 012.166 5C2.056 5.649 2 6.319 2 7c0 5.225 3.34 9.67 8 11.317C14.66 16.67 18 12.225 18 7c0-.682-.057-1.35-.166-2.001A11.954 11.954 0 0110 1.944zM11 14a1 1 0 11-2 0 1 1 0 012 0zm0-7a1 1 0 10-2 0v3a1 1 0 102 0V7z" clipRule="evenodd"/>
              </svg>
              <span>Secure HTTP Session Isolation</span>
            </div>
            <div className="trust-badge-item">
              <svg width="16" height="16" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd"/>
              </svg>
              <span>Aadhaar & Employee ID Validation</span>
            </div>
          </div>
        </div>
      </div>

      <div className="container footer-bottom">
        <div>
          © {new Date().getFullYear()} Smart Governance Platform for Administrative Operations with Citizen Service Assistance. All rights reserved.
        </div>
        <div>
          Designed for Institutional Transparency and Citizen Welfare
        </div>
      </div>
    </footer>
  );
}
