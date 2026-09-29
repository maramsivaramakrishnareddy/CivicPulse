import React, { useEffect, useState } from 'react';
import { MapContainer, TileLayer, CircleMarker, Popup, Tooltip, useMap } from 'react-leaflet';
import L from 'leaflet';
import api from '../../services/api';

// Shows all complaints visible to the logged-in officer's department on a map,
// including newly registered and currently unassigned citizen complaints.
// Uses Leaflet + OpenStreetMap - free, no API key needed.
const priorityColor = { HIGH: '#c0392b', MEDIUM: '#e67e22', LOW: '#27ae60' };

// Automatically zooms/pans the map so every complaint marker is visible, instead of a
// fixed zoom centered only on the first complaint (which could leave others off-screen).
function FitBounds({ points }) {
  const map = useMap();
  useEffect(() => {
    if (!points.length) return;
    if (points.length === 1) {
      map.setView(points[0], 15);
      return;
    }
    const bounds = L.latLngBounds(points);
    map.fitBounds(bounds, { padding: [40, 40] });
  }, [points, map]);
  return null;
}

export default function OfficerComplaintsMap() {
  const [complaints, setComplaints] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let alive = true;
    const load = async () => {
      try {
        const res = await api.getMyComplaints();
        const list = Array.isArray(res.data) ? res.data : (res.complaints || []);
        if (alive) {
          setComplaints(list.filter((c) => Number.isFinite(Number(c.latitude)) && Number.isFinite(Number(c.longitude))));
          setError('');
        }
      } catch (err) {
        if (alive) setError(err.message || 'Failed to load complaints');
      } finally {
        if (alive) setLoading(false);
      }
    };
    // Load once on mount. No automatic polling/setInterval here: a timer-driven
    // reload would repeatedly rebuild the map (re-fitting bounds, resetting pan/zoom)
    // while the officer is looking at it.
    load();
    return () => { alive = false; };
  }, []);

  const points = complaints.map((c) => [Number(c.latitude), Number(c.longitude)]);
  const fallbackCenter = [20.5937, 78.9629]; // fallback: center of India, only used with zero complaints

  return (
    <div className="dashboard-card">
      <h2 className="dashboard-card-title">Complaints Map — My Department</h2>
      <p style={{ fontSize: '0.9rem', color: '#555' }}>
        <span style={{ color: '#c0392b' }}>●</span> High &nbsp;
        <span style={{ color: '#e67e22' }}>●</span> Medium &nbsp;
        <span style={{ color: '#27ae60' }}>●</span> Low
        &nbsp;— based on AI-detected priority where available. {complaints.length} complaint(s) with location shown below.
      </p>

      {loading && <p>Loading map...</p>}
      {error && <div className="alert alert-error">{error}</div>}

      {!loading && !error && (
        <div style={{ height: '480px', borderRadius: '10px', overflow: 'hidden' }}>
          <MapContainer center={fallbackCenter} zoom={5} style={{ height: '100%', width: '100%' }}>
            <TileLayer
              attribution='&copy; OpenStreetMap contributors'
              url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            />
            <FitBounds points={points} />
            {complaints.map((c) => (
              <CircleMarker
                key={c.complaintId}
                center={[Number(c.latitude), Number(c.longitude)]}
                radius={12}
                pathOptions={{
                  color: '#1a1a1a',
                  weight: 2,
                  fillColor: priorityColor[c.aiPriority] || '#3388ff',
                  fillOpacity: 0.85,
                }}
              >
                <Tooltip direction="top" offset={[0, -8]} permanent={complaints.length <= 5}>
                  {c.title}
                </Tooltip>
                <Popup>
                  <b>{c.complaintId}: {c.title}</b><br />
                  {c.location || 'Location not provided'}<br />
                  Status: {c.status}<br />
                  Type: {c.complaintType || 'NORMAL'}<br />
                  {c.aiPriority && <>AI Priority: {c.aiPriority}<br /></>}
                  {c.aiPhotoMismatchSuspected && <span style={{ color: '#c0392b' }}>⚠️ Photo mismatch flagged</span>}
                </Popup>
              </CircleMarker>
            ))}
          </MapContainer>
        </div>
      )}

      {!loading && !error && complaints.length === 0 && (
        <p>No complaints with valid location data are available for your department yet.</p>
      )}
    </div>
  );
}
