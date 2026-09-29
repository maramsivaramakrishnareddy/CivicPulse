# CivicPulse — Smart Governance Management Platform

A production-grade, full-stack digital governance platform designed to bridge citizens and municipal administration with AI-driven triage, tamper-proof live geotagged evidence capture, transparent complaint lifecycles, structured citizen feedback, multi-tier escalation management, and real-time lifecycle notifications.

Built with **React 18 + Vite**, **Java 17 + Spring Boot 3.2.3**, **Spring Data JPA**, **MySQL 8.x**, and powered by the **Google Gemini Generative AI API**.

---

## 1. Project Title
**CivicPulse — Smart Governance Management Platform for Administrative Operations & Citizen Service Assistance**

---

## 2. Project Overview & Problem Statement

### The Problem
Traditional municipal grievance systems suffer from critical bottlenecks:
- **Misclassification**: Citizens file complaints under incorrect departments, delaying response by days or weeks.
- **Fake or Recycled Evidence**: Complaints frequently include stock photos, internet downloads, or outdated images that municipal teams cannot verify.
- **Lack of Transparency**: Citizens have zero visibility into complaint progress after submission.
- **Unresolved Disputes & Dead Ends**: When a resolution fails in the field, citizens have no formal escalation path other than creating duplicate grievances.
- **Absence of Accountability**: No mechanism exists for citizens to rate resolution quality or provide verified proof that work was or was not completed satisfactorily.

### The CivicPulse Solution
CivicPulse introduces an end-to-end municipal workflow:
1. **AI-Driven Category & Priority Triage**: Leverages Google Gemini AI to analyze natural language descriptions in real time, automatically recommending the responsible department and urgency priority.
2. **Mandatory Live Geotagged Camera Capture**: Enforces live camera capture with hardware GPS coordinate embedding directly on the media canvas. Gallery uploads are blocked to guarantee physical authenticity.
3. **Dedicated Officer Portal**: Departmental officers manage assigned queues with geospatial map views, duplicate detection inspection, and verified status transitions.
4. **Structured Citizen Feedback**: Once a complaint is marked `COMPLAINT_RESOLVED`, citizens submit a 1-5 star rating, detailed remarks, and optional live verification media.
5. **Multi-Level Escalation Management**: High-priority or stalled complaints can be escalated to higher authorities (e.g., Chief Engineer, Municipal Commissioner) with audit tracking and resolution sign-offs.
6. **Unified Notification Hub**: Real-time event notifications generated across the entire complaint lifecycle, with live unread badge counts, filtering, and bulk read actions.

---

## 3. Technology Stack

### Frontend
- **Framework**: React 18.2
- **Language**: JavaScript (ES6+ / JSX)
- **Routing**: React Router DOM (v6)
- **Build Tool & Dev Server**: Vite 5.4
- **Styling**: Pure CSS with Custom Property Tokens (`#1e3a8a`, `#059669`, `#dc2626`, `#f59e0b`, `#2563eb`)
- **APIs & Networking**: Native `fetch` with `credentials: 'include'` for secure session cookies
- **Hardware Integration**: HTML5 MediaDevices API (`getUserMedia`), Canvas 2D overlay rendering, and W3C Geolocation API

### Backend
- **Language**: Java 17 (LTS)
- **Framework**: Spring Boot 3.2.3
- **Web Layer**: Spring MVC RESTful Web Services
- **Data Persistence**: Spring Data JPA / Hibernate 6.4
- **Database Engine**: MySQL 8.x
- **Security & Cryptography**: Spring Security Crypto (`BCryptPasswordEncoder`, strength 10)
- **Validation**: Jakarta Bean Validation (`jakarta.validation-api` + Hibernate Validator)
- **AI Integration**: Google Gemini 1.5 Flash API via Java REST client
- **Build System**: Apache Maven 3.8+

---

## 4. Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           REACT FRONTEND (Vite @ 5173)                          │
│                                                                                 │
│   Citizen Experience:                        Officer Administration:             │
│   • Guidelines & Auth Portal                 • Departmental Queue & Dashboard   │
│   • Live Photo + GPS Capture                 • Geospatial Map View              │
│   • Category Suggestion                      • Status Progression (Action/Done) │
│   • Complaint Tracking & Detail              • Duplicate Review & Merge         │
│   • Notification Center & Bell               • Escalation Management & Sign-off │
│   • Resolution Feedback Form                 • Citizen Feedback Inspection      │
└────────────────────────────────────────┬────────────────────────────────────────┘
                                         │ JSON REST APIs (Session Cookie)
                                         ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                     SPRING BOOT BACKEND (Port 8080)                            │
