import React from "react";
import { useEffect } from "react";
import "./Ir.css";

function Ir({ name, data }) {
    const className = "ir__" + name;

    var height = 255 - data;
    height = (height * 100) / 255;

    const bottomRadius = height < 100 ? "overlaying__div__bottom__radius" : "";

    return (
        <div className={className}>
            <div className={"ir"}>
                <div
                    className={`overlaying__div ${bottomRadius}`}
                    style={{
                        height: `${height}%`,
                    }}
                ></div>
            </div>
        </div>
    );
}

export default Ir;
