# Smart Governance Platform — Integrated Feature Checklist

This package keeps the existing Citizen UI and implements the Officer workflow on the same central `complaint` record.

## Officer module
- Dashboard with live officer identity and department
- Live Total / Pending / In Progress / Resolved / Reopened / Duplicate / Escalated / Rejected counts
- Maximum 4 newest assigned complaints on dashboard
- Search by complaint ID or title
- Status filtering using existing ComplaintStatus values
- Detailed complaint review
- Citizen information, location, priority/type, date/time
- Photo/video evidence display
- Status update with remarks, officer attribution, timestamp and complaint history
- Backend authorization by assigned officer
- Reopened complaint handling in normal Complaints flow
- Reopen request details on review page
- Rule-based possible duplicate detection
- Dedicated Duplicate Complaints page
- Duplicate comparison/review
- Confirm Duplicate with original complaint link, no deletion, history
- Not a Duplicate decision with history
- Dedicated Escalated Complaints page
- Escalate Complaint workflow with reason, remarks, level and target
- Escalation details and history on complaint review
- Logout
- Existing application styling reused; responsive officer pages

## Citizen workflow preserved
- Citizen registration/login/session
- Citizen complaint submission to the central Complaint table
- Existing department selection
- Existing Normal/Emergency selection
- Existing Get Location/geolocation behavior
- Existing photo/video evidence behavior
- Citizen complaint list/detail/history
- Feedback after resolution
- Reopen request on the same complaint ID

## Data integrity
- No new CitizenComplaint/OfficerComplaint/DuplicateComplaint tables
- Duplicate complaints are retained and linked to the original
- Reopened complaints retain the same complaint ID
- Officer-created changes are persisted through Spring Boot/JPA/MySQL
- Officer complaint seeding was removed so future startups do not create synthetic complaints
- Existing database records are not deleted or reset

## New additive backend fields
- `complaint.duplicate_of_complaint_id`
- `escalation.remarks`
- `escalation.target`

The existing `SchemaMigration` adds these fields safely on application startup.