│                                                                                 │
│   [Controllers]                                                                 │
│   • AuthController        • ComplaintController    • OfficerController          │
│   • SessionController     • NotificationController • GeminiController           │
│                                                                                 │
│   [Business Services Layer]                                                     │
│   • ComplaintService      • OfficerService         • NotificationService        │
│   • AuthService           • GeminiService                                       │
│                                                                                 │
│   [Data Layer / Repositories]                                                   │
│   • CitizenRepository     • OfficerRepository      • DepartmentRepository       │
│   • ComplaintRepository   • ComplaintHistoryRepo   • FeedbackRepository         │
│   • EscalationRepository  • NotificationRepository                              │
└───────────────────────┬─────────────────────────────────┬───────────────────────┘
                        │                                 │
                        ▼                                 ▼
   ┌────────────────────────────────────────┐ ┌───────────────────────────────────┐
   │          MySQL DATABASE (3306)         │ │         GOOGLE GEMINI AI        │
   │                                        │ │                                   │
   │   • citizens        • officers         │ │   • gemini-1.5-flash              │
   │   • departments     • complaints       │ │   • Real-time classification      │
   │   • complaint_history                  │ │   • Urgency & priority scoring    │
   │   • feedback        • escalation       │ │   • Evidence mismatch validation  │
   │   • notifications                      │ └───────────────────────────────────┘
   └────────────────────────────────────────┘
