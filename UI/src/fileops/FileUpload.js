import React, { useState } from 'react';
import axios from 'axios';

const FileUploader = () => {
  const [file, setFile] = useState(null);
  const [filename, setFilename] = useState('');
  const [foldername, setFoldername] = useState('');
  const [uploading, setUploading] = useState(false);
  const [progress, setProgress] = useState(0);

  const CHUNK_SIZE = 100 * 1024; // 100 KB

  const handleFileChange = (e) => {
    setFile(e.target.files[0]);
    setFilename(e.target.files[0].name);
  };

  const uploadChunk = async (chunk, index) => {
    const formData = new FormData();
    formData.append('file', chunk);
    formData.append('filename', filename);
    formData.append('foldername', foldername);
    formData.append('chunkIndex', index);

    try {
      const response = await axios.post('http://localhost:8080/cms/uploadchunk', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      return response.data;
    } catch (error) {
      console.error(`Error uploading chunk ${index}:`, error);
      return null;
    }
  };

  const uploadAllChunks = async (failedChunks = []) => {
    const totalChunks = Math.ceil(file.size / CHUNK_SIZE);
    let uploadedChunks = new Set(); // Track uploaded chunks

    // Upload chunks that haven't been uploaded yet or failed
    for (let i = 0; i < totalChunks; i++) {
      if (!uploadedChunks.has(i) && !failedChunks.includes(i)) {
        const start = i * CHUNK_SIZE;
        const end = Math.min(start + CHUNK_SIZE, file.size);
        const chunk = file.slice(start, end);

        let result = await uploadChunk(chunk, i);

        if (result === "Chunk uploaded successfully!") {
          uploadedChunks.add(i);
        } else {
          failedChunks.push(i); // Mark chunk as failed
        }

        const progressPercentage = (uploadedChunks.size / totalChunks) * 100;
        setProgress(progressPercentage);
      }
    }

    return failedChunks;
  };

  const startUpload = async () => {
    if (!file) return;
    setUploading(true);
    setProgress(0);

    let failedChunks = [];
    
    do {
      failedChunks = await uploadAllChunks(failedChunks);
      // Retry failed chunks
      if (failedChunks.length > 0) {
        await new Promise(resolve => setTimeout(resolve, 1000)); // Wait before retrying
      }
    } while (failedChunks.length > 0);

    setUploading(false);
  };

  return (
    <div>
      <input type="file" onChange={handleFileChange} disabled={uploading} />
      <input
        type="text"
        placeholder="Folder name (optional)"
        value={foldername}
        onChange={(e) => setFoldername(e.target.value)}
        disabled={uploading}
      />
      <button onClick={startUpload} disabled={uploading || !file}>
        {uploading ? `Uploading... ${progress.toFixed(2)}%` : 'Upload File'}
      </button>
    </div>
  );
};

export default FileUploader;
