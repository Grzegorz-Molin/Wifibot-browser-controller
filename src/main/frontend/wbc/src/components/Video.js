import React from "react";
import ReactPlayer from "react-player";
import "./Video.css";
import Ir from "./Ir";

function Video({ robotIP, videoIsConnected, setVideoIsConnected }) {
    const streamUrl = "http://" + robotIP + "/?action=stream";

    // console.log("[video] stream url is:"+streamUrl);
    return (
        <div className="component video">
            <div className="video__wrapper">
                {videoIsConnected ? (
                    <img src={streamUrl} alt={streamUrl} />
                ) : (
                    <p>Video not connected</p>
                )}
            </div>
        </div>
    );
}

export default Video;
