import React, { useState } from 'react';

function CompleteFileUpload (){
    const [selectedFile, setSelectedFile] = useState(null);
    const [responseMessage, setResponseMessage] = useState('');

    // Handle file selection
    const handleFileChange = (event) => {
        setSelectedFile(event.target.files[0]);
    };

    // Handle file upload
    const handleUpload = async () => {
        if (!selectedFile) {
            setResponseMessage('No file selected.');
            return;
        }

        const formData = new FormData();
        formData.append('file', selectedFile);

        try {
            const response = await fetch('http://localhost:8080/cms/submit', {
                method: 'POST',
                body: formData,
            });

            const result = await response.text();
            setResponseMessage(result);
        } catch (error) {
            setResponseMessage('Failed to upload file: ' + error.message);
        }
    };

    return (
        <div>
            <h1>File Upload</h1>
            <input type="file" onChange={handleFileChange} />
            <button onClick={handleUpload}>Upload</button>
            <p>{responseMessage}</p>
        </div>
    );
};

export default CompleteFileUpload;
