import React from 'react';

export default function OfficerProfile({ session }) {
  const officer = session?.details || {};

  return (
    <>
      <div className="dashboard-header dashboard-header-officer">
        <div className="dashboard-welcome">
          <span className="dashboard-role-badge">Officer Profile</span>
          <h1 className="dashboard-title">My Profile</h1>
          <p className="dashboard-subtitle">Your provisioned administrative account details.</p>
        </div>
      </div>

      <div className="dashboard-card">
        <h2 className="dashboard-card-title">
          <span>Officer Profile</span>
          <span style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--secondary)' }}>
            {officer.employeeId || 'EMP'}
          </span>
        </h2>

        <div className="profile-info-list">
          <div className="profile-info-item">
            <span className="profile-label">Officer Name</span>
            <span className="profile-value">{officer.fullName || session?.userName || '—'}</span>
          </div>

          <div className="profile-info-item">
            <span className="profile-label">Employee ID</span>
            <span className="profile-value">{officer.employeeId || '—'}</span>
          </div>

          <div className="profile-info-item">
            <span className="profile-label">Assigned Department</span>
            <span className="profile-value">{officer.department || '—'}</span>
          </div>

          <div className="profile-info-item">
            <span className="profile-label">Official Email</span>
            <span className="profile-value">{officer.email || '—'}</span>
          </div>

          <div className="profile-info-item">
            <span className="profile-label">Official Contact</span>
            <span className="profile-value">{officer.phoneNumber || '—'}</span>
          </div>

          <div className="profile-info-item">
            <span className="profile-label">Provisioned On</span>
            <span className="profile-value">
              {officer.createdAt ? new Date(officer.createdAt).toLocaleDateString(undefined, { year: 'numeric', month: 'long', day: 'numeric' }) : 'System Default'}
            </span>
          </div>
        </div>
      </div>
    </>
  );
}
