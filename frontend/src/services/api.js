/**
 * Central API Service for Smart Governance Platform.
 * Single source of truth for backend communication.
 * Base URL: http://localhost:8090/api
 */

const BASE_URL = import.meta.env.DEV ? '/api' : 'http://localhost:8090/api';

/**
 * Standard fetch helper configured with session credentials and JSON headers.
 */
async function request(endpoint, options = {}) {
  const url = `${BASE_URL}${endpoint}`;
  const isFormData = options.body instanceof FormData;

  const headers = {
    'Accept': 'application/json',
    ...options.headers,
  };

  if (!isFormData) {
    headers['Content-Type'] = 'application/json';
  }

  const config = {
    ...options,
    headers,
    credentials: 'include', // Ensure HTTP Session cookies are transmitted
  };

  try {
    const response = await fetch(url, config);
    const data = await response.json().catch(() => ({
      success: false,
      message: 'Failed to parse response from server'
    }));

    if (!response.ok) {
      throw new Error(data.message || `Request failed with status ${response.status}`);
    }

    return data;
  } catch (error) {
    if (error.name === 'TypeError' && error.message.includes('fetch')) {
      throw new Error('Unable to connect to backend server. Please verify the Spring Boot service is running on port 8090.');
    }
    throw error;
  }
}

