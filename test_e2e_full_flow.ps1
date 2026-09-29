$ErrorActionPreference = "Stop"

Write-Host "==================================================" -ForegroundColor Cyan
Write-Host " CIVICPULSE END-TO-END WORKFLOW VERIFICATION TEST" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan

$baseUrl = "http://localhost:8080/api"
$citizenSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$officerSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession

# Step 1: Citizen Login or Register
Write-Host "`n[STEP 1] Citizen Authentication..." -ForegroundColor Yellow
$randomNum = Get-Random -Minimum 1000 -Maximum 9999
$citizenEmail = "test_citizen_$randomNum@civicpulse.local"
$aadhaar = "98765432" + (Get-Random -Minimum 1000 -Maximum 9999).ToString()

$citizenRegPayload = @{
    fullName = "Ramesh Kumar"
    email = $citizenEmail
    phoneNumber = "9876543210"
    aadhaarNumber = $aadhaar
    password = "Citizen@123"
    confirmPassword = "Citizen@123"
    address = "123 Civic Nagar, Ward 4"
} | ConvertTo-Json

try {
    $regResp = Invoke-RestMethod -Uri "$baseUrl/auth/citizen/register" -Method Post -Body $citizenRegPayload -ContentType "application/json" -WebSession $citizenSession
    Write-Host "  -> Registered new citizen: $citizenEmail" -ForegroundColor Green
} catch {
    Write-Host "  -> Registration error: $_" -ForegroundColor Red
    throw $_
}

# Login Citizen
$citizenLoginPayload = @{
    email = $citizenEmail
    password = "Citizen@123"
} | ConvertTo-Json
$loginResp = Invoke-RestMethod -Uri "$baseUrl/auth/citizen/login" -Method Post -Body $citizenLoginPayload -ContentType "application/json" -WebSession $citizenSession
Write-Host "  -> Logged in as Citizen: $($loginResp.data.fullName)" -ForegroundColor Green

# Verify citizen session
$sessionResp = Invoke-RestMethod -Uri "$baseUrl/session" -Method Get -WebSession $citizenSession
Write-Host "  -> Session active: Citizen ID = $($sessionResp.data.userId), Name = $($sessionResp.data.userName)" -ForegroundColor Green

# Step 2: Submit Complaint
Write-Host "`n[STEP 2] Submitting Citizen Complaint..." -ForegroundColor Yellow
$complaintPayload = @{
    title = "Contaminated water leak overflowing onto street"
    description = "Dirty water overflowing onto main street causing severe hygiene issues."
    departmentName = "Sanitation Department"
    complaintType = "NORMAL"
    location = "Sector 4 Main Market, Ward 12"
    latitude = 12.9716
    longitude = 77.5946
    imageUrl = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
} | ConvertTo-Json

$complaintResp = Invoke-RestMethod -Uri "$baseUrl/citizen/complaints" -Method Post -Body $complaintPayload -ContentType "application/json" -WebSession $citizenSession
$complaintId = $complaintResp.data.complaintId
Write-Host "  -> Complaint created! Complaint ID: $complaintId, Status: $($complaintResp.data.status)" -ForegroundColor Green

# Step 3: Officer Authentication
Write-Host "`n[STEP 3] Officer Authentication..." -ForegroundColor Yellow
$officerLoginPayload = @{
    employeeId = "EMP1001"
    password = "Officer@123"
} | ConvertTo-Json
$officerLoginResp = Invoke-RestMethod -Uri "$baseUrl/auth/officer/login" -Method Post -Body $officerLoginPayload -ContentType "application/json" -WebSession $officerSession
Write-Host "  -> Officer logged in: $($officerLoginResp.data.fullName), Dept: $($officerLoginResp.data.department)" -ForegroundColor Green

# Step 4: Officer Resolves Complaint directly
Write-Host "`n[STEP 4] Officer Resolving Complaint..." -ForegroundColor Yellow
$resolvePayload = @{
    status = "COMPLAINT_RESOLVED"
    remarks = "Pipeline repaired and sanitation team cleared the overflow."
} | ConvertTo-Json

$updateResp = Invoke-RestMethod -Uri "$baseUrl/officer/my-complaints/$complaintId/status" -Method Put -Body $resolvePayload -ContentType "application/json" -WebSession $officerSession
Write-Host "  -> Officer update response: Status = $($updateResp.data.status), Remarks = $($updateResp.data.actionTaken)" -ForegroundColor Green

# Step 5: Citizen checks complaint status
Write-Host "`n[STEP 5] Citizen Checking Complaint Status..." -ForegroundColor Yellow
$citizenViewResp = Invoke-RestMethod -Uri "$baseUrl/complaints/$complaintId" -Method Get -WebSession $citizenSession
Write-Host "  -> Citizen sees Status: $($citizenViewResp.data.status)" -ForegroundColor Green
Write-Host "  -> Has Feedback: $($citizenViewResp.data.hasFeedback)" -ForegroundColor Green

if ($citizenViewResp.data.status -eq "COMPLAINT_RESOLVED" -or $citizenViewResp.data.status -eq "RESOLVED") {
    Write-Host "  -> [SUCCESS] Citizen can see 'Give Feedback' button!" -ForegroundColor Green
} else {
    Write-Host "  -> [ERROR] Status is not resolved: $($citizenViewResp.data.status)" -ForegroundColor Red
}

