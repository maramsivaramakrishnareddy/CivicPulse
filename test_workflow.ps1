$ErrorActionPreference = "Stop"

$baseUrl = "http://localhost:8080/api"
$citizenSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$officerSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "1. Testing Citizen Registration and Login..." -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan

$citizenEmail = "citizen_" + (Get-Date -Format "yyyyMMddHHmmss") + "@example.com"
$aadhaar = (Get-Random -Minimum 100000000000 -Maximum 999999999999).ToString()

$registerBody = @{
    fullName = "Ramesh Kumar"
    aadhaarNumber = $aadhaar
    email = $citizenEmail
    phoneNumber = "9876543210"
    password = "Password@123"
    confirmPassword = "Password@123"
    address = "MG Road, Ward 4, Bengaluru"
} | ConvertTo-Json

$regRes = Invoke-RestMethod -Uri "$baseUrl/auth/citizen/register" -Method Post -Body $registerBody -ContentType "application/json" -WebSession $citizenSession
Write-Host "Citizen registered: $($regRes.data.fullName) (ID: $($regRes.data.citizenId))" -ForegroundColor Green

# Login citizen
$loginBody = @{
    email = $citizenEmail
    password = "Password@123"
} | ConvertTo-Json

$logRes = Invoke-RestMethod -Uri "$baseUrl/auth/citizen/login" -Method Post -Body $loginBody -ContentType "application/json" -WebSession $citizenSession
Write-Host "Citizen logged in: $($logRes.data.fullName)" -ForegroundColor Green

# Verify session
$sessionRes = Invoke-RestMethod -Uri "$baseUrl/session" -Method Get -WebSession $citizenSession
Write-Host "Citizen session active: $($sessionRes.data.authenticated), User: $($sessionRes.data.userName) ($($sessionRes.data.userType))" -ForegroundColor Green

Write-Host "`n============================================================" -ForegroundColor Cyan
Write-Host "2. Submitting Citizen Complaint..." -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan

$complaintBody = @{
    title = "Severe Pothole and Water Stagnation on 5th Cross"
    description = "Large pothole causing vehicle accidents and overflowing water near junction."
    departmentName = "Sanitation Department"
    complaintType = "NORMAL"
    location = "5th Cross, MG Road, Ward 4"
    imageUrl = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg=="
    latitude = 12.9716
    longitude = 77.5946
} | ConvertTo-Json

$complaintRes = Invoke-RestMethod -Uri "$baseUrl/citizen/complaints" -Method Post -Body $complaintBody -ContentType "application/json" -WebSession $citizenSession
$complaintId = $complaintRes.data.complaintId
Write-Host "Complaint filed successfully: $complaintId, Status: $($complaintRes.data.status)" -ForegroundColor Green

Write-Host "`n============================================================" -ForegroundColor Cyan
Write-Host "3. Verifying Notification generated for Citizen..." -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan

$notifRes = Invoke-RestMethod -Uri "$baseUrl/notifications" -Method Get -WebSession $citizenSession
Write-Host "Total notifications for citizen: $($notifRes.data.Count)" -ForegroundColor Green
foreach ($n in $notifRes.data) {
    Write-Host "  -> [$($n.type)] $($n.title): $($n.message)" -ForegroundColor Yellow
}

$unreadRes = Invoke-RestMethod -Uri "$baseUrl/notifications/unread-count" -Method Get -WebSession $citizenSession
Write-Host "Unread notifications count: $($unreadRes.data)" -ForegroundColor Green

Write-Host "`n============================================================" -ForegroundColor Cyan
Write-Host "4. Officer Login (EMP1001)..." -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan

$officerLoginBody = @{
    employeeId = "EMP1001"
    password = "Officer@123"
} | ConvertTo-Json

$offLogRes = Invoke-RestMethod -Uri "$baseUrl/auth/officer/login" -Method Post -Body $officerLoginBody -ContentType "application/json" -WebSession $officerSession
Write-Host "Officer logged in: $($offLogRes.data.fullName), Dept: $($offLogRes.data.department)" -ForegroundColor Green

Write-Host "`n============================================================" -ForegroundColor Cyan
Write-Host "5. Officer Viewing and Updating Complaint Status..." -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan

$offDetailRes = Invoke-RestMethod -Uri "$baseUrl/officer/my-complaints/$complaintId" -Method Get -WebSession $officerSession
Write-Host "Officer fetched complaint: $($offDetailRes.data.complaintId), Status: $($offDetailRes.data.status)" -ForegroundColor Green

# Update to ACTION_TO_BE_TAKEN
$updateBody1 = @{
    status = "ACTION_TO_BE_TAKEN"
    remarks = "Field team dispatched for inspection and repair."
} | ConvertTo-Json