```

---

## 5. Complete Folder Structure

```
CivicPulse_Modified_Working_Project/
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/smartgovernance/
│       │   │   ├── SmartGovernanceApplication.java
│       │   │   ├── config/
│       │   │   │   ├── CorsConfig.java
│       │   │   │   ├── DataInitializer.java
│       │   │   │   ├── DepartmentDataInitializer.java
│       │   │   │   ├── GlobalExceptionHandler.java
│       │   │   │   ├── PasswordConfig.java
│       │   │   │   └── SchemaMigration.java
│       │   │   ├── controller/
│       │   │   │   ├── AuthController.java
│       │   │   │   ├── ComplaintController.java
│       │   │   │   ├── NotificationController.java
│       │   │   │   ├── OfficerController.java
│       │   │   │   └── SessionController.java
│       │   │   ├── dto/
│       │   │   │   ├── ApiResponse.java
│       │   │   │   ├── CitizenRegisterRequest.java
│       │   │   │   ├── ComplaintCreateRequest.java
│       │   │   │   ├── ComplaintDuplicateStatusRequest.java
│       │   │   │   ├── ComplaintResponseDto.java
│       │   │   │   ├── ComplaintStatusUpdateRequest.java
│       │   │   │   ├── EscalationRequest.java
│       │   │   │   ├── EscalationStatusUpdateRequest.java
│       │   │   │   ├── FeedbackDto.java
│       │   │   │   ├── FeedbackRequest.java
│       │   │   │   ├── LoginRequest.java
│       │   │   │   ├── NotificationDto.java
│       │   │   │   ├── OfficerComplaintDetailDto.java
│       │   │   │   ├── OfficerComplaintDto.java
│       │   │   │   ├── OfficerDashboardStatsDto.java
│       │   │   │   ├── OfficerLoginRequest.java
│       │   │   │   ├── ReopenRequestDto.java
│       │   │   │   └── SessionResponse.java
│       │   │   ├── entity/
│       │   │   │   ├── Citizen.java
│       │   │   │   ├── Complaint.java
│       │   │   │   ├── ComplaintHistory.java
│       │   │   │   ├── Department.java
│       │   │   │   ├── Escalation.java
│       │   │   │   ├── Feedback.java
│       │   │   │   ├── Notification.java
│       │   │   │   └── Officer.java
│       │   │   ├── repository/
│       │   │   │   ├── CitizenRepository.java
│       │   │   │   ├── ComplaintHistoryRepository.java
│       │   │   │   ├── ComplaintRepository.java
│       │   │   │   ├── DepartmentRepository.java
│       │   │   │   ├── EscalationRepository.java
│       │   │   │   ├── FeedbackRepository.java
│       │   │   │   ├── NotificationRepository.java
│       │   │   └── service/
│       │   │       ├── AuthService.java
│       │   │       ├── ComplaintService.java
│       │   │       ├── GeminiService.java
│       │   │       ├── NotificationService.java
│       │   │       ├── OfficerService.java
│       │   │       └── impl/
│       │   │           ├── AuthServiceImpl.java
│       │   │           ├── ComplaintServiceImpl.java
│       │   │           ├── NotificationServiceImpl.java
│       │   │           └── OfficerServiceImpl.java
│       │   └── resources/
│       │       └── application.properties
│       └── test/
│           └── java/com/smartgovernance/SmartGovernanceApplicationTests.java
├── database/
│   └── schema.sql
├── frontend/
│   ├── index.html
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── App.jsx
│       ├── index.css
│       ├── main.jsx
│       ├── components/
│       │   ├── Footer.jsx
│       │   ├── LiveEvidenceCapture.jsx
│       │   ├── LivePhotoCapture.jsx
│       │   ├── Navbar.jsx
│       │   ├── OfficerComplaintsMap.jsx
│       │   └── ProtectedRoute.jsx
│       ├── pages/
│       │   ├── LandingPage.jsx
│       │   ├── auth/
│       │   │   ├── CitizenLogin.jsx
│       │   │   ├── CitizenRegister.jsx
│       │   │   └── OfficerLogin.jsx
│       │   ├── citizen/
│       │   │   ├── CitizenDashboard.jsx
│       │   │   ├── ComplaintDetail.jsx
│       │   │   ├── ComplaintForm.jsx
│       │   │   ├── ComplaintGuidelines.jsx
│       │   │   ├── FeedbackForm.jsx
│       │   │   ├── NotificationCenter.jsx
│       │   │   ├── NotificationPreferences.jsx
│       │   │   └── ReopenComplaint.jsx
│       │   └── officer/
│       │       ├── ComplaintQueue.jsx
│       │       ├── ComplaintReview.jsx
│       │       ├── DuplicateReview.jsx
│       │       ├── EmergencyComplaints.jsx
│       │       ├── EscalatedComplaints.jsx
│       │       ├── OfficerDashboard.jsx
│       │       └── ReopenedComplaints.jsx
│       ├── services/
│       │   └── api.js
│       └── styles/
│           ├── auth.css
│           ├── dashboard.css
│           ├── guidelines.css
│           ├── navbar.css
│           ├── notifications.css
│           └── officer.css
├── test_workflow.ps1
└── README.md
```

---

## 6. Core Modules Explained

### 1. Authentication & Security Module
- **Citizen Registration**: Validates Full Name, unique 12-digit Indian Aadhaar number, unique email address, 10-digit phone number, and minimum 8-character password.
- **Administrative Officer Access**: Officers log in using a government Employee ID (e.g., `EMP1001`) and password. Self-registration for officers is restricted; accounts are seeded or provisioned by administrative command.
- **Session Architecture**: Backed by secure HTTP cookies with `HttpOnly` and `SameSite=Lax` protection, verified on each route via `/api/session`.

### 2. Complaint Registration Module
- **Guidelines Acknowledgement**: Before filing, citizens review guidelines detailing valid grievance types, required documentation, and consequences of fraudulent submissions.
- **AI Category & Priority Suggestion**: As the citizen describes the problem, `GeminiService` analyzes keywords and suggests the most relevant department (e.g., Sanitation, Roads, Electricity) and assigns an initial AI priority (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`).
- **Mandatory Live Photo & GPS Hardware Binding**: Integrates `LivePhotoCapture` which activates the device camera, queries current geolocation coordinates (`latitude`, `longitude`), and renders an indelible timestamp and address watermark onto the image canvas.
- **Optional Live Video**: Citizens can record short video evidence via `LiveEvidenceCapture` without allowing gallery uploads.
- **Emergency Priority Flagging**: Incidents involving safety hazards (e.g., open transformer, gas leakage) can be flagged as emergency for rapid intervention.

### 3. Officer Dashboard & Complaint Review Module
- **Assigned Queue**: Departmental officers view grievances assigned to their specific department.
- **Status Workflow**: Officers update complaint status through valid progressions:
  `APPLIED` → `IN_PROGRESS` / `ACTION_TO_BE_TAKEN` → `COMPLAINT_RESOLVED` or `REJECTED`.
