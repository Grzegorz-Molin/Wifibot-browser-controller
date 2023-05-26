import "./CustomSlider.css";
import charToPercent from "./CharToPercent";

export default function CustomSlider({
    min,
    max,
    step,
    value,
    handleChange,
    typeOfProperty,
    tagMin,
    tagDefault,
    tagMax,
}) {
    const percentFromleft = charToPercent(tagDefault, tagMax);

    // In positioning absolute this default label tag the minimal leftmargin is 10%, and maximal 80%.
    // Therefore, when we get value from charToPercent() whe then need to convert it to the scale of maximal move of 70%
    function percentToEms(percent) {
        var maximalValueOnTheRightInPercent = 90
        var temp = (((percent*maximalValueOnTheRightInPercent) /100)).toFixed(2);
        temp = temp.toString();
        temp += "%";
        return temp;
    }

    const leftMargin = percentToEms(percentFromleft);

    const tagDefaultStyle = {
        left: leftMargin,
    };
    return (
        <div className="customSlider">
            <input
                type="range"
                value={value}
                onChange={(event) =>
                    handleChange(typeOfProperty, event.target.value, min, max)
                }
                min={min}
                max={max}
                step={step}
            />
            <div className="labels">
                <p
                    onClick={(event) =>
                        handleChange(typeOfProperty, event.target.textContent, min, max)
                    }
                >
                    {tagMin}
                </p>
                <p
                    style={tagDefaultStyle}
                    onClick={(event) =>
                        handleChange(typeOfProperty, event.target.textContent, min, max)
                    }
                >
                    {tagDefault}
                </p>
                <p
                    onClick={(event) =>
                        handleChange(typeOfProperty, event.target.textContent, min, max)
                    }
                >
                    {tagMax}
                </p>
            </div>
        </div>
    );
}
