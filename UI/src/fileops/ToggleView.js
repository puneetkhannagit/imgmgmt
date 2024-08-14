import React, { useState } from 'react';
import { Button, Box } from '@mui/material';
import ImagesAsGrid from './ImagesAsGrid'; // Assuming you have this component
import ShowTheList from './ShowTheList'; // Assuming you have this component

const ToggleView = () => {
    const [view, setView] = useState('grid'); // 'grid' or 'table'

    const toggleView = () => {
        setView((prevView) => (prevView === 'grid' ? 'table' : 'grid'));
    };

    return (
        <Box>
            <Button variant="contained" onClick={toggleView} sx={{ mb: 2 }}>
                Switch to {view === 'grid' ? 'Table' : 'Grid'} View
            </Button>
            {view === 'grid' ? <ImagesAsGrid /> : <ShowTheList />}
        </Box>
    );
};

export default ToggleView;
