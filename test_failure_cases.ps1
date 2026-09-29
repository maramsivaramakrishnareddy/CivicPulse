$ErrorActionPreference = "Stop"
$baseUrl = "http://localhost:8080/api"
$citizenSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$offSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession

# Register and login citizen with unique email/aadhaar
$rand = Get-Date -Format "yyyyMMddHHmmss"
$regBody = @{ fullName="Test User"; aadhaarNumber="987654321012"; email="testfail_$rand@example.com"; phoneNumber="9876543210"; password="Test@123"; confirmPassword="Test@123"; address="Test" } | ConvertTo-Json
Invoke-RestMethod -Uri "$baseUrl/auth/citizen/register" -Method Post -Body $regBody -ContentType "application/json" -WebSession $citizenSession | Out-Null
$loginBody = @{ email="testfail_$rand@example.com"; password="Test@123" } | ConvertTo-Json
Invoke-RestMethod -Uri "$baseUrl/auth/citizen/login" -Method Post -Body $loginBody -ContentType "application/json" -WebSession $citizenSession | Out-Null

# Submit complaint
$compBody = @{ title="Test"; description="Test"; departmentName="Sanitation Department"; complaintType="NORMAL"; location="Test"; imageUrl="data:image/png;base64,AAA="; latitude=12.97; longitude=77.59 } | ConvertTo-Json
$compRes = Invoke-RestMethod -Uri "$baseUrl/citizen/complaints" -Method Post -Body $compBody -ContentType "application/json" -WebSession $citizenSession
$cid = $compRes.data.complaintId
Write-Host "Complaint: $cid"

# Try feedback on unresolved complaint - should fail
$fbBody = @{ rating=5; comment="test" } | ConvertTo-Json
try {
  Invoke-RestMethod -Uri "$baseUrl/complaints/$cid/feedback" -Method Post -Body $fbBody -ContentType "application/json" -WebSession $citizenSession
  Write-Host "FAIL: Should have thrown for unresolved complaint"
} catch {
  Write-Host "PASS: Unresolved feedback rejected: $($_.Exception.Message)"
}

# Login officer and resolve
$offLogin = @{ employeeId="EMP1001"; password="Officer@123" } | ConvertTo-Json
Invoke-RestMethod -Uri "$baseUrl/auth/officer/login" -Method Post -Body $offLogin -ContentType "application/json" -WebSession $offSession | Out-Null
$updBody = @{ status="COMPLAINT_RESOLVED"; remarks="Done" } | ConvertTo-Json
Invoke-RestMethod -Uri "$baseUrl/officer/my-complaints/$cid/status" -Method Put -Body $updBody -ContentType "application/json" -WebSession $offSession | Out-Null

# Submit feedback successfully
$fbRes = Invoke-RestMethod -Uri "$baseUrl/complaints/$cid/feedback" -Method Post -Body $fbBody -ContentType "application/json" -WebSession $citizenSession
Write-Host "PASS: First feedback submitted"

# Try duplicate feedback - should fail
try {
  Invoke-RestMethod -Uri "$baseUrl/complaints/$cid/feedback" -Method Post -Body $fbBody -ContentType "application/json" -WebSession $citizenSession
  Write-Host "FAIL: Should have thrown for duplicate feedback"
} catch {
  Write-Host "PASS: Duplicate feedback rejected: $($_.Exception.Message)"
}

# Try officer submitting feedback - should fail
try {
  Invoke-RestMethod -Uri "$baseUrl/complaints/$cid/feedback" -Method Post -Body $fbBody -ContentType "application/json" -WebSession $offSession
  Write-Host "FAIL: Should have thrown for officer feedback"
} catch {
  Write-Host "PASS: Officer feedback rejected: $($_.Exception.Message)"
}

# Escalate complaint
$escBody = @{ reason="Test escalation"; escalationLevel=1; remarks="Test"; target="Chief Engineer" } | ConvertTo-Json
$escRes = Invoke-RestMethod -Uri "$baseUrl/officer/my-complaints/$cid/escalate" -Method Post -Body $escBody -ContentType "application/json" -WebSession $offSession
$escalationId = $escRes.data.latestEscalation.escalationId
Write-Host "PASS: Escalation created, ID: $escalationId"

# Create a second officer session to test unauthorized escalation update
$offSession2 = New-Object Microsoft.PowerShell.Commands.WebRequestSession
# Register a second officer manually in DB for this test, or try to use the same officer but different session
# Actually, let's test by creating a second citizen and trying to update escalation as citizen (should fail)
$citizenSession2 = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$regBody2 = @{ fullName="Test User 2"; aadhaarNumber="987654321013"; email="testfail2_$rand@example.com"; phoneNumber="9876543210"; password="Test@123"; confirmPassword="Test@123"; address="Test" } | ConvertTo-Json
Invoke-RestMethod -Uri "$baseUrl/auth/citizen/register" -Method Post -Body $regBody2 -ContentType "application/json" -WebSession $citizenSession2 | Out-Null
$loginBody2 = @{ email="testfail2_$rand@example.com"; password="Test@123" } | ConvertTo-Json
Invoke-RestMethod -Uri "$baseUrl/auth/citizen/login" -Method Post -Body $loginBody2 -ContentType "application/json" -WebSession $citizenSession2 | Out-Null

# Citizen tries to update escalation - should fail
try {
  Invoke-RestMethod -Uri "$baseUrl/officer/escalations/$escalationId/status" -Method Put -Body (@{ status="RESOLVED"; remarks="Hacked" } | ConvertTo-Json) -ContentType "application/json" -WebSession $citizenSession2
  Write-Host "FAIL: Should have thrown for citizen escalation update"
} catch {
  Write-Host "PASS: Citizen escalation update rejected: $($_.Exception.Message)"
}

Write-Host "All failure case tests completed."
