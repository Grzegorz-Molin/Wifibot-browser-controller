export default function charToPercent(value, maxValue) {
    const temp = (value * 100) / maxValue;

    return temp.toFixed(2);
}