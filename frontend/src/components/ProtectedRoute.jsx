import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';

/**
 * Route protection wrapper based on session authentication and role (CITIZEN / OFFICER).
 */
export default function ProtectedRoute({ session, requiredRole, loading }) {
  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '60vh' }}>
        <div className="spinner spinner-primary" style={{ width: '2rem', height: '2rem' }}></div>
      </div>
    );
  }

  if (!session?.authenticated) {
    return <Navigate to={requiredRole === 'OFFICER' ? '/auth/officer/login' : '/auth/citizen/login'} replace />;
  }

  if (requiredRole && session.userType !== requiredRole) {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
}
