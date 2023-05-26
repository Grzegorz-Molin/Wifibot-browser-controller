import React from "react";
import "./Controls.css";
import Switch from "./Switch";
import CustomSlider from "./Others/CustomSlider";

function Controls({
    videoIsConnected,
    proxyIsConnected,
    handleProxyChange,
    handleVideoChange,
    proxyProperties,
    handleProxyFirstPropertiesChange,
    handleLocalFirstPropertiesChange,
}) {
    function alreadyConnected() {
        setTimeout(() => {
            return "alreadyConnected";
        }, 1000);
    }
    return (
        <div className="component controls">
            <h1>Wifibot Browser Controller</h1>

            <div className="formGroup formGroup__switches">
                <h2>Connection</h2>
                <Switch
                    text="Robot"
                    toggled={proxyIsConnected}
                    onChange={handleProxyChange}
                />
                <Switch
                    text="Video"
                    toggled={videoIsConnected}
                    onChange={handleVideoChange}
                />
            </div>
            {/* Speed */}
            <div className="formGroup">
                <div className="formGroup__item formGroup__item__switches">
                    <div className="formGroup__groupName">
                        <h2>Robot Speed [tcs]</h2>
                        <input
                            className="inputText"
                            value={proxyProperties.speed}
                            onChange={(event) =>
                                handleProxyFirstPropertiesChange(
                                    "speed",
                                    event.target.value,
                                    0,
                                    240
                                )
                            }
                            type="number"
                        />
                    </div>

                    <CustomSlider
                        min={0}
                        max={240}
                        step={1}
                        value={proxyProperties.speed}
                        handleChange={handleProxyFirstPropertiesChange}
                        typeOfProperty={"speed"}
                        tagMin={0}
                        tagDefault={130}
                        tagMax={240}
                    />
                </div>
            </div>

            {/* Connectivity roxy properties */}

            <div className="formGroup">
                <div className="formGroup__item">
                    <div className="formGroup__groupName">
                        {/* Sending interval */}
                        <h2>Sending commands interval [ms]</h2>
                        <input
                            className="inputText"
                            value={proxyProperties.sendingInterval}
                            onChange={(event) =>
                                handleProxyFirstPropertiesChange(
                                    "sendingInterval",
                                    event.target.value,
                                    0,
                                    200
                                )
                            }
                            type="number"
                        />
                    </div>
                    <CustomSlider
                        min={10}
                        max={200}
                        step={5}
                        value={proxyProperties.sendingInterval}
                        handleChange={handleProxyFirstPropertiesChange}
                        typeOfProperty={"sendingInterval"}
                        tagMin={10}
                        tagDefault={25}
                        tagMax={200}
                    />
                </div>

                {/* Fetching interval */}
                <div className="formGroup__item">
                    <div className="formGroup__groupName">
                        <h2>Fetching data interval [ms]</h2>
                        <input
                            className="inputText"
                            value={proxyProperties.fetchingInterval}
                            onChange={(event) =>
                                handleProxyFirstPropertiesChange(
                                    "fetchingInterval",
                                    event.target.value,
                                    0,
                                    5000
                                )
                            }
                            type="number"
                        />
                    </div>
                    <CustomSlider
                        min={0}
                        max={5000}
                        step={100}
                        value={proxyProperties.fetchingInterval}
                        handleChange={handleProxyFirstPropertiesChange}
                        typeOfProperty={"fetchingInterval"}
                        tagMin={0}
                        tagDefault={250}
                        tagMax={5000}
                    />
                </div>
            </div>
            {/* IP addresses and ports */}
            <div className="formGroup">
                <div className="formGroup__item">
                    <div className="formGroup__groupName formGroup__groupName__textInputs">
                        <h2>Robot IP address</h2>
                        <input
                            // disabled={proxyIsConnected ? true : false}
                            className="inputText"
                            value={proxyProperties.robotIP}
                            onChange={(event) => {
                                handleLocalFirstPropertiesChange(
                                    "robotIP",
                                    event.target.value
                                );
                            }}
                            type="text"
                        />
                    </div>
                    <div className="formGroup__groupName formGroup__groupName__textInputs">
                        <h2>Robot sending port</h2>
                        <input
                            // disabled={proxyIsConnected ? true : false}
                            className="inputText"
                            value={proxyProperties.robotSendingPort}
                            onChange={(event) => {
                                handleLocalFirstPropertiesChange(
                                    "robotSendingPort",
                                    event.target.value
                                );
                            }}
                            type="text"
                        />
                    </div>
                    <div className="formGroup__groupName formGroup__groupName__textInputs">
                        <h2>Robot fetching port</h2>
                        <input
                            // disabled={proxyIsConnected ? true : false}
                            className="inputText"
                            value={proxyProperties.robotFetchingPort}
                            onChange={(event) => {
                                handleLocalFirstPropertiesChange(
                                    "robotFetchingPort",
                                    event.target.value
                                );
                            }}
                            type="text"
                        />
                    </div>
                    <div className="formGroup__groupName formGroup__groupName__textInputs">
                        <h2>Proxy IP address</h2>
                        <input
                            // disabled={proxyIsConnected ? true : false}
                            className="inputText"
                            value={proxyProperties.proxyIP}
                            onChange={(event) => {
                                handleLocalFirstPropertiesChange(
                                    "proxyIP",
                                    event.target.value
                                );
                            }}
                            type="text"
                        />
                    </div>
                    <div className="formGroup__groupName formGroup__groupName__textInputs">
                        <h2>Proxy port</h2>
                        <input
                            // disabled={proxyIsConnected ? true : false}
                            className="inputText"
                            value={proxyProperties.proxyPort}
                            onChange={(event) => {
                                handleLocalFirstPropertiesChange(
                                    "proxyPort",
                                    event.target.value
                                );
                            }}
                            type="text"
                        />
                    </div>
                </div>
            </div>
        </div>
    );
}

export default Controls;
