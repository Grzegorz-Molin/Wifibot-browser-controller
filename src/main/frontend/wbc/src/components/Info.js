import React from "react";
import "./Info.css";
import "./BatteryIndicator";
import Current from "./CurrentIndicator";
import BatteryIndicator from "./BatteryIndicator";
import SpeedIndicator from "./SpeedIndicator";
import MemoizedCurrentIndicator from "./Odometer";

function Info({
    speedL,
    speedR,
    battery,
    current,
    odometryLeft,
    odometryRight
}) {
    return (
        <div className="component info">
            <div className="info__item info__item__topBar">
                <div className="info__item info__item__topBar__inner">
                    <div className="info__item__bg info__item__others info__item__others__batteryIndicator">
                        <BatteryIndicator data={battery} />
                    </div>
                    <div className="info__item__bg info__item__others info__item__others__current">
                        <Current data={current} />
                    </div>
                </div>
            </div>

            <div className="info__item__odometer">
                <MemoizedCurrentIndicator
                    odometryLeft={odometryLeft}
                    odometryRight={odometryRight}
                />
            </div>
            <div className="info__item info__item__speed__left">
                <SpeedIndicator speedInTics={speedL} />
            </div>
            <div className="info__item info__item__speed__right">
                <SpeedIndicator speedInTics={speedR} />
            </div>
        </div>
    );
}

export default Info;