- **Geospatial Map View**: `OfficerComplaintsMap` plots complaints geographically, allowing field officers to group nearby tasks.
- **Duplicate Detection Review**: Potential duplicates identified by location or description can be inspected, marked, and linked to master grievances.
- **Citizen Reopen Review**: When citizens request a case reopen, the officer reviews their justification and determines whether to restart work.

### 4. Feedback Module
- **Resolution Verification**: Available exclusively after a complaint reaches `COMPLAINT_RESOLVED`.
- **Rating (1–5 Stars) & Comments**: Captures citizen satisfaction ratings and qualitative commentary.
- **Optional Resolution Evidence**: Allows citizens to provide live photographic or video proof showing whether work was completed properly.
- **Officer & Admin Inspection**: Feedback details are directly embedded inside `OfficerComplaintDetailDto` and presented on the officer review screen.
- **Single Submission Constraint**: Exactly one feedback entry per complaint, enforced at the database layer with unique index `uk_feedback_complaint`.

### 5. Escalation Module
- **Multi-Level Escalation**: Complaints can be escalated by officers or citizens to higher authorities (e.g., Level 1: Zonal Executive, Level 2: Chief Engineer, Level 3: Municipal Commissioner).
- **Target & Reason Tracking**: Each escalation specifies the target authority, formal reason, and detailed remarks.
- **Officer & Admin Handling**: Escalations are monitored in the Dedicated Officer Escalation Queue (`/officer/escalated`). Authorities can update the escalation status to `RESOLVED` with sign-off notes.
- **Lifecycle Integration**: Escalations update the master complaint state and trigger citizen notifications.

### 6. Notification Module
- **Event-Driven Lifecycle Triggers**: Automated notifications created on every significant milestone:
  - Complaint Submission (`COMPLAINT_SUBMITTED`)
  - Status Progression (`STATUS_CHANGED`, `COMPLAINT_RESOLVED`)
  - Duplicate Marked (`COMPLAINT_MARKED_DUPLICATE`)
  - Feedback Recorded (`FEEDBACK_RECEIVED`)
  - Case Escalated (`COMPLAINT_ESCALATED`)
  - Escalation Updated/Resolved (`ESCALATION_UPDATED`)
  - Case Reopened (`COMPLAINT_REOPENED`)
- **Live Navbar Badge**: Unread notification counter dynamically polls `/api/notifications/unread-count` and displays an indicator on the bell icon.
- **Notification Center**: Dedicated page (`/citizen/notifications`) with tabs for "All" vs "Unread", individual "Mark as Read", bulk "Mark All as Read", and "Delete".
- **Notification Preferences**: Configurable citizen preferences page (`/citizen/notifications/preferences`) persisted in browser storage.

---

## 7. End-to-End Workflow

```
[1. Citizen Registration & Login]
         │
         ▼
[2. Review Guidelines] ───► [3. Fill Complaint Form]
                                     │
                                     ├─► AI Category & Priority Detection
                                     ├─► Live Camera Photo + Hardware GPS Stamp
                                     └─► Location Address Geocoding
                                     │
                                     ▼
                      [4. Complaint Submitted in MySQL]
                                     │
                                     ├─► Auto-assigned to Department Officer
                                     └─► NOTIFICATION: COMPLAINT_SUBMITTED
                                     │
                                     ▼
                    [5. Officer Reviews Complaint]
                                     │
                                     ├─► Views Details, Photos, GPS on Map
                                     ├─► Status: ACTION_TO_BE_TAKEN
                                     │   └─► NOTIFICATION: STATUS_CHANGED
                                     │
                                     ▼
                    [6. Officer Resolves Complaint]
                                     │
                                     ├─► Status: COMPLAINT_RESOLVED
                                     └─► NOTIFICATION: COMPLAINT_RESOLVED
                                     │
                                     ▼
                   [7. Citizen Submits Feedback]
                                     │
                                     ├─► Rating (1-5), Remarks, Live Photo
                                     └─► NOTIFICATION: FEEDBACK_RECEIVED
                                     │
                                     ▼
                  [8. Officer Escalates / Reopens]
                                     │
                                     ├─► Escalates to Chief Engineer / Level 1
                                     ├─► NOTIFICATION: COMPLAINT_ESCALATED
                                     │
                                     ▼
                [9. Higher Authority Resolves Escalation]
                                     │
                                     ├─► Escalation Status: RESOLVED
                                     └─► NOTIFICATION: ESCALATION_UPDATED
                                     │
                                     ▼
               [10. Citizen Notification Center Hub]
                                     │
                                     ├─► Views all 6 real-time notifications
                                     ├─► Marks single / all as read
                                     └─► Unread count updates to 0
```