export const api = {
  /**
   * Register a new citizen
   * @param {Object} citizenData
   */
  registerCitizen: (citizenData) => {
    return request('/auth/citizen/register', {
      method: 'POST',
      body: JSON.stringify(citizenData),
    });
  },

  /**
   * Login citizen with email and password
   * @param {Object} credentials { email, password }
   */
  loginCitizen: (credentials) => {
    return request('/auth/citizen/login', {
      method: 'POST',
      body: JSON.stringify(credentials),
    });
  },

  /**
   * Login administrative officer with Employee ID and password
   * @param {Object} credentials { employeeId, password }
   */
  loginOfficer: (credentials) => {
    return request('/auth/officer/login', {
      method: 'POST',
      body: JSON.stringify(credentials),
    });
  },

  /**
   * Terminate current session
   */
  logout: () => {
    return request('/auth/logout', {
      method: 'POST',
    });
  },

  /**
   * Retrieve active session state
   */
  getSession: () => {
    return request('/session', {
      method: 'GET',
    });
  },

  /**
   * Submit a complaint for the logged-in citizen
   */
  submitComplaint: (complaintData, videoBlob = null) => {
    if (videoBlob) {
      const formData = new FormData();
      formData.append('complaint', new Blob([JSON.stringify(complaintData)], {
        type: 'application/json',
      }));
      formData.append('video', videoBlob, 'complaint-video.webm');

      return request('/citizen/complaints', {
        method: 'POST',
        body: formData,
      });
    }

    return request('/citizen/complaints', {
      method: 'POST',
      body: JSON.stringify(complaintData),
    });
  },

  /**
   * Fetch complaints for the logged-in citizen
   */
  getCitizenComplaints: () => {
    return request('/citizen/complaints', {
      method: 'GET',
    });
  },

  /**
   * Fetch all complaints for the officer queue
   */
  getAllComplaints: () => {
    return request('/officer/complaints', {
      method: 'GET',
    });
  },

  /**
   * Update complaint status as officer
   */
  updateComplaintStatus: (complaintId, payload) => {
    return request(`/officer/complaints/${complaintId}/status`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    });
  },

  /**
   * Get detailed complaint info
   */
  getComplaintById: (complaintId) => {
    return request(`/complaints/${complaintId}`, {
      method: 'GET',
    });
  },

  /**
   * Get complaint history/audit trail
   */
  getComplaintHistory: (complaintId) => {
    return request(`/complaints/${complaintId}/history`, {
      method: 'GET',
    });
  },

  /**
   * Submit feedback/rating for a resolved complaint
   */
  submitFeedback: (complaintId, feedbackData) => {
    return request(`/complaints/${complaintId}/feedback`, {
      method: 'POST',
      body: JSON.stringify(feedbackData),
    });
  },

  /**
   * Request to reopen a resolved complaint
   */
  requestReopen: (complaintId, reopenData) => {
    return request(`/complaints/${complaintId}/reopen`, {
      method: 'POST',
      body: JSON.stringify(reopenData),
    });
  },

  /**
   * Set duplicate status of a complaint
   */
  setDuplicateStatus: (complaintId, payload) => {
    return request(`/officer/complaints/${complaintId}/duplicate-status`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    });
  },

  /**
   * Escalate a complaint to higher level (officer)
   */
  escalateComplaint: (complaintId, escalationData) => {
    return request(`/officer/complaints/${complaintId}/escalate`, {
      method: 'POST',
      body: JSON.stringify(escalationData),
    });
  },

  /**
   * Escalate a complaint to higher level (citizen)
   */
  escalateCitizenComplaint: (complaintId, escalationData) => {
    return request(`/citizen/complaints/${complaintId}/escalate`, {
      method: 'POST',
      body: JSON.stringify(escalationData),
    });
  },

  /**
   * Get emergency priority complaints
   */
  getEmergencyComplaints: () => {
    return request('/officer/complaints/emergency', {
      method: 'GET',
    });
  },

  /**
   * Get reopened complaints
   */
  getReopenedComplaints: () => {
    return request('/officer/complaints/reopened', {
      method: 'GET',
    });
  },

  /**
   * Check backend health status
   */
  checkHealth: () => {
    return request('/auth/health', {
      method: 'GET',
    });
  },

  /**
   * AI-suggested department + priority based on the complaint description.
   * Used to auto-fill the department dropdown while the citizen is still typing.
   */
  suggestComplaintCategory: (description) => {
    return request('/citizen/complaints/suggest-category', {
      method: 'POST',
      body: JSON.stringify({ description }),
    });
  },

  // ---------------------------------------------------------------------
  // Officer Module — complaints visible to the logged-in officer's department,
  // including newly registered/unassigned citizen complaints.
  // ---------------------------------------------------------------------

  /**
   * Get the logged-in officer's dashboard: profile + live stats + recent complaints
   */
  getOfficerDashboard: () => {
    return request('/officer/dashboard', {
      method: 'GET',
    });
  },

  /**
   * Get complaints visible to the logged-in officer's department.
   * This includes newly registered and currently unassigned complaints.
   */
  getMyComplaints: () => {
    return request('/officer/my-complaints', {
      method: 'GET',
    });
  },

  /**
   * Get full detail of one complaint assigned to the logged-in officer
   */
  getMyComplaintById: (complaintId) => {
    return request(`/officer/my-complaints/${complaintId}`, {
      method: 'GET',
    });
  },

  /**
   * Update status/remarks of a complaint assigned to the logged-in officer
   */
  updateMyComplaintStatus: (complaintId, payload) => {
    return request(`/officer/my-complaints/${complaintId}/status`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    });
  },

  /**
   * Get the audit/history timeline of a complaint assigned to the logged-in officer
   */
  getMyComplaintHistory: (complaintId) => {
    return request(`/officer/my-complaints/${complaintId}/history`, {
      method: 'GET',
    });
  },

  getOfficerDuplicates: () => request('/officer/duplicates', { method: 'GET' }),

  getOfficerDuplicateReview: (complaintId) => request(`/officer/duplicates/${complaintId}`, { method: 'GET' }),

  verifyOfficerDuplicate: (complaintId, payload) => request(`/officer/duplicates/${complaintId}/verify`, { method: 'PUT', body: JSON.stringify(payload) }),

  getOfficerEscalated: () => request('/officer/escalated', { method: 'GET' }),

  escalateOfficerComplaint: (complaintId, payload) => request(`/officer/my-complaints/${complaintId}/escalate`, { method: 'POST', body: JSON.stringify(payload) }),

  /**
   * Update escalation status and remarks (Officer)
   */
  updateEscalationStatus: (escalationId, payload) => request(`/officer/escalations/${escalationId}/status`, { method: 'PUT', body: JSON.stringify(payload) }),

  // ---------------------------------------------------------------------
  // Notification Module (Citizen)
  // ---------------------------------------------------------------------
  /**
   * Fetch all notifications for the logged-in citizen
   * @param {boolean} [unreadOnly=false]
   */
  getNotifications: (unreadOnly = false) => {
    const query = unreadOnly ? '?unreadOnly=true' : '';
    return request(`/notifications${query}`, { method: 'GET' });
  },

  /**
   * Get unread notification count
   */
  getUnreadNotificationCount: () => request('/notifications/unread-count', { method: 'GET' }),

  /**
   * Mark a single notification as read
   */
  markNotificationAsRead: (notificationId) => request(`/notifications/${notificationId}/read`, { method: 'PUT' }),

  /**
   * Mark all notifications as read
   */
  markAllNotificationsAsRead: () => request('/notifications/read-all', { method: 'PUT' }),

  /**
   * Delete a notification
   */
  deleteNotification: (notificationId) => request(`/notifications/${notificationId}`, { method: 'DELETE' }),
};

export default api;
