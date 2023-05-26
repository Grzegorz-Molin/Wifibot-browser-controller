import React, { useEffect } from "react";
import "./Odometer.css";
import TextTransition, { presets } from "react-text-transition";

function CurrentIndicator({ odometryLeft, odometryRight }) {

  return (
    <div className="odometer">
      <div className="label odometryLeft">
        <MemoizedTextTransition value={odometryLeft} />
        <p className="tcs">tcs</p>
      </div>
      <div className="label odometryRight">
        <MemoizedTextTransition value={odometryRight} />
        <p className="tcs">tcs</p>
      </div>
    </div>
  );
}

function arePropsEqual(prevProps, nextProps) {
  return prevProps.odometryLeft === nextProps.odometryLeft &&
    prevProps.odometryRight === nextProps.odometryRight;
}

const MemoizedCurrentIndicator = React.memo(CurrentIndicator, arePropsEqual);

export default MemoizedCurrentIndicator;

function TextTransitionComponent({ value }) {
  return (
    <TextTransition translateValue={"20%"} springConfig={presets.wobbly}>
      <p>{value !== null ? value : 0}</p>
    </TextTransition>
  );
}

const MemoizedTextTransition = React.memo(TextTransitionComponent);
