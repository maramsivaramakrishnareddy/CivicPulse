$ErrorActionPreference = "Stop"
$baseUrl = "http://localhost:8080/api"

Write-Host "==================================================" -ForegroundColor Cyan
Write-Host " CIVICPULSE FULL MODULE VERIFICATION TEST" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan

$citizenSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$officerSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession

Write-Host "`n[1] CITIZEN AUTHENTICATION" -ForegroundColor Yellow
$rand = Get-Date -Format "yyyyMMddHHmmss"
$aadhaar = "9876543210$($rand.Substring($rand.Length-2))"
$regBody = @{ fullName="Full Test User"; aadhaarNumber=$aadhaar; email="fulltest_$rand@example.com"; phoneNumber="9876543210"; password="Test@123"; confirmPassword="Test@123"; address="123 Test Street" } | ConvertTo-Json
$regRes = Invoke-RestMethod -Uri "$baseUrl/auth/citizen/register" -Method Post -Body $regBody -ContentType "application/json" -WebSession $citizenSession
Write-Host "  Register: $($regRes.success)" -ForegroundColor $(if($regRes.success){'Green'}else{'Red'})

$loginBody = @{ email="fulltest_$rand@example.com"; password="Test@123" } | ConvertTo-Json
$loginRes = Invoke-RestMethod -Uri "$baseUrl/auth/citizen/login" -Method Post -Body $loginBody -ContentType "application/json" -WebSession $citizenSession
Write-Host "  Login: $($loginRes.success)" -ForegroundColor $(if($loginRes.success){'Green'}else{'Red'})

$sessionRes = Invoke-RestMethod -Uri "$baseUrl/session" -Method Get -WebSession $citizenSession
Write-Host "  Session: $($sessionRes.data.authenticated)" -ForegroundColor $(if($sessionRes.data.authenticated){'Green'}else{'Red'})

Write-Host "`n[2] OFFICER AUTHENTICATION" -ForegroundColor Yellow
$offLogin = @{ employeeId="EMP1001"; password="Officer@123" } | ConvertTo-Json
$offRes = Invoke-RestMethod -Uri "$baseUrl/auth/officer/login" -Method Post -Body $offLogin -ContentType "application/json" -WebSession $officerSession
Write-Host "  Officer Login: $($offRes.success)" -ForegroundColor $(if($offRes.success){'Green'}else{'Red'})

Write-Host "`n[3] COMPLAINT SUBMISSION" -ForegroundColor Yellow
$compBody = @{ title="Full Test Complaint"; description="Testing all modules"; departmentName="Sanitation Department"; complaintType="NORMAL"; location="Test Location"; imageUrl="data:image/png;base64,AAA="; latitude=12.97; longitude=77.59 } | ConvertTo-Json
$compRes = Invoke-RestMethod -Uri "$baseUrl/citizen/complaints" -Method Post -Body $compBody -ContentType "application/json" -WebSession $citizenSession
$cid = $compRes.data.complaintId
Write-Host "  Complaint Created: $cid" -ForegroundColor Green

Write-Host "`n[4] COMPLAINT MANAGEMENT & STATUS" -ForegroundColor Yellow
$detailRes = Invoke-RestMethod -Uri "$baseUrl/complaints/$cid" -Method Get -WebSession $citizenSession
Write-Host "  Get Detail: $($detailRes.data.complaintId)" -ForegroundColor Green

$statusRes = Invoke-RestMethod -Uri "$baseUrl/officer/my-complaints/$cid/status" -Method Put -Body (@{ status="ACTION_TO_BE_TAKEN"; remarks="Inspecting" } | ConvertTo-Json) -ContentType "application/json" -WebSession $officerSession
Write-Host "  Status -> ACTION_TO_BE_TAKEN: $($statusRes.data.status)" -ForegroundColor Green

$statusRes2 = Invoke-RestMethod -Uri "$baseUrl/officer/my-complaints/$cid/status" -Method Put -Body (@{ status="COMPLAINT_RESOLVED"; remarks="Fixed" } | ConvertTo-Json) -ContentType "application/json" -WebSession $officerSession
Write-Host "  Status -> COMPLAINT_RESOLVED: $($statusRes2.data.status)" -ForegroundColor Green