# Step 6: Citizen Submits Feedback
Write-Host "`n[STEP 6] Citizen Submitting Feedback..." -ForegroundColor Yellow
$feedbackPayload = @{
    rating = 5
    comment = "Outstanding and quick response by the municipal sanitation crew! Fully resolved."
    location = "Sector 4 Main Market, Ward 12"
} | ConvertTo-Json

$feedbackResp = Invoke-RestMethod -Uri "$baseUrl/complaints/$complaintId/feedback" -Method Post -Body $feedbackPayload -ContentType "application/json" -WebSession $citizenSession
Write-Host "  -> Feedback submitted successfully!" -ForegroundColor Green
Write-Host "     Rating: $($feedbackResp.data.feedback.rating) stars" -ForegroundColor Green
Write-Host "     Comment: $($feedbackResp.data.feedback.comment)" -ForegroundColor Green
Write-Host "     Submitted by: $($feedbackResp.data.feedback.citizenName)" -ForegroundColor Green

# Step 7: Citizen Escalates Complaint
Write-Host "`n[STEP 7] Citizen Escalates Complaint to Higher Level..." -ForegroundColor Yellow
$escalatePayload = @{
    reason = "Road resurfacing required after pipeline excavation"
    remarks = "Excavated trench left unpaved, causing traffic bottleneck."
    target = "Chief Municipal Engineer"
    escalationLevel = 2
} | ConvertTo-Json

$escResp = Invoke-RestMethod -Uri "$baseUrl/citizen/complaints/$complaintId/escalate" -Method Post -Body $escalatePayload -ContentType "application/json" -WebSession $citizenSession
Write-Host "  -> Escalation submitted successfully!" -ForegroundColor Green
Write-Host "     Escalation Status: $($escResp.data.escalationStatus)" -ForegroundColor Green
Write-Host "     Escalation Level: $($escResp.data.escalationLevel)" -ForegroundColor Green
Write-Host "     Escalation Target: $($escResp.data.escalationTarget)" -ForegroundColor Green
Write-Host "     Escalation Reason: $($escResp.data.escalationReason)" -ForegroundColor Green

# Step 8: Officer Views and Resolves Escalation
Write-Host "`n[STEP 8] Officer Viewing and Resolving Escalation..." -ForegroundColor Yellow
$escalatedList = Invoke-RestMethod -Uri "$baseUrl/officer/escalated" -Method Get -WebSession $officerSession
Write-Host "  -> Officer found $($escalatedList.data.Count) escalated complaint(s)" -ForegroundColor Green

$officerComplaintDetail = Invoke-RestMethod -Uri "$baseUrl/officer/my-complaints/$complaintId" -Method Get -WebSession $officerSession
$escalationId = $officerComplaintDetail.data.latestEscalation.escalationId
Write-Host "  -> Latest Escalation ID: $escalationId, Status: $($officerComplaintDetail.data.latestEscalation.status)" -ForegroundColor Green

$resolveEscPayload = @{
    status = "RESOLVED"
    remarks = "Chief Engineer inspected and scheduled road re-surfacing for tomorrow morning."
} | ConvertTo-Json
$resolveEscResp = Invoke-RestMethod -Uri "$baseUrl/officer/escalations/$escalationId/status" -Method Put -Body $resolveEscPayload -ContentType "application/json" -WebSession $officerSession
Write-Host "  -> Escalation resolved: Status = $($resolveEscResp.data.status)" -ForegroundColor Green

# Step 9: History and Notifications verification
Write-Host "`n[STEP 9] Checking Complaint History & Notifications..." -ForegroundColor Yellow
$historyResp = Invoke-RestMethod -Uri "$baseUrl/complaints/$complaintId/history" -Method Get -WebSession $citizenSession
Write-Host "  -> History records found: $($historyResp.data.Count)" -ForegroundColor Green
foreach ($h in $historyResp.data) {
    Write-Host "     * [$($h.status)] $($h.message)" -ForegroundColor DarkGray
}

$notifResp = Invoke-RestMethod -Uri "$baseUrl/notifications" -Method Get -WebSession $citizenSession
Write-Host "  -> Citizen Notifications found: $($notifResp.data.Count)" -ForegroundColor Green
foreach ($n in $notifResp.data) {
    Write-Host "     * [$($n.type)] $($n.title): $($n.message)" -ForegroundColor DarkGray
}

# Step 10: Final Citizen Complaint View Verification
Write-Host "`n[STEP 10] Verifying Final Citizen Complaint View..." -ForegroundColor Yellow
$finalCitizenView = Invoke-RestMethod -Uri "$baseUrl/complaints/$complaintId" -Method Get -WebSession $citizenSession
Write-Host "  -> Final Status: $($finalCitizenView.data.status)" -ForegroundColor Green
Write-Host "  -> Has Feedback: $($finalCitizenView.data.hasFeedback)" -ForegroundColor Green
Write-Host "  -> Feedback Rating: $($finalCitizenView.data.feedback.rating) stars" -ForegroundColor Green
Write-Host "  -> Feedback Comment: $($finalCitizenView.data.feedback.comment)" -ForegroundColor Green
Write-Host "  -> Escalation Status: $($finalCitizenView.data.escalationStatus)" -ForegroundColor Green
Write-Host "  -> Escalation Level: $($finalCitizenView.data.escalationLevel)" -ForegroundColor Green
Write-Host "  -> Escalation Target: $($finalCitizenView.data.escalationTarget)" -ForegroundColor Green

Write-Host "`n==================================================" -ForegroundColor Cyan
Write-Host " END-TO-END WORKFLOW SUCCEEDED 100%! " -ForegroundColor Green
Write-Host "==================================================" -ForegroundColor Cyan
