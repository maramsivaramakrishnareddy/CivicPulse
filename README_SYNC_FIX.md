# CivicPulse synchronization fix

Citizen and Officer modules use the same MySQL database and the same `Complaint` entity.

## Synchronization rules
- Citizen-created complaints are stored with their selected `Department`.
- Officer queue/dashboard/map read all complaints for the logged-in officer department, including unassigned complaints.
- Officer status/remarks updates modify that same complaint row and create complaint history.
- Citizen dashboard/detail poll every 5 seconds so officer changes become visible without a manual refresh.
- Officer dashboard/queue/map/review also poll every 5 seconds so newly registered/reopened complaints appear automatically.
- Department matching is case-insensitive and trims surrounding whitespace.

## Run
Backend: `mvn spring-boot:run` on port 8090.
Frontend: `npm run dev` on port 5173.