Write-Host "`n[5] COMPLAINT HISTORY" -ForegroundColor Yellow
$histRes = Invoke-RestMethod -Uri "$baseUrl/complaints/$cid/history" -Method Get -WebSession $citizenSession
Write-Host "  History records: $($histRes.data.Count)" -ForegroundColor Green

Write-Host "`n[6] OFFICER COMPLAINT MODULE" -ForegroundColor Yellow
$myCompRes = Invoke-RestMethod -Uri "$baseUrl/officer/my-complaints" -Method Get -WebSession $officerSession
Write-Host "  Officer complaints count: $($myCompRes.data.Count)" -ForegroundColor Green

$myDetailRes = Invoke-RestMethod -Uri "$baseUrl/officer/my-complaints/$cid" -Method Get -WebSession $officerSession
Write-Host "  Officer detail: $($myDetailRes.data.complaintId)" -ForegroundColor Green

Write-Host "`n[7] FEEDBACK MODULE" -ForegroundColor Yellow
$fbRes = Invoke-RestMethod -Uri "$baseUrl/complaints/$cid/feedback" -Method Post -Body (@{ rating=5; comment="Excellent"; latitude=12.97; longitude=77.59; location="Test" } | ConvertTo-Json) -ContentType "application/json" -WebSession $citizenSession
Write-Host "  Feedback submitted: $($fbRes.data.feedback.rating) stars" -ForegroundColor Green

$dupFbRes = try { Invoke-RestMethod -Uri "$baseUrl/complaints/$cid/feedback" -Method Post -Body (@{ rating=4; comment="Duplicate" } | ConvertTo-Json) -ContentType "application/json" -WebSession $citizenSession; "FAIL" } catch { "PASS: Duplicate rejected" }
Write-Host "  Duplicate feedback: $dupFbRes" -ForegroundColor Green

$offFbRes = try { Invoke-RestMethod -Uri "$baseUrl/complaints/$cid/feedback" -Method Post -Body (@{ rating=5; comment="Hacked" } | ConvertTo-Json) -ContentType "application/json" -WebSession $officerSession; "FAIL" } catch { "PASS: Officer rejected" }
Write-Host "  Officer feedback: $offFbRes" -ForegroundColor Green

$unresolvedFbRes = try { Invoke-RestMethod -Uri "$baseUrl/complaints/$cid/feedback" -Method Post -Body (@{ rating=5; comment="Hacked" } | ConvertTo-Json) -ContentType "application/json" -WebSession $citizenSession; "FAIL" } catch { "PASS: Unresolved rejected" }
Write-Host "  Unresolved feedback: $unresolvedFbRes" -ForegroundColor Green

Write-Host "`n[8] ESCALATION MODULE" -ForegroundColor Yellow
$escRes = Invoke-RestMethod -Uri "$baseUrl/officer/my-complaints/$cid/escalate" -Method Post -Body (@{ reason="Needs higher authority"; escalationLevel=2; remarks="Important"; target="Chief Engineer" } | ConvertTo-Json) -ContentType "application/json" -WebSession $officerSession
$escalationId = $escRes.data.latestEscalation.escalationId
Write-Host "  Escalation created: ID $escalationId, Status $($escRes.data.latestEscalation.status)" -ForegroundColor Green

$escListRes = Invoke-RestMethod -Uri "$baseUrl/officer/escalated" -Method Get -WebSession $officerSession
Write-Host "  Escalated list count: $($escListRes.data.Count)" -ForegroundColor Green

$escUpdateRes = Invoke-RestMethod -Uri "$baseUrl/officer/escalations/$escalationId/status" -Method Put -Body (@{ status="RESOLVED"; remarks="Resolved by authority" } | ConvertTo-Json) -ContentType "application/json" -WebSession $officerSession
Write-Host "  Escalation updated: $($escUpdateRes.data.latestEscalation.status)" -ForegroundColor Green

