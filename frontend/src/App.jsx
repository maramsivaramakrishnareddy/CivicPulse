import React, { useState, useEffect } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import api from './services/api';
import Navbar from './components/Navbar';
import Footer from './components/Footer';
import ProtectedRoute from './components/ProtectedRoute';

import HomePage from './pages/HomePage';
import CitizenRegister from './pages/auth/CitizenRegister';
import CitizenLogin from './pages/auth/CitizenLogin';
import OfficerLogin from './pages/auth/OfficerLogin';
import CitizenDashboard from './pages/citizen/CitizenDashboard';
import ComplaintForm from './pages/citizen/ComplaintForm';
import ComplaintGuidelines from './pages/citizen/ComplaintGuidelines';
import ComplaintDetail from './pages/citizen/ComplaintDetail';
import FeedbackForm from './pages/citizen/FeedbackForm';
import ReopenForm from './pages/citizen/ReopenForm';
import NotificationCenter from './pages/citizen/NotificationCenter';
import NotificationPreferences from './pages/citizen/NotificationPreferences';
import OfficerDashboard from './pages/officer/OfficerDashboard';
import ComplaintQueue from './pages/officer/ComplaintQueue';
import OfficerProfile from './pages/officer/OfficerProfile';
import ComplaintReview from './pages/officer/ComplaintReview';
import DuplicateComplaints from './pages/officer/DuplicateComplaints';
import DuplicateReview from './pages/officer/DuplicateReview';
import EscalatedComplaints from './pages/officer/EscalatedComplaints';
import OfficerLayout from './pages/officer/OfficerLayout';
import OfficerComplaintsMap from './pages/officer/OfficerComplaintsMap';

export default function App() {
  const [session, setSession] = useState({
    authenticated: false,
    userType: null,
    userId: null,
    userName: null,
    details: null,
  });
  const [loading, setLoading] = useState(true);

  // Check active session on initial app render
  useEffect(() => {
    const checkSession = async () => {
      try {
        const response = await api.getSession();
        if (response.success && response.data?.authenticated) {
          setSession(response.data);
        } else {
          setSession({ authenticated: false, userType: null, userId: null, userName: null, details: null });
        }
      } catch (err) {
        // Backend not reachable or unauthenticated
        setSession({ authenticated: false, userType: null, userId: null, userName: null, details: null });
      } finally {
        setLoading(false);
      }
    };

    checkSession();
  }, []);

  return (
    <>
      <Navbar session={session} setSession={setSession} />
      
      <main className="main-content">
        <Routes>
          {/* Public Routes */}
          <Route path="/" element={<HomePage session={session} />} />
          
          <Route 
            path="/auth/citizen/register" 
            element={
              session.authenticated ? (
                <Navigate to={session.userType === 'CITIZEN' ? '/citizen/dashboard' : '/officer/dashboard'} replace />
              ) : (
                <CitizenRegister />
              )
            } 
          />

          <Route 
            path="/auth/citizen/login" 
            element={
              session.authenticated ? (
                <Navigate to={session.userType === 'CITIZEN' ? '/citizen/dashboard' : '/officer/dashboard'} replace />
              ) : (
                <CitizenLogin setSession={setSession} />
              )
            } 
          />

          <Route 
            path="/auth/officer/login" 
            element={
              session.authenticated ? (
                <Navigate to={session.userType === 'CITIZEN' ? '/citizen/dashboard' : '/officer/dashboard'} replace />
              ) : (
                <OfficerLogin setSession={setSession} />
              )
            } 
          />

          {/* Protected Citizen Dashboard */}
          <Route element={<ProtectedRoute session={session} requiredRole="CITIZEN" loading={loading} />}>
            <Route path="/citizen/dashboard" element={<CitizenDashboard session={session} />} />
            <Route path="/citizen/complaints/new" element={<ComplaintForm session={session} />} />
            <Route path="/citizen/complaints/guidelines" element={<ComplaintGuidelines />} />
            <Route path="/citizen/complaints/:complaintId" element={<ComplaintDetail session={session} />} />
            <Route path="/citizen/complaint/:complaintId" element={<ComplaintDetail session={session} />} />
            <Route path="/citizen/complaints/:complaintId/feedback" element={<FeedbackForm />} />
            <Route path="/citizen/complaint/:complaintId/feedback" element={<FeedbackForm />} />
            <Route path="/citizen/complaints/:complaintId/reopen" element={<ReopenForm />} />
            <Route path="/citizen/complaint/:complaintId/reopen" element={<ReopenForm />} />
            <Route path="/citizen/notifications" element={<NotificationCenter session={session} />} />
            <Route path="/citizen/notifications/preferences" element={<NotificationPreferences session={session} />} />
          </Route>

          {/* Protected Officer Dashboard */}
          <Route element={<ProtectedRoute session={session} requiredRole="OFFICER" loading={loading} />}>
            <Route element={<OfficerLayout session={session} setSession={setSession} />}>
              <Route path="/officer/dashboard" element={<OfficerDashboard session={session} />} />
              <Route path="/officer/profile" element={<OfficerProfile session={session} />} />
              <Route path="/officer/complaints" element={<ComplaintQueue session={session} />} />
              <Route path="/officer/complaints-map" element={<OfficerComplaintsMap />} />
              <Route path="/officer/complaints/:complaintId" element={<ComplaintReview session={session} />} />
              <Route path="/officer/duplicates" element={<DuplicateComplaints session={session} />} />
              <Route path="/officer/duplicates/:complaintId" element={<DuplicateReview session={session} />} />
              <Route path="/officer/escalated" element={<EscalatedComplaints session={session} />} />
            </Route>
          </Route>

          {/* Catch-all fallback */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>

      <Footer />
    </>
  );
}