---

## 8. Database Schema & Entity Relationships

### 1. `citizen` Table
| Column | Type | Constraints | Description |
|---|---|---|---|
| `citizen_id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier for citizen |
| `aadhaar_number` | VARCHAR(12) | UNIQUE, NOT NULL | 12-digit Indian national ID |
| `full_name` | VARCHAR(100) | NOT NULL | Legal name |
| `email` | VARCHAR(100) | UNIQUE, NOT NULL | Login email address |
| `phone_number` | VARCHAR(15) | NOT NULL | 10-digit mobile number |
| `password` | VARCHAR(255) | NOT NULL | BCrypt hashed password |
| `address` | TEXT | NULL | Residential address |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Registration timestamp |

### 2. `department` Table
| Column | Type | Constraints | Description |
|---|---|---|---|
| `department_id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique department ID |
| `name` | VARCHAR(100) | UNIQUE, NOT NULL | Department name (e.g. Sanitation) |
| `description` | TEXT | NULL | Scope and responsibilities |

### 3. `officer` Table
| Column | Type | Constraints | Description |
|---|---|---|---|
| `officer_id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique officer ID |
| `employee_id` | VARCHAR(50) | UNIQUE, NOT NULL | Government ID (e.g. `EMP1001`) |
| `full_name` | VARCHAR(100) | NOT NULL | Officer name |
| `email` | VARCHAR(100) | UNIQUE, NOT NULL | Official email |
| `phone_number` | VARCHAR(15) | NOT NULL | Contact phone |
| `password` | VARCHAR(255) | NOT NULL | BCrypt hashed password |
| `department` | VARCHAR(100) | NOT NULL | Department name |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Provisioning date |

### 4. `complaint` Table
| Column | Type | Constraints | Description |
|---|---|---|---|
| `complaint_id` | VARCHAR(50) | PRIMARY KEY | Unique ID (e.g. `CMP-178872...`) |
| `citizen_id` | BIGINT | FOREIGN KEY -> citizen | Submitting citizen |
| `department_id` | BIGINT | FOREIGN KEY -> department | Responsible department |
| `officer_id` | BIGINT | FOREIGN KEY -> officer, NULL | Assigned officer |
| `title` | VARCHAR(200) | NOT NULL | Brief grievance title |
| `description` | TEXT | NOT NULL | Detailed problem description |
| `location` | VARCHAR(255) | NOT NULL | Human-readable address |
| `latitude` | DECIMAL(10,8) | NULL | GPS latitude |
| `longitude` | DECIMAL(11,8) | NULL | GPS longitude |
| `image_url` | LONGTEXT | NOT NULL | Live camera geotagged image |
| `video_url` | LONGTEXT | NULL | Optional live video recording |
| `complaint_type` | VARCHAR(50) | DEFAULT 'NORMAL' | NORMAL or EMERGENCY |
| `ai_priority` | VARCHAR(20) | NULL | LOW, MEDIUM, HIGH, CRITICAL |
| `status` | VARCHAR(30) | NOT NULL | APPLIED, IN_PROGRESS, ACTION_TO_BE_TAKEN, COMPLAINT_RESOLVED, REJECTED, REOPENED |
| `duplicate_status` | VARCHAR(30) | DEFAULT 'ORIGINAL' | ORIGINAL, DUPLICATE_FLAGGED, VERIFIED_DUPLICATE |
| `duplicate_of_complaint_id` | VARCHAR(50) | FOREIGN KEY -> complaint | Master complaint ID if duplicate |
| `action_taken` | TEXT | NULL | Officer resolution remarks |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| `updated_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Last update timestamp |
| `resolved_at` | TIMESTAMP | NULL | Resolution timestamp |