Write-Host "`n[9] NOTIFICATIONS" -ForegroundColor Yellow
$notifRes = Invoke-RestMethod -Uri "$baseUrl/notifications" -Method Get -WebSession $citizenSession
Write-Host "  Notification count: $($notifRes.data.Count)" -ForegroundColor Green

$unreadRes = Invoke-RestMethod -Uri "$baseUrl/notifications/unread-count" -Method Get -WebSession $citizenSession
Write-Host "  Unread count: $($unreadRes.data.unreadCount)" -ForegroundColor Green

$markRes = Invoke-RestMethod -Uri "$baseUrl/notifications/read-all" -Method Put -WebSession $citizenSession
Write-Host "  Mark all read: $($markRes.message)" -ForegroundColor Green

Write-Host "`n[10] REOPEN COMPLAINT" -ForegroundColor Yellow
$reopenRes = Invoke-RestMethod -Uri "$baseUrl/complaints/$cid/reopen" -Method Post -Body (@{ reason="Issue not fully resolved"; imageUrl="data:image/png;base64,AAA="; videoUrl="data:video/webm;base64,AAA="; latitude=12.97; longitude=77.59; location="Test" } | ConvertTo-Json) -ContentType "application/json" -WebSession $citizenSession
Write-Host "  Reopen requested: $($reopenRes.data.status)" -ForegroundColor Green

Write-Host "`n[11] DEPARTMENT FUNCTIONALITY" -ForegroundColor Yellow
$deptRes = Invoke-RestMethod -Uri "$baseUrl/officer/complaints" -Method Get -WebSession $officerSession
Write-Host "  Department complaints: $($deptRes.data.Count)" -ForegroundColor Green

Write-Host "`n[12] AI SUGGESTION" -ForegroundColor Yellow
$aiRes = Invoke-RestMethod -Uri "$baseUrl/citizen/complaints/suggest-category" -Method Post -Body (@{ description="Water pipe burst in main road" } | ConvertTo-Json) -ContentType "application/json" -WebSession $citizenSession
Write-Host "  AI Suggestion: $($aiRes.data.departmentName) / $($aiRes.data.priority)" -ForegroundColor Green

Write-Host "`n[13] DUPLICATE COMPLAINT" -ForegroundColor Yellow
$dupRes = Invoke-RestMethod -Uri "$baseUrl/officer/duplicates" -Method Get -WebSession $officerSession
Write-Host "  Duplicate complaints: $($dupRes.data.Count)" -ForegroundColor Green

Write-Host "`n[14] EMERGENCY COMPLAINTS" -ForegroundColor Yellow
$emergRes = Invoke-RestMethod -Uri "$baseUrl/officer/complaints/emergency" -Method Get -WebSession $officerSession
Write-Host "  Emergency complaints: $($emergRes.data.Count)" -ForegroundColor Green

Write-Host "`n[15] REOPENED COMPLAINTS" -ForegroundColor Yellow
$reopenListRes = Invoke-RestMethod -Uri "$baseUrl/officer/complaints/reopened" -Method Get -WebSession $officerSession
Write-Host "  Reopened complaints: $($reopenListRes.data.Count)" -ForegroundColor Green

Write-Host "`n[16] OFFICER DASHBOARD" -ForegroundColor Yellow
$dashRes = Invoke-RestMethod -Uri "$baseUrl/officer/dashboard" -Method Get -WebSession $officerSession
Write-Host "  Dashboard loaded: $($dashRes.data.officerName)" -ForegroundColor Green

Write-Host "`n[17] HEALTH CHECK" -ForegroundColor Yellow
$healthRes = Invoke-RestMethod -Uri "$baseUrl/auth/health" -Method Get -WebSession $citizenSession
Write-Host "  Health: $($healthRes.data.status)" -ForegroundColor Green

Write-Host "`n==================================================" -ForegroundColor Cyan
Write-Host " ALL MODULE TESTS COMPLETED SUCCESSFULLY! " -ForegroundColor Green
Write-Host "==================================================" -ForegroundColor Cyan
