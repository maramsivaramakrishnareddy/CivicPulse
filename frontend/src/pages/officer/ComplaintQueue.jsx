import React, { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../../services/api';

const STATUS_META = {
  APPLIED: ['Applied','officer-status-pending'], COMPLAINT_VIEWED: ['Viewed','officer-status-pending'], ACTION_TO_BE_TAKEN: ['In Progress','officer-status-progress'],
  COMPLAINT_RESOLVED: ['Resolved','officer-status-resolved'], REOPENED: ['Reopened','officer-status-reopened'], DUPLICATE: ['Duplicate','officer-status-duplicate'], REJECTED: ['Rejected','officer-status-rejected']
};
export default function ComplaintQueue() {
  const [complaints,setComplaints]=useState([]); const [loading,setLoading]=useState(true); const [error,setError]=useState(''); const [query,setQuery]=useState(''); const [status,setStatus]=useState('ALL');
  const load=async()=>{setLoading(true);setError('');try{const r=await api.getMyComplaints();setComplaints(r.data||[]);}catch(e){setError(e.message||'Unable to load complaints.');}finally{setLoading(false);}};
  // Load once on mount. No automatic polling/setInterval here: refreshing on a
  // timer causes the whole list to flash back to a loading state and can wipe
  // out whatever the officer is doing (search text, selected filter, etc).
  useEffect(()=>{load();},[]);
  const filtered=useMemo(()=>complaints.filter(c=>{const q=query.trim().toLowerCase(); const matches=!q||(c.complaintId||'').toLowerCase().includes(q)||(c.title||'').toLowerCase().includes(q); return matches&&(status==='ALL'||c.status===status);}),[complaints,query,status]);
  return <>
    <div className="dashboard-header dashboard-header-officer"><div className="dashboard-welcome"><span className="dashboard-role-badge">Officer Complaints</span><h1 className="dashboard-title">Complaints</h1><p className="dashboard-subtitle">Actual complaints for your department, including newly registered and unassigned citizen complaints.</p></div></div>
    {error&&<div className="alert alert-error">{error}</div>}
    <div className="dashboard-card officer-complaints"><div className="officer-filter-row"><input value={query} onChange={e=>setQuery(e.target.value)} placeholder="Search complaint ID or title..."/><select value={status} onChange={e=>setStatus(e.target.value)}><option value="ALL">All Statuses</option><option value="APPLIED">Applied</option><option value="COMPLAINT_VIEWED">Viewed</option><option value="ACTION_TO_BE_TAKEN">In Progress</option><option value="COMPLAINT_RESOLVED">Resolved</option><option value="REOPENED">Reopened</option><option value="DUPLICATE">Duplicate</option><option value="REJECTED">Rejected</option></select></div>
      {loading?<p>Loading complaints...</p>:filtered.length===0?<p>No complaints found.</p>:<div className="table-wrapper"><table className="data-table"><thead><tr><th>ID</th><th>Title</th><th>Department</th><th>Priority</th><th>Location</th><th>Status</th><th>Submitted</th><th>Action</th></tr></thead><tbody>{filtered.map(c=>{const m=STATUS_META[c.status]||[c.status,'officer-status-pending'];return <tr key={c.complaintId}><td>{c.complaintId}</td><td>{c.title}{c.aiPhotoMismatchSuspected&&<span title={`AI flagged photo mismatch${c.aiPhotoMismatchReason?': '+c.aiPhotoMismatchReason:''}`} style={{marginLeft:'6px'}}>⚠️</span>}</td><td>{c.departmentName||'—'}</td><td><span className={`officer-priority ${c.complaintType==='EMERGENCY'?'emergency':''}`}>{c.complaintType==='EMERGENCY'?'Emergency':'Normal'}</span></td><td>{c.location||'—'}</td><td><span className={`officer-status-pill ${m[1]}`}>{m[0]}</span></td><td>{c.createdAt?new Date(c.createdAt).toLocaleString():'—'}</td><td><Link to={`/officer/complaints/${c.complaintId}`} className="secondary-button" style={{textDecoration:'none'}}>View</Link></td></tr>})}</tbody></table></div>}
    </div>
  </>;
}