### 5. `feedback` Table
| Column | Type | Constraints | Description |
|---|---|---|---|
| `feedback_id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique feedback ID |
| `complaint_id` | VARCHAR(50) | FOREIGN KEY -> complaint, UNIQUE | Target complaint (1:1 constraint) |
| `citizen_id` | BIGINT | FOREIGN KEY -> citizen | Submitting citizen |
| `rating` | INT | NOT NULL, CHECK (rating BETWEEN 1 AND 5) | Satisfaction rating |
| `comment` | TEXT | NULL | Citizen remarks |
| `image_url` | LONGTEXT | NULL | Live geotagged resolution proof |
| `video_url` | LONGTEXT | NULL | Live resolution video proof |
| `latitude` | DOUBLE | NULL | GPS latitude of feedback |
| `longitude` | DOUBLE | NULL | GPS longitude of feedback |
| `location` | VARCHAR(255) | NULL | Address where feedback captured |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Submission timestamp |

### 6. `escalation` Table
| Column | Type | Constraints | Description |
|---|---|---|---|
| `escalation_id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique escalation ID |
| `complaint_id` | VARCHAR(50) | FOREIGN KEY -> complaint | Grievance being escalated |
| `escalation_level` | INT | NOT NULL, DEFAULT 1 | Escalation tier (1, 2, 3) |
| `reason` | VARCHAR(255) | NOT NULL | Reason category |
| `target` | VARCHAR(100) | NULL | Designated authority |
| `status` | VARCHAR(50) | NOT NULL, DEFAULT 'PENDING' | PENDING, IN_PROGRESS, RESOLVED, REJECTED |
| `remarks` | TEXT | NULL | Action and resolution remarks |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Escalation timestamp |
| `updated_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Last status update |

### 7. `notifications` Table
| Column | Type | Constraints | Description |
|---|---|---|---|
| `notification_id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique notification ID |
| `citizen_id` | BIGINT | FOREIGN KEY -> citizen | Recipient citizen |
| `complaint_id` | VARCHAR(50) | FOREIGN KEY -> complaint | Associated grievance |
| `type` | VARCHAR(50) | NOT NULL | Event classification code |
| `title` | VARCHAR(255) | NOT NULL | Header message |
| `message` | TEXT | NOT NULL | Detailed notification body |
| `is_read` | BOOLEAN | DEFAULT FALSE | Read/unread flag |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Notification timestamp |

---

## 9. Complete REST API Reference

### 1. Authentication Endpoints (`/api/auth`)
| Method | Path | Request Body | Response Data | Description |
|---|---|---|---|---|
| `POST` | `/api/auth/citizen/register` | `CitizenRegisterRequest` | `Citizen` details | Register new citizen account |
| `POST` | `/api/auth/citizen/login` | `LoginRequest` | `Citizen` details + session cookie | Citizen authentication |
| `POST` | `/api/auth/officer/login` | `OfficerLoginRequest` | `Officer` details + session cookie | Officer authentication |
| `POST` | `/api/auth/logout` | None | `{ message: "Logged out" }` | Invalidate active session |
| `GET` | `/api/auth/health` | None | `{ status: "UP", timestamp }` | Service health status |

### 2. Session Management (`/api/session`)
| Method | Path | Request Body | Response Data | Description |
|---|---|---|---|---|
| `GET` | `/api/session` | None | `SessionResponse` | Check active authentication session |

### 3. Citizen Complaints (`/api/citizen/complaints`)
| Method | Path | Request Body | Response Data | Description |
|---|---|---|---|---|
| `POST` | `/api/citizen/complaints` | `ComplaintCreateRequest` | `ComplaintResponseDto` | File new complaint with live evidence |
| `GET` | `/api/citizen/complaints` | None | `List<ComplaintResponseDto>` | List complaints filed by logged-in citizen |
| `POST` | `/api/citizen/complaints/suggest-category` | `{ description }` | `{ suggestedDepartment, suggestedPriority }` | AI-assisted department suggestion |

