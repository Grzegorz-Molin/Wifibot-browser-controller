import * as React from 'react';
import './BatteryIndicator.css';


export default function BatteryIndicator({data}) {
    var temp = (data * 100) / 255;
    temp = temp.toFixed(0);
    var percent = temp.toString() + "%"

  return (
    <div className="batteryIndicator">
        <div className="content">
            <div className="batteryShape">
                <div className="level">
                    <div className="percentage" style={{"width": `${percent}`}}></div>
                </div>
            </div>
            <div className="textInfo">
                <div className="label">{percent}</div>
            </div>
        </div>
    </div>
  );
}
