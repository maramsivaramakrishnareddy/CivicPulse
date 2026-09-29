import React, { useEffect, useRef, useState } from 'react';

export default function LivePhotoCapture({ value, onChange, required = false }) {
  const videoRef = useRef(null);
  const streamRef = useRef(null);
  const [open, setOpen] = useState(false);
  const [videoReady, setVideoReady] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  const resolveAddress = async (latitude, longitude) => {
    const controller = new AbortController();
    const timeout = window.setTimeout(() => controller.abort(), 12000);
    try {
      const response = await fetch(`https://nominatim.openstreetmap.org/reverse?format=jsonv2&addressdetails=1&zoom=18&lat=${latitude}&lon=${longitude}`, {
        headers: { Accept: 'application/json' },
        signal: controller.signal,
      });
      if (!response.ok) throw new Error('Address lookup failed');
      const data = await response.json();
      const address = data.address || {};
      const place = [address.city, address.town, address.village, address.municipality]
        .find(Boolean) || address.county || 'Location captured';
      const lines = [
        [address.house_number, address.road].filter(Boolean).join(' '),
        [address.neighbourhood, address.suburb, address.city_district].filter(Boolean).join(', '),
        [place, address.state, address.postcode, address.country].filter(Boolean).join(', '),
      ].filter(Boolean);
      return { place, address: lines.join(' | ') };
    } finally {
      window.clearTimeout(timeout);
    }
  };

  useEffect(() => () => streamRef.current?.getTracks().forEach((track) => track.stop()), []);

  useEffect(() => {
    if (!open || !videoRef.current || !streamRef.current) return;
    videoRef.current.srcObject = streamRef.current;
    videoRef.current.play().catch(() => setError('Unable to start the camera preview.'));
  }, [open]);

  const openCamera = async () => {
    if (!navigator.mediaDevices?.getUserMedia) {
      setError('Live camera capture is not supported by this browser.');
      return;
    }
    try {
      setError('');
      const stream = await navigator.mediaDevices.getUserMedia({ video: { facingMode: 'environment' }, audio: false });
      streamRef.current = stream;
      setVideoReady(false);
      setOpen(true);
    } catch {
      setError('Camera access is required. Please allow camera permission and try again.');
    }
  };

  const capture = () => {
    if (!videoRef.current || !videoReady || !navigator.geolocation) {
      setError('Camera and location access are required for a verified evidence photo.');
      return;
    }
    setError('');
    setBusy(true);
    navigator.geolocation.getCurrentPosition(async (position) => {
      const latitude = Number(position.coords.latitude.toFixed(6));
      const longitude = Number(position.coords.longitude.toFixed(6));
      let resolvedAddress;
      try {
        resolvedAddress = await resolveAddress(latitude, longitude);
        if (!resolvedAddress?.address) throw new Error('No address found');
      } catch {
        // Reverse geocoding (needs internet to reach Nominatim) failed or timed out -
        // don't block the whole capture. Fall back to raw GPS coordinates as the
        // location text so latitude/longitude/location are still always captured.
        resolvedAddress = { place: 'GPS location', address: `Lat ${latitude}, Long ${longitude}` };
      }
      const canvas = document.createElement('canvas');
      canvas.width = videoRef.current.videoWidth;
      canvas.height = videoRef.current.videoHeight;
      if (!canvas.width || !canvas.height) {
        setError('Camera is still starting. Please wait a moment and try again.');
        setBusy(false);
        return;
      }
      const context = canvas.getContext('2d');
      context.drawImage(videoRef.current, 0, 0, canvas.width, canvas.height);
      const overlayHeight = Math.max(174, canvas.height * 0.32);
      context.fillStyle = 'rgba(8, 20, 31, 0.86)';
      context.fillRect(0, canvas.height - overlayHeight, canvas.width, overlayHeight);
      context.fillStyle = '#ffffff';
      const textWidth = canvas.width - 48;
      const wrap = (text, font, maxLines = 2) => {
        context.font = font;
        const words = text.split(' ');
        const lines = [];
        let line = '';
        words.forEach((word) => {
          const next = line ? `${line} ${word}` : word;
          if (context.measureText(next).width > textWidth && line) {
            lines.push(line);
            line = word;
          } else line = next;
        });
        if (line) lines.push(line);
        return lines.slice(0, maxLines);
      };
      context.font = `bold ${Math.max(18, canvas.width * 0.035)}px sans-serif`;
      context.fillText(`GPS MAP CAMERA  |  ${resolvedAddress.place}`, 24, canvas.height - overlayHeight + 30);
      context.font = `${Math.max(13, canvas.width * 0.022)}px sans-serif`;
      const addressLines = resolvedAddress.address.split(' | ').flatMap((line) => wrap(line, context.font));
      addressLines.slice(0, 3).forEach((line, index) => context.fillText(line, 24, canvas.height - overlayHeight + 56 + (index * 23)));
      context.fillStyle = '#b7f7d8';
      const coordinateY = canvas.height - overlayHeight + 56 + (Math.min(addressLines.length, 3) * 23);
      context.fillText(`Lat ${latitude}  Long ${longitude}`, 24, coordinateY);
      context.fillText(`Captured ${new Date().toLocaleString()}`, 24, coordinateY + 23);
      onChange({
        ...value,
        imageUrl: canvas.toDataURL('image/jpeg', 0.88),
        latitude,
        longitude,
        location: resolvedAddress.address,
      });
      streamRef.current?.getTracks().forEach((track) => track.stop());
      streamRef.current = null;
      setVideoReady(false);
      setOpen(false);
      setBusy(false);
    }, (locationError) => {
      setError(`Location is required for this photo: ${locationError.message}`);
      setBusy(false);
    }, { enableHighAccuracy: true, timeout: 20000, maximumAge: 0 });
  };

  const removePhoto = () => onChange({ ...value, imageUrl: '' });

  const cancel = () => {
    streamRef.current?.getTracks().forEach((track) => track.stop());
    streamRef.current = null;
    setVideoReady(false);
    setOpen(false);
  };

  return (
    <div className="live-photo-capture">
      <label>Photo Evidence {required ? '*' : '(Optional)'}</label>
      <small>Live camera only — location is captured automatically with the photo, so gallery uploads aren't allowed (they'd have no location and could be old or unrelated).</small>
      {!open && <div className="evidence-actions">
        <button type="button" className="secondary-button" onClick={openCamera}>📷 Open camera</button>
      </div>}
      {open && <>
        <video ref={videoRef} className="evidence-video" autoPlay muted playsInline onLoadedMetadata={() => setVideoReady(true)} />
        {!videoReady && <small>Connecting to camera preview...</small>}
        <div className="evidence-actions">
          <button type="button" className="primary-button" onClick={capture} disabled={busy || !videoReady}>{busy ? 'Capturing...' : videoReady ? 'Capture photo' : 'Starting camera...'}</button>
          <button type="button" className="secondary-button" onClick={cancel}>Cancel</button>
        </div>
      </>}
      {value.imageUrl && <div>
        <img className="evidence-preview" src={value.imageUrl} alt="Complaint evidence" />
        <button type="button" className="secondary-button" onClick={removePhoto}>Remove photo</button>
      </div>}
      {value.location && <small className="evidence-confirmed">Address: {value.location}</small>}
      {error && <span className="error-text">{error}</span>}
    </div>
  );
}