### 4. Officer Endpoints (`/api/officer`)
| Method | Path | Request Body | Response Data | Description |
|---|---|---|---|---|
| `GET` | `/api/officer/dashboard` | None | `OfficerDashboardStatsDto` | Live officer queue metrics & counts |
| `GET` | `/api/officer/my-complaints` | None | `List<ComplaintResponseDto>` | Complaints assigned to officer |
| `GET` | `/api/officer/my-complaints/{id}` | None | `OfficerComplaintDetailDto` | Detail view with history, feedback, escalation |
| `PUT` | `/api/officer/my-complaints/{id}/status` | `ComplaintStatusUpdateRequest` | `OfficerComplaintDetailDto` | Update progress status and resolution remarks |
| `POST` | `/api/officer/my-complaints/{id}/escalate` | `EscalationRequest` | `OfficerComplaintDetailDto` | Escalate complaint to higher authority |
| `PUT` | `/api/officer/escalations/{escId}/status` | `EscalationStatusUpdateRequest` | `OfficerComplaintDetailDto` | Resolve or update escalation status |
| `GET` | `/api/officer/escalated` | None | `List<ComplaintResponseDto>` | View all escalated grievances |
| `GET` | `/api/officer/duplicates` | None | `List<ComplaintResponseDto>` | View duplicate-flagged grievances |
| `PUT` | `/api/officer/duplicates/{id}/verify` | `ComplaintDuplicateStatusRequest` | `OfficerComplaintDetailDto` | Confirm or clear duplicate status |
| `GET` | `/api/officer/reopened` | None | `List<ComplaintResponseDto>` | View citizen-reopened grievances |

### 5. Feedback Endpoints (`/api/complaints/{id}/feedback`)
| Method | Path | Request Body | Response Data | Description |
|---|---|---|---|---|
| `POST` | `/api/complaints/{id}/feedback` | `FeedbackRequest` | `ComplaintResponseDto` | Submit rating, comment, and resolution evidence |

### 6. Notifications Endpoints (`/api/notifications`)
| Method | Path | Request Body | Response Data | Description |
|---|---|---|---|---|
| `GET` | `/api/notifications` | Query: `?unreadOnly=true` | `List<NotificationDto>` | List citizen notifications |
| `GET` | `/api/notifications/unread-count` | None | `{ unreadCount: n }` | Count of unread citizen notifications |
| `PUT` / `PATCH` | `/api/notifications/{id}/read` | None | `NotificationDto` | Mark single notification as read |
| `PUT` / `PATCH` | `/api/notifications/read-all` | None | `{ count: n }` | Mark all notifications as read |
| `DELETE` | `/api/notifications/{id}` | None | Success acknowledgment | Delete notification |

---

## 10. Notification Types & Triggers Table

| Notification Type | Trigger Event | Title | Recipient |
|---|---|---|---|
| `COMPLAINT_SUBMITTED` | Citizen files a grievance | Complaint Registered: {title} | Submitting Citizen |
| `STATUS_CHANGED` | Officer moves complaint to `IN_PROGRESS` or `ACTION_TO_BE_TAKEN` | Complaint Status: {status} | Submitting Citizen |
| `COMPLAINT_RESOLVED` | Officer marks complaint as `COMPLAINT_RESOLVED` | Complaint Status: COMPLAINT_RESOLVED | Submitting Citizen |
| `FEEDBACK_RECEIVED` | Citizen submits 1-5 star resolution feedback | Feedback Received | Submitting Citizen |
| `COMPLAINT_ESCALATED` | Officer or system escalates case to higher tier | Complaint Escalated | Submitting Citizen |
| `ESCALATION_UPDATED` | Authority resolves or updates escalation | Escalation Updated | Submitting Citizen |
| `COMPLAINT_REOPENED` | Citizen submits valid case reopen request | Complaint Reopened | Submitting Citizen |
| `COMPLAINT_MARKED_DUPLICATE`| Officer confirms case as duplicate of master | Complaint Update: Duplicate Flagged | Submitting Citizen |

---

## 11. Security & Validation Rules

1. **Aadhaar Verification**: Enforces 12-digit numeric constraint via regex `^\d{12}$` with uniqueness validation.
2. **Password Cryptography**: Salted hash using Spring Security's `BCryptPasswordEncoder` with strength 10. Raw passwords are never persisted.
3. **Role Authorization**: Endpoints verify session user types (`CITIZEN` vs `OFFICER`). Citizens cannot update officer queues; officers cannot file citizen feedback.
4. **Anti-Spoofing Evidence Policy**:
   - Web application uses HTML5 Camera API exclusively (`video: { facingMode: 'environment' }`).
   - File upload (`<input type="file">`) is disabled to prevent gallery, screenshot, or internet image spoofing.
   - Coordinates (`latitude`, `longitude`) are permanently stamped onto canvas frames before submission.
5. **Feedback Constraints**:
   - Only allowed when complaint status is `COMPLAINT_RESOLVED`.
   - Only the citizen who filed the grievance can submit feedback.
   - Unique complaint constraint prevents duplicate feedback entries for a single grievance.

---

## 12. Setup & Installation Guide

