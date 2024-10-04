import { useState } from "react";
import "./App.css";
import VideoUpload from "./components/VideoUpload";
import { Toaster } from "react-hot-toast";
import VideoPlayer from "./components/VideoPlayer";
import { Button, TextInput } from "flowbite-react";

function App() {

  
  const videoId = 
    "8c9716b9-54d1-410a-82f6-d600b41cf471";
  

  /*function playVideo(videoId) {
    setVideoId(videoId);
  }*/
  return (
    <>
      <Toaster />
      <div className="flex flex-col  items-center space-y-9 justify-center py-9">
    

        <div className="flex mt-14 w-full space-x-2  justify-between">
          <div className="w-full">
            <h1 className="text-white text-center mt-2">Playing Video</h1>

            

            <div>
              <VideoPlayer
                src={`http://localhost:8080/api/v1/videos/${videoId}/master.m3u8`}
              ></VideoPlayer>
            </div>

        
          </div>

          <div className="w-full">
            <VideoUpload />
          </div>
        </div>

        
      </div>
    </>
  );
}

export default App;