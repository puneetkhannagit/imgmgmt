import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { Box, Typography, Pagination } from '@mui/material';

const ImagesAsGrid = () => {
    const [files, setFiles] = useState([]);
    const [totalPages, setTotalPages] = useState(0);
    const [currentPage, setCurrentPage] = useState(0);
    const pageSize = 2; // Number of images per page

    useEffect(() => {
        fetchImages(currentPage);
    }, [currentPage]);

    const fetchImages = (page) => {
        axios.get(`http://localhost:8080/cms/list?page=${page}&size=${pageSize}`)
            .then(response => {
                setFiles(response.data.files);
                setTotalPages(response.data.totalPages);
            })
            .catch(error => {
                console.error('There was an error fetching the images!', error);
            });
    };

    const handlePageChange = (event, value) => {
        setCurrentPage(value - 1);
    };

    const openImageInNewTab = (base64Image, fileType) => {
        const newWindow = window.open();
        if (newWindow) {
            newWindow.document.write(
                `<img src="data:${fileType};base64,${base64Image}" style="width: auto; height: auto; max-width: 100%; max-height: 100%;" />`
            );
            newWindow.document.title = "Full-Size Image";
        }
    };

    return (
        <Box>
            <Typography variant="h4" gutterBottom>
                Image List
            </Typography>
            {files.map(file => (
                <Box key={file.metadata.id} mb={4}>
                    <Typography variant="subtitle1">{file.metadata.filename}</Typography>
                    {file.image ? (
                        <img 
                            src={`data:${file.metadata.fileType};base64,${file.image}`} 
                            alt={file.metadata.filename} 
                            style={{ cursor: 'pointer', border: '1px solid #ddd', padding: '5px', width: '100%', height: 'auto' }}
                            onClick={() => openImageInNewTab(file.image, file.metadata.fileType)}
                        />
                    ) : (
                        <Typography variant="body2">No image available</Typography>
                    )}
                </Box>
            ))}
            <Pagination 
                count={totalPages} 
                page={currentPage + 1} 
                onChange={handlePageChange} 
                color="primary" 
                style={{ marginTop: '20px' }}
            />
        </Box>
    );
};

export default ImagesAsGrid;