### Prerequisites
- **Java**: JDK 17 or higher
- **Maven**: 3.8+ (or included `mvn`)
- **Node.js**: v18+ and `npm`
- **MySQL**: 8.0+ server running on port 3306

### 1. Database Setup
Log into MySQL and initialize the database schema:
```sql
CREATE DATABASE finally_civicpulse_db;
USE finally_civicpulse_db;
SOURCE database/schema.sql;
```

### 2. Backend Configuration
The backend configuration in `backend/src/main/resources/application.properties` supports environment variable overrides:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/finally_civicpulse_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:Root@321}
gemini.api.key=${GEMINI_API_KEY:YOUR_GEMINI_API_KEY}
server.port=8080
```

> **Note**: If your local MySQL password differs from `Root@321`, simply pass the environment variable when running (e.g., `$env:DB_PASSWORD="your_password"`).

### 3. Frontend Configuration
The frontend connects directly to `http://localhost:8080/api` with automatic fallback.

---

## 13. How to Run

### Step 1: Start Backend
In a terminal, navigate to `backend/` and execute:
```bash
# Windows PowerShell
$env:DB_PASSWORD="your_mysql_password"
mvn spring-boot:run

# Linux / macOS
DB_PASSWORD="your_mysql_password" mvn spring-boot:run
```
The backend will initialize JPA entities, seed the 12 municipal departments, seed the default officer (`EMP1001`), and start on `http://localhost:8080`.

### Step 2: Start Frontend
In a second terminal, navigate to `frontend/`:
```bash
npm install
npm run dev
```
Open your browser at `http://localhost:5173`.

---

## 14. How to Test & Verify

### Default Credentials
- **Default Officer**:
  - Employee ID: `EMP1001`
  - Password: `Officer@123`
  - Department: `Sanitation Department`
- **Citizen Account**:
  - Register a new citizen via the UI or run the automated verification script.

### Automated End-to-End Verification Script
An automated verification test script `test_workflow.ps1` is provided. It runs all 10 workflow phases end-to-end against real MySQL database tables without mock data:
```powershell
powershell -ExecutionPolicy Bypass -File test_workflow.ps1
```

The script executes and verifies:
1. Citizen Registration & Login
2. Complaint Submission with Live Geotagged Evidence
3. Verification of Notification Generation
4. Officer Login
5. Officer Status Progression (`APPLIED` → `ACTION_TO_BE_TAKEN` → `COMPLAINT_RESOLVED`)
6. Citizen Feedback Submission (Rating 5/5 + Remarks)
7. Officer Inspection of Citizen Feedback
8. Officer Complaint Escalation (Level 1, Target: Chief Engineer)
9. Officer Escalation Resolution
10. Citizen Notification Hub Verification (Bulk Read Actions & Counter)

---

## 15. Team Contributions & Module Breakdown

| Module | Team Member | Status | Verification |
|---|---|---|---|
| **Complaint Registration** | Teammate | Complete | Live camera photo & GPS hardware binding, AI category suggestion, emergency flag |
| **Officer Dashboard & Review**| Teammate | Complete | Department queue, geospatial map view, duplicate detection, status update |
| **Feedback Module** | Current Work | Complete | Rating (1-5), comments, resolution verification, officer inspection, MySQL persistence |
| **Escalation Module** | Current Work | Complete | Multi-tier escalation, target authority tracking, escalation queue, status resolution |
| **Notification Module** | Current Work | Complete | Event triggers, unread badge counter, notification center, bulk mark-as-read, preferences |

---

## 16. Troubleshooting & Common Issues

1. **MySQL Access Denied (Password mismatch)**:
   Set the `DB_PASSWORD` environment variable before running Maven:
   `$env:DB_PASSWORD="your_password"; mvn spring-boot:run`
2. **Camera / Geolocation Access Denied**:
   Ensure browser site permissions permit Camera and Location access. In Chrome, check `chrome://settings/content/camera` and `chrome://settings/content/location`.
3. **CORS or Cookie Issues**:
   Ensure both frontend (`localhost:5173`) and backend (`localhost:8080`) are accessed via `http://localhost` (not `127.0.0.1`) so session cookies match domain origins.

---

## 17. License & Academic Disclaimer
Developed as part of the **CivicPulse Smart Governance Platform** project. Intended for institutional demonstration and administrative civic workflow modernization.
All rights reserved.
