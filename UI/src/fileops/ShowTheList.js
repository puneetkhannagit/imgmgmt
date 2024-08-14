import React, { useState, useEffect } from 'react';
import {
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Paper,
    CircularProgress,
    Typography,
    Modal,
    Box,
    Button,
    Pagination
} from '@mui/material';

const ShowTheList = () => {
    const [data, setData] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [open, setOpen] = useState(false);
    const [selectedImage, setSelectedImage] = useState(null);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(1);
    const pageSize = 2; // Adjust page size as needed

    const handleOpen = (id) => {
        fetch(`http://localhost:8080/cms/image/${id}`)
            .then((res) => res.blob())
            .then((blob) => {
                setSelectedImage(URL.createObjectURL(blob));
                setOpen(true);
            })
            .catch(() => setSelectedImage(null));
    };

    const handleClose = () => setOpen(false);

    const fetchData = async (page) => {
        try {
            const response = await fetch(`http://localhost:8080/cms/list/all?page=${page}&size=${pageSize}`);
            if (!response.ok) throw new Error('Failed to fetch list');

            const result = await response.json();
            setTotalPages(result.totalPages);

            const dataWithThumbnails = await Promise.all(result.files.map(async (item) => {
                try {
                    const thumbnailResponse = await fetch(`http://localhost:8080/cms/thumbnail/${item.id}`);
                    if (thumbnailResponse.ok) {
                        const blob = await thumbnailResponse.blob();
                        const thumbnailUrl = URL.createObjectURL(blob);
                        return { ...item, thumbnailUrl };
                    }
                    return { ...item, thumbnailUrl: null };
                } catch {
                    return { ...item, thumbnailUrl: null };
                }
            }));

            setData(dataWithThumbnails);
            setLoading(false);
        } catch (err) {
            setError(err.message);
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchData(page);
    }, [page]);

    if (loading) return <div>Loading...</div>;
    if (error) return <div>Something went wrong: {error}, If the network is not a problm,It is not you , its us.</div>;

    return (
        <div>
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
                        {data.map((item) => (
                            <TableRow key={item.id}>
                                <TableCell>
                                    {item.thumbnailUrl ? (
                                        <img
                                            src={item.thumbnailUrl}
                                            alt="Thumbnail"
                                            style={{ maxWidth: '50px', height: 'auto', cursor: 'pointer' }}
                                            onClick={() => handleOpen(item.id)}
                                        />
                                    ) : (
                                        'No Thumbnail'
                                    )}
                                </TableCell>
                                <TableCell>{item.id}</TableCell>
                                <TableCell>{item.thumbnailLocation}</TableCell>
                                <TableCell>{item.location}</TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
                <Modal open={open} onClose={handleClose}>
                    <Box sx={{ p: 2, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                        <Button onClick={handleClose}>Close</Button>
                        {selectedImage && <img src={selectedImage} alt="Full Size" style={{ maxWidth: '100%' }} />}
                    </Box>
                </Modal>
            </TableContainer>
            <Pagination
                count={totalPages}
                page={page + 1}
                onChange={(event, value) => setPage(value - 1)}
                color="primary"
                sx={{ mt: 2, display: 'flex', justifyContent: 'center' }}
            />
        </div>
    );
};

export default ShowTheList;
