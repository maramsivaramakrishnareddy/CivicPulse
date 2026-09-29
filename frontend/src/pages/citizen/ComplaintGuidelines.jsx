import React from 'react';
import { useNavigate } from 'react-router-dom';
import './ComplaintGuidelines.css';

function ComplaintGuidelines() {
  const navigate = useNavigate();
  return (
    <div className="complaint-guidelines-container">
      <h1>Complaint Guidelines</h1>
      <p className="subtitle">Learn how to file an effective complaint</p>

      <div className="guidelines-content">
        <section className="guideline-section">
          <h2>1. Before You Start</h2>
          <ul>
            <li>Make sure you have a clear understanding of the issue</li>
            <li>Gather any relevant documents or evidence</li>
            <li>Note the date and time of the incident</li>
            <li>Identify the responsible department</li>
          </ul>
        </section>

        <section className="guideline-section">
          <h2>2. Required Information</h2>
          <ul>
            <li><strong>Title:</strong> A brief, clear description of your issue (max 200 characters)</li>
            <li><strong>Description:</strong> Detailed explanation of what happened and why you believe it's a problem</li>
            <li><strong>Department:</strong> Select the department responsible for the issue</li>
            <li><strong>Location:</strong> Specify where the issue occurred</li>
            <li><strong>Complaint Type:</strong> Mark as EMERGENCY only if it's urgent and requires immediate attention</li>
          </ul>
        </section>

        <section className="guideline-section">
          <h2>3. Supported Media</h2>
          <ul>
            <li><strong>Photo (required):</strong> Take a live photo using your camera — gallery uploads aren't allowed. Location is captured automatically with it.</li>
            <li><strong>Video (optional):</strong> Record a live video for additional evidence.</li>
            <li><strong>Location:</strong> Captured automatically from your live photo's GPS.</li>
          </ul>
        </section>

        <section className="guideline-section">
          <h2>4. Complaint Categories</h2>
          <div className="categories-list">
            <div className="category">
              <h3>Sanitation Department</h3>
              <p>Issues related to waste management, street cleaning, and hygiene</p>
            </div>
            <div className="category">
              <h3>Water Supply Department</h3>
              <p>Issues with water supply, leaks, and water quality</p>
            </div>
            <div className="category">
              <h3>Road Maintenance Department</h3>
              <p>Issues with pothole, road damage, or infrastructure</p>
            </div>
            <div className="category">
              <h3>Public Health Department</h3>
              <p>Issues related to public health and sanitation</p>
            </div>
            <div className="category">
              <h3>Electricity Department</h3>
              <p>Issues with power supply, street lights, and electrical infrastructure</p>
            </div>
            <div className="category">
              <h3>Traffic & Transport Department</h3>
              <p>Traffic signals, illegal parking, and public transport issues</p>
            </div>
            <div className="category">
              <h3>Parks & Horticulture Department</h3>
              <p>Park maintenance, trees, and public green spaces</p>
            </div>
            <div className="category">
              <h3>Public Safety Department</h3>
              <p>Unsafe areas, broken safety infrastructure, and public hazards</p>
            </div>
            <div className="category">
              <h3>Drainage & Sewerage Department</h3>
              <p>Blocked drains, sewage overflow, and stormwater issues</p>
            </div>
            <div className="category">
              <h3>Street Lighting Department</h3>
              <p>Non-functional or damaged streetlights</p>
            </div>
            <div className="category">
              <h3>Building & Construction Department</h3>
              <p>Illegal construction and building safety violations</p>
            </div>
            <div className="category">
              <h3>Animal Control Department</h3>
              <p>Stray animal issues and animal-related public safety</p>
            </div>
          </div>
        </section>

        <section className="guideline-section">
          <h2>5. What Happens After Filing</h2>
          <ol>
            <li><strong>Application Received:</strong> Your complaint is registered in the system</li>
            <li><strong>Complaint Viewed:</strong> An officer reviews your complaint</li>
            <li><strong>Action to be Taken:</strong> The department prepares a response</li>
            <li><strong>Resolved:</strong> The issue has been addressed or action completed</li>
            <li><strong>Feedback:</strong> You can provide feedback on the resolution</li>
            <li><strong>Reopen (if needed):</strong> If not satisfied, you can reopen the complaint</li>
          </ol>
        </section>

        <section className="guideline-section">
          <h2>6. Emergency Complaints</h2>
          <p>Mark your complaint as EMERGENCY if:</p>
          <ul>
            <li>There is an immediate threat to public health or safety</li>
            <li>The issue is causing significant disruption to community services</li>
            <li>There is an ongoing hazard that needs urgent attention</li>
          </ul>
          <p><strong>Note:</strong> Emergency complaints receive priority and faster response time.</p>
        </section>

        <section className="guideline-section">
          <h2>7. Tips for Better Complaints</h2>
          <ul>
            <li>Be specific and factual; avoid opinions or accusations</li>
            <li>Include relevant dates and times</li>
            <li>Take a clear live photo — it's required and captures your location automatically</li>
            <li>Attach a video too if it helps show the problem (optional)</li>
            <li>Provide accurate location information</li>
            <li>Be courteous in your language and tone</li>
            <li>Follow up on your complaint status using the tracking feature</li>
          </ul>
        </section>

        <section className="guideline-section">
          <h2>8. Escalation & Reopen Options</h2>
          <ul>
            <li><strong>Escalation:</strong> If you're not satisfied with the response, officers can escalate to higher levels</li>
            <li><strong>Reopen:</strong> After a complaint is resolved, you can request reopening with additional evidence or if the issue persists</li>
          </ul>
        </section>
      </div>

      <div className="guidelines-proceed-bar">
        <button
          type="button"
          className="primary-button"
          onClick={() => navigate('/citizen/complaints/new')}
        >
          I Understand — Proceed to File Complaint
        </button>
      </div>
    </div>
  );
}

export default ComplaintGuidelines;
