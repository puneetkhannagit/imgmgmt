import { BrowserRouter, Route, Routes } from "react-router-dom";
import FileUpload from "./fileops/FileUpload";
import CompleteFileUpload from "./fileops/CompleteFileUpload";
import ShowTheList from "./fileops/ShowTheList";

import ImagesComponent from "./fileops/ImagesComponent";
import ImagesAsGrid from "./fileops/ImagesAsGrid";
import ToggleView from "./fileops/ToggleView";

function App() {
  return (
    <>
      <div>Image Viewer App </div>
     <BrowserRouter>
      
      <Routes>
        <Route exact path='/submit' element={<CompleteFileUpload/>}/>
        <Route exact path='/list' element={<ToggleView/>}/>
        {/* <Route exact path='/grid' element={<ImagesAsGrid/>}/>
        <Route exact path='/toggle' element={<ToggleView/>}/>
        */}
      </Routes>
     </BrowserRouter>
    </>
  );
}

export default App;
