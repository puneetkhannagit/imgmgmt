import React, { useState, useEffect } from 'react';

const ImageViewer = ({ fileId }) => {
    const [imageSrc, setImageSrc] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchImage = async () => {
            try {
                const response = await fetch(`http://localhost:8080/cms/thumbnail/${fileId}`);
                if (!response.ok) {
                    throw new Error('Image not found');
                }
                const blob = await response.blob();
                const url = URL.createObjectURL(blob);
                setImageSrc(url);
                setLoading(false);
            } catch (err) {
                setError(err.message);
                setLoading(false);
            }
        };

        fetchImage();
    }, [fileId]);

    if (loading) return <p>Loading...</p>;
    if (error) return <p>Error: {error}</p>;

    return (
        <div style={{ textAlign: 'center' }}>
            {imageSrc && <img src={imageSrc} alt="Thumbnail" style={{ maxWidth: '100%', height: 'auto' }} />}
        </div>
    );
};

export default ImageViewer;
