import React from "react";
import image from "../images/signal.png";

export default function Signal({ data }) {
    return (
        <div className="container">
            <img className="image" src={image} alt="image" />
            <div className="textInfo">
                <div className="label">{data}</div>
            </div>
        </div>
    );
}
