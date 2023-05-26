import * as React from "react";
import image from "../images/current.png";

export default function CurrentIndicator({ data }) {
    var current = (data * 0.1).toFixed(0);

    return (
        <div className="container">
            <img className="image" src={image} alt="image" />
            <div className="textInfo">
                <div className="label">{current}A</div>
            </div>
        </div>
    );
}