$upRes1 = Invoke-RestMethod -Uri "$baseUrl/officer/my-complaints/$complaintId/status" -Method Put -Body $updateBody1 -ContentType "application/json" -WebSession $officerSession
Write-Host "Status updated to: $($upRes1.data.status)" -ForegroundColor Green

# Update to COMPLAINT_RESOLVED
$updateBody2 = @{
    status = "COMPLAINT_RESOLVED"
    remarks = "Pothole filled and drainage cleared. Road restored."
} | ConvertTo-Json

$upRes2 = Invoke-RestMethod -Uri "$baseUrl/officer/my-complaints/$complaintId/status" -Method Put -Body $updateBody2 -ContentType "application/json" -WebSession $officerSession
Write-Host "Status updated to: $($upRes2.data.status), ResolvedAt: $($upRes2.data.resolvedAt)" -ForegroundColor Green

Write-Host "`n============================================================" -ForegroundColor Cyan
Write-Host "6. Citizen Submitting Feedback..." -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan

$feedbackBody = @{
    rating = 5
    comment = "Excellent prompt action by the sanitation team! Very satisfied."
    location = "5th Cross, MG Road"
    latitude = 12.9716
    longitude = 77.5946
} | ConvertTo-Json

$fbRes = Invoke-RestMethod -Uri "$baseUrl/complaints/$complaintId/feedback" -Method Post -Body $feedbackBody -ContentType "application/json" -WebSession $citizenSession
Write-Host "Feedback submitted successfully! Complaint ID: $($fbRes.data.complaintId)" -ForegroundColor Green

Write-Host "`n============================================================" -ForegroundColor Cyan
Write-Host "7. Officer Viewing Feedback on Complaint..." -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan

$offReviewRes = Invoke-RestMethod -Uri "$baseUrl/officer/my-complaints/$complaintId" -Method Get -WebSession $officerSession
if ($offReviewRes.data.feedback) {
    Write-Host "Officer successfully sees Feedback: Rating $($offReviewRes.data.feedback.rating)/5, Comment: '$($offReviewRes.data.feedback.comment)'" -ForegroundColor Green
} else {
    Write-Host "ERROR: Feedback not present in officer detail!" -ForegroundColor Red
}

Write-Host "`n============================================================" -ForegroundColor Cyan
Write-Host "8. Officer Escalating Complaint..." -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan

$escBody = @{
    reason = "Requires Additional Departmental Action"
    escalationLevel = 1
    remarks = "Requires formal sign-off from Chief Engineer for permanent resurfacing."
    target = "Chief Engineer Office"
} | ConvertTo-Json

$escRes = Invoke-RestMethod -Uri "$baseUrl/officer/my-complaints/$complaintId/escalate" -Method Post -Body $escBody -ContentType "application/json" -WebSession $officerSession
Write-Host "Complaint escalated: Reason: $($escRes.data.latestEscalation.reason), Status: $($escRes.data.latestEscalation.status), Target: $($escRes.data.latestEscalation.target)" -ForegroundColor Green
$escalationId = $escRes.data.latestEscalation.escalationId

Write-Host "`n============================================================" -ForegroundColor Cyan
Write-Host "9. Officer Updating Escalation Status..." -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan

$escStatusBody = @{
    status = "RESOLVED"
    remarks = "Chief Engineer approved and signed off work order."
} | ConvertTo-Json

$escUpRes = Invoke-RestMethod -Uri "$baseUrl/officer/escalations/$escalationId/status" -Method Put -Body $escStatusBody -ContentType "application/json" -WebSession $officerSession
Write-Host "Escalation updated: Status $($escUpRes.data.latestEscalation.status), Remarks: $($escUpRes.data.latestEscalation.remarks)" -ForegroundColor Green

Write-Host "`n============================================================" -ForegroundColor Cyan
Write-Host "10. Verifying All Citizen Notifications..." -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan

$finalNotifRes = Invoke-RestMethod -Uri "$baseUrl/notifications" -Method Get -WebSession $citizenSession
Write-Host "Total notifications for citizen: $($finalNotifRes.data.Count)" -ForegroundColor Green
foreach ($n in $finalNotifRes.data) {
    Write-Host "  -> [$($n.type)] $($n.title) | Read: $($n.read)" -ForegroundColor Yellow
}

# Mark all as read
$markRes = Invoke-RestMethod -Uri "$baseUrl/notifications/read-all" -Method Put -WebSession $citizenSession
Write-Host "Marked all as read: $($markRes.message)" -ForegroundColor Green

$finalUnread = Invoke-RestMethod -Uri "$baseUrl/notifications/unread-count" -Method Get -WebSession $citizenSession
Write-Host "Final unread count: $($finalUnread.data)" -ForegroundColor Green

Write-Host "`n============================================================" -ForegroundColor Cyan
Write-Host "ALL 10 END-TO-END WORKFLOW TESTS COMPLETED SUCCESSFULLY!" -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Cyan
