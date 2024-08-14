import React, { useState, useEffect } from 'react';
import {
    Box,
    Button,
    Typography,
    Grid,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Paper,
    Modal,
    Pagination,
} from '@mui/material';
import axios from 'axios';

const ImagesComponent = () => {
    const [view, setView] = useState('grid'); // 'grid' or 'table'
    const [files, setFiles] = useState([]);
    const [totalPages, setTotalPages] = useState(0);
    const [currentPage, setCurrentPage] = useState(0);
    const [selectedImage, setSelectedImage] = useState(null);
    const [open, setOpen] = useState(false);
    const pageSize = 2; // Number of images per page

    useEffect(() => {
        fetchImages(currentPage);
    }, [currentPage]);

    const fetchImages = (page) => {
        axios.get(`http://localhost:8080/cms/listimages?page=${page}&size=${pageSize}`)
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

    const handleOpen = (id) => {
        axios.get(`http://localhost:8080/cms/image/${id}`, { responseType: 'blob' })
            .then(response => {
                const imageUrl = URL.createObjectURL(response.data);
                setSelectedImage(imageUrl);
                setOpen(true);
            })
            .catch(() => setSelectedImage(null));
    };

    const handleClose = () => setOpen(false);

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
            <Button variant="contained" onClick={() => setView(view === 'grid' ? 'table' : 'grid')}>
                Toggle to {view === 'grid' ? 'Table View' : 'Grid View'}
            </Button>

            {view === 'grid' ? (
                <Box mt={4}>
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
                </Box>
            ) : (
                <Box mt={4}>
                    <TableContainer component={Paper}>
                        <Table>
                            <TableHead>
                                <TableRow>
                                    <TableCell>Thumbnail</TableCell>
                                    <TableCell>ID</TableCell>
                                    <TableCell>Thumbnail Location</TableCell>
                                    <TableCell>Resized File Location</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {files.map((item) => (
                                    <TableRow key={item.metadata.id}>
                                        <TableCell>
                                            {item.thumbnailLocation ? (
                                                <img
                                                    src={`data:${item.metadata.fileType};base64,${item.image}`}
                                                    alt="Thumbnail"
                                                    style={{ maxWidth: '50px', height: 'auto', cursor: 'pointer' }}
                                                    onClick={() => handleOpen(item.metadata.id)}
                                                />
                                            ) : (
                                                'No Thumbnail'
                                            )}
                                        </TableCell>
                                        <TableCell>{item.metadata.id}</TableCell>
                                        <TableCell>{item.metadata.thumbnailLocation}</TableCell>
                                        <TableCell>{item.metadata.location}</TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </TableContainer>
                    <Modal open={open} onClose={handleClose}>
                        <Box sx={{ p: 2, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                            <Button onClick={handleClose}>Close</Button>
                            {selectedImage && <img src={selectedImage} alt="Full Size" style={{ maxWidth: '100%' }} />}
                        </Box>
                    </Modal>
                </Box>
            )}

            <Pagination
                count={totalPages}
                page={currentPage + 1}
                onChange={handlePageChange}
                color="primary"
                sx={{ mt: 2, display: 'flex', justifyContent: 'center' }}
            />
        </Box>
    );
};

export default ImagesComponent;
