import React, { useEffect, useRef, useState } from 'react';
import LivePhotoCapture from './LivePhotoCapture';

export default function LiveEvidenceCapture({ value, onChange, includePhoto = true }) {
  const videoRef = useRef(null);
  const canvasRef = useRef(null);
  const streamRef = useRef(null);
  const recorderRef = useRef(null);
  const animationRef = useRef(null);
  const previewUrlRef = useRef('');
  const [cameraOpen, setCameraOpen] = useState(false);
  const [videoReady, setVideoReady] = useState(false);
  const [recording, setRecording] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => () => {
    streamRef.current?.getTracks().forEach((track) => track.stop());
    if (previewUrlRef.current) URL.revokeObjectURL(previewUrlRef.current);
  }, []);

  useEffect(() => {
    if (!cameraOpen || !videoRef.current || !streamRef.current) return;
    videoRef.current.srcObject = streamRef.current;
    videoRef.current.play().catch(() => setError('Unable to start the video preview.'));
  }, [cameraOpen]);

  const openCamera = async () => {
    if (!navigator.mediaDevices?.getUserMedia || !window.MediaRecorder) {
      setError('Live video capture is not supported by this browser.');
      return;
    }
    try {
      setError('');
      const stream = await navigator.mediaDevices.getUserMedia({ video: { facingMode: 'environment' }, audio: true });
      streamRef.current = stream;
      setVideoReady(false);
      setCameraOpen(true);
    } catch {
      setError('Camera and microphone access are required for live video evidence.');
    }
  };

  const startRecording = () => {
    const video = videoRef.current;
    const canvas = canvasRef.current;
    if (!video || !videoReady || !video.videoWidth || !video.videoHeight) {
      setError('Camera preview is still starting. Please wait a moment.');
      return;
    }
    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;
    const context = canvas.getContext('2d');
    const capturedAt = new Date().toLocaleString();
    const drawFrame = () => {
      context.drawImage(video, 0, 0, canvas.width, canvas.height);
      const overlayHeight = Math.max(154, canvas.height * 0.29);
      context.fillStyle = 'rgba(8, 20, 31, 0.86)';
      context.fillRect(0, canvas.height - overlayHeight, canvas.width, overlayHeight);
      context.fillStyle = '#ffffff';
      const textWidth = canvas.width - 48;
      const wrap = (text, font, maxLines) => {
        context.font = font;
        const lines = [];
        let line = '';
        text.split(' ').forEach((word) => {
          const next = line ? `${line} ${word}` : word;
          if (context.measureText(next).width > textWidth && line) {
            lines.push(line);
            line = word;
          } else line = next;
        });
        if (line) lines.push(line);
        return lines.slice(0, maxLines);
      };
      const addressParts = (value.location || '').split('|').map((part) => part.trim()).filter(Boolean);
      const place = addressParts[addressParts.length - 1]?.split(',')[0] || 'Location captured';
      context.font = `bold ${Math.max(16, Math.min(30, canvas.width * 0.022))}px sans-serif`;
      context.fillText(`GPS MAP CAMERA  |  ${place}`, 24, canvas.height - overlayHeight + 28);
      context.font = `${Math.max(12, Math.min(22, canvas.width * 0.016))}px sans-serif`;
      const addressLines = addressParts.flatMap((part) => wrap(part, context.font, 1)).slice(0, 2);
      addressLines.forEach((line, index) => context.fillText(line, 24, canvas.height - overlayHeight + 53 + (index * 20)));
      context.fillStyle = '#b7f7d8';
      const detailsY = canvas.height - overlayHeight + 53 + (addressLines.length * 20);
      context.fillText(`Lat ${value.latitude}  Long ${value.longitude}`, 24, detailsY);
      context.fillText(`Captured ${capturedAt}`, 24, detailsY + 20);
      animationRef.current = requestAnimationFrame(drawFrame);
    };
    const canvasStream = canvas.captureStream(30);
    streamRef.current.getAudioTracks().forEach((track) => canvasStream.addTrack(track));
    const recorder = new MediaRecorder(canvasStream);
    const chunks = [];
    recorder.ondataavailable = (event) => event.data.size && chunks.push(event.data);
    recorder.onstop = () => {
      cancelAnimationFrame(animationRef.current);
      canvasStream.getTracks().forEach((track) => track.stop());
      const blob = new Blob(chunks, { type: recorder.mimeType || 'video/webm' });
      if (previewUrlRef.current) URL.revokeObjectURL(previewUrlRef.current);
      previewUrlRef.current = URL.createObjectURL(blob);
      const reader = new FileReader();
      reader.onload = () => onChange({ ...value, videoUrl: reader.result });
      reader.readAsDataURL(blob);
    };
    recorderRef.current = recorder;
    drawFrame();
    recorder.start();
    setRecording(true);
  };

  const stopRecording = () => {
    if (recorderRef.current?.state === 'recording') {
      recorderRef.current.stop();
      setRecording(false);
    }
  };

  const closeCamera = () => {
    cancelAnimationFrame(animationRef.current);
    streamRef.current?.getTracks().forEach((track) => track.stop());
    streamRef.current = null;
    setVideoReady(false);
    setCameraOpen(false);
    setRecording(false);
  };

  return (
    <div className="live-evidence-capture">
      <div className="live-evidence-grid">
        {includePhoto && <div className="live-evidence-item">
          <LivePhotoCapture value={value} onChange={onChange} />
        </div>}

        <div className="live-evidence-item">
          <label>Video Evidence (Optional)</label>
          <small>Live camera recording only — no gallery upload, so evidence can't be faked or reused.</small>
          {!cameraOpen && !value.videoUrl && <div className="evidence-actions">
            <button type="button" className="secondary-button" onClick={openCamera}>🎥 Record video</button>
          </div>}
          {cameraOpen && !value.videoUrl && <>
            <div className="evidence-video-frame">
              <video ref={videoRef} className="evidence-video" autoPlay muted playsInline onLoadedMetadata={() => setVideoReady(true)} />
              <div className="live-gps-overlay">
                <strong>LIVE GPS EVIDENCE</strong>
                <span>{value.latitude && value.longitude ? `Lat ${value.latitude}  Long ${value.longitude}` : 'Capture photo first for GPS coordinates'}</span>
                <span>{value.location || 'Exact address will appear after photo capture'}</span>
                <span>{new Date().toLocaleString()}</span>
              </div>
            </div>
            <canvas ref={canvasRef} className="evidence-canvas" />
            <small>{videoReady ? 'Camera preview is active. Start recording when ready.' : 'Connecting to camera preview...'}</small>
            <div className="evidence-actions">
              {!recording ? <button type="button" className="primary-button" onClick={startRecording} disabled={!videoReady}>{videoReady ? 'Start recording' : 'Starting camera...'}</button> : <button type="button" className="secondary-button" onClick={stopRecording}>Stop recording</button>}
              <button type="button" className="secondary-button" onClick={closeCamera}>Cancel camera</button>
            </div>
          </>}
          {value.videoUrl && <>
            <video className="evidence-video" controls src={previewUrlRef.current || value.videoUrl} />
            <button type="button" className="secondary-button" onClick={() => { onChange({ ...value, videoUrl: '' }); closeCamera(); }}>Remove / change video</button>
          </>}
        </div>
      </div>
      {error && <span className="error-text">{error}</span>}
    </div>
  );
}
