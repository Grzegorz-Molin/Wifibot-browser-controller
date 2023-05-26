import "./Switch.css";

export default function Switch({ text, toggled, onChange }){
    const className = "text state " + (toggled ? "on" : "off");
    const state = toggled ? "ON" : "OFF";
    return (
        <div className="toggleContainer">
            <div className="switch text">
                <strong>{text}</strong>
            </div>
            <label>
                <input type="checkbox" checked={toggled} onChange={onChange} />
                <span />
                <div className={`${className}`}>{state}</div>
            </label>
        </div>
    );
};

