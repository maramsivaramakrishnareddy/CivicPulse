import React,{useState} from 'react';
import {NavLink,Outlet,useNavigate} from 'react-router-dom';
import api from '../../services/api';

export default function OfficerLayout({session,setSession}){
  const navigate=useNavigate(); const [loggingOut,setLoggingOut]=useState(false);
  const logout=async()=>{setLoggingOut(true);try{await api.logout();}catch(e){}finally{setSession?.({authenticated:false,userType:null,userId:null,userName:null,details:null});setLoggingOut(false);navigate('/');}};
  const links=[['/officer/dashboard','Dashboard'],['/officer/profile','Profile'],['/officer/complaints','Complaints'],['/officer/complaints-map','Complaints Map'],['/officer/duplicates','Duplicate Complaints'],['/officer/escalated','Escalated Complaints']];
  return <div className="dashboard-page"><div className="container officer-layout"><aside className="officer-sidebar"><div className="officer-sidebar-title">Officer Menu</div><nav className="officer-sidebar-nav">{links.map(([to,label])=><NavLink key={to} to={to} className={({isActive})=>`officer-sidebar-link ${isActive?'active':''}`}>{label}</NavLink>)}<button type="button" className="officer-sidebar-link officer-logout-link" onClick={logout} disabled={loggingOut}>{loggingOut?'Logging out...':'Logout'}</button></nav></aside><div className="officer-content"><Outlet context={{session}}/></div></div></div>;
}
