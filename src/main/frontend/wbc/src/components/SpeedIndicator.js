import * as React from "react";
import "./SpeedIndicator.css";
import styled, { keyframes } from "styled-components";

const SpeedIndicator = React.memo(({ speedInTics }) => {
    const ROTATION_TIME = 0.3;
    const FULL_ROTATION_ANGLE = 135;
    
    // Conversion of speed from range 0-255 to percent 0-100%
    var speedInPercent = (speedInTics * 100) / 255;
    speedInPercent = speedInPercent.toFixed(0);

    // Conversion of percent from range 0-100% to rotation 0-90deg
    var rotationAmount = (speedInPercent * FULL_ROTATION_ANGLE) / 100;
    rotationAmount = rotationAmount.toFixed(0);

    // Color of bar
    var greenColor1 = 159;
    var greenColor2 = 120;
    var greenColorDifference = greenColor1 - greenColor2;
    var greenColorDelta = (
        Math.abs(speedInPercent) *
        0.01 *
        greenColorDifference
    ).toFixed(0);
    var greenColorFinal = greenColor1 - greenColorDelta;
    var barColor = "rgb(70, " + greenColorFinal + ", 200)";

    // Rotation styles of hiding bars
    const progressStyle1 = {
        transform: `rotate(${rotationAmount < 0 ? rotationAmount : 0}deg)`,
        transition: `all 0.3s`,
    };

    const progressStyle2 = {
        transform: `rotate(${rotationAmount > 0 ? rotationAmount : 0}deg)`,
        transition: `all 0.3s`,
    };

    return (
        <div class="speedIndicator">
            <div class="progress">
                <div style={progressStyle1} className="progress1"></div>
                <div style={progressStyle2} className="progress2"></div>
                <Bar
                    id="bar"
                    backgroundColor={barColor}
                    rotationAmount={rotationAmount}
                    finalColor={barColor}
                    rotationTime={ROTATION_TIME}
                ></Bar>
                <div className="textBadge">
                    <div className="number">{speedInTics}</div>
                    <div className="text">tcs</div>
                </div>
                <div
                    style={{ transform: `rotate(${rotationAmount}deg)` }}
                    className="needle"
                ></div>
            </div>
        </div>
    );
});

// Color change
const rotateBar = (rotationAmount, finalColor) => {
    if (rotationAmount > 0)
        return keyframes`
        0% {
            background-color: ${finalColor};
        }
        90% { 
            background-color: ${finalColor}; 
        }
        100% { 
            transform: rotate(${rotationAmount}deg);
        }
    `;
    else if (rotationAmount < 0)
        return keyframes`
        0% {
            background-color: ${finalColor};
        }
        90% { 
            background-color: ${finalColor}; 
        }
    `;
};

// Styled component
const Bar = styled.div`
    & {
        position: absolute;
        z-index: 3;
        width: 50%;
        height: 100%;
        background-color: ${(props) => props.backgroundColor};
        transform: rotate(90deg);
        transform-origin: center right;
        animation: ${(props) =>
                rotateBar(props.rotationAmount, props.finalColor)}
            ${(props) => props.rotationTime}s ease-in-out normal;
        animation-fill-mode: forwards;
    }
`;

export default SpeedIndicator;
