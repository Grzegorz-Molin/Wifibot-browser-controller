import * as React from 'react';
import "./Gauge.css"

export default function Gauge({data}) {
    console.log(`This is speed: ${data}`)
    var temp = (data * 100) / 255;
    temp = temp.toFixed(0);

    return (
        <div class="gauge">
            <div class="progress">
                <div class="bar"></div>
                <div class="needle"></div>
            </div>
        </div>
    )
}