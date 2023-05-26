import React from "react";
import "./Video.css";

function Video({ robotIP, videoIsConnected, setVideoIsConnected }) {
    const streamUrl = "http://" + robotIP + ":8080/?action=stream";

    return (
        <div className="component video">
            <div className="video__wrapper">
                {videoIsConnected ? (
                    <img src={streamUrl} alt={`video stream from: ${streamUrl}`}/>
                ) : (
                    <p>Video not connected</p>
                )}
            </div>
        </div>
    );
}

export default Video;
