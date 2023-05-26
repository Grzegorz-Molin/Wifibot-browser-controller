import "./App.css";
import Video from "./components/Video";
import Controls from "./components/Controls";
import Info from "./components/Info";
import Ir from "./components/Ir";
import { useEffect, useState } from "react";
import SockJS from "sockjs-client";
import { over } from "stompjs";

// ---------------------------------------------------------------------------------------
var stompClient = null;

export default function App() {
    // Proxy and robot status variables
    const [proxyIsConnected, setProxyIsConnected] = useState(false);
    const [videoIsConnected, setVideoIsConnected] = useState(false);
    const [proxyProperties, setProxyProperties] = useState({
        speed: 130,
        sendingInterval: 25,
        fetchingInterval: 250,
        robotIP: "192.168.1.106",
        robotSendingPort: 15000,
        robotFetchingPort: 15010,
        proxyIP: "localhost",
        proxyPort: 8080,
    });

    const [data, setData] = useState({
        speedFrontLeft: 0,
        batLevel: 0,
        irLeftFront: 0,
        irLeftBack: 0,
        odometryLeft: 0,

        speedFrontRight: 0,
        irRightFront: 0,
        irRightBack: 0,
        odometryRight: 0,

        current: 0,
        version: 0,
    });

    useEffect(() => {
        console.log("Video: " + videoIsConnected);
    }, [videoIsConnected]);

    // ---------------------------------------------------------------------------------------

    // DATA
    // useEffect(() => {
    //     console.log("DATA: " + JSON.stringify(data, null, 4));
    //     console.log("data.left.odometry: " + data.left.odometry);
    // }, [data]);

    function parseResponse(response) {
        // console.log("[parseResponse]: ---> " + JSON.stringify(response, 0, 4));
        const odometryLeftBigger =
            response.odometryLeft > data.odometryLeft
                ? response.odometryLeft
                : data.odometryLeft;
        const odometryRightBigger =
            response.odometryRight > data.odometryRight
                ? response.odometryRight
                : data.odometryRight;

        console.log("\nold - left: "+data.odometryLeft + "right: "+data.odometryRight);
        console.log("new - left: "+response.odometryLeft + "right: "+response.odometryRight);
        console.log("final - left: " + odometryLeftBigger + ", right: " + odometryRightBigger);
        setData((prevData) => ({
            ...prevData,
            speedFrontLeft: response.speedFrontLeft,
            speedFrontRight: response.speedFrontRight,
            batLevel: response.batLevel, // assuming battery level is same for left and right
            current: response.current,
            odometryLeft: odometryLeftBigger,
            odometryRight: odometryRightBigger,
            irLeftFront: response.irLeftFront,
            irLeftBack: response.irLeftBack,
            irRightFront: response.irRightFront,
            irRightBack: response.irRightBack,
            version: response.version,
        }));

    }

    useEffect(()=> {
        console.log("data: "+JSON.stringify(data, 0, 2))
    }, [data])

    // --- SOCK JS and CONNECTIVITY------------------------------------------------------------------------------------

    function connectProxySocket() {
        const proxyUrl =
            "http://" +
            proxyProperties.proxyIP +
            ":" +
            proxyProperties.proxyPort +
            "/ws";
        const sock = new SockJS(proxyUrl);
        stompClient = over(sock);
        stompClient.debug = null;
        stompClient.connect({}, onConnected, onError);
    }

    function disconnectProxySocket() {
        if (stompClient !== null) {
            disconnectFromRobot();
            stompClient.disconnect();
        }
        setProxyIsConnected(false);
        console.log("[Disconnected]");
    }

    function connectToRobot() {
        setPropertyOnProxy("robotIP", proxyProperties.robotIP);
        setPropertyOnProxy(
            "robotSendingPort",
            proxyProperties.robotSendingPort
        );
        setPropertyOnProxy(
            "robotFetchingPort",
            proxyProperties.robotFetchingPort
        );
        stompClient.send("/app/connectToRobot", {});
    }

    function disconnectFromRobot() {
        console.log("Disconnecting from robot...");
        stompClient.send("/app/disconnectFromRobot", {});
    }

    function onConnected() {
        setProxyIsConnected(true);
        connectToRobot();
        stompClient.subscribe("/topic/bot", onMessageReceived);
    }

    function onMessageReceived(payload) {
        let payloadData = JSON.parse(payload.body);

        // console.log("---> [server] " + JSON.stringify(payloadData, 0, 2));
        if (payloadData.message != null) {
            if (payloadData.message === "[Server] [Robot not connected]") {
                console.log("Robot NOT connected, disconnecting from proxy");
                disconnectProxySocket();
            } else if (payloadData.message === "[Server] [Robot connected]") {
                // nothing
            }
        } else {
            console.log(
                "'Message' IS empty, parsing reponse ..." + payloadData
            );
            parseResponse(payloadData);
        }
        // console.log("Message received: " + JSON.stringify(payloadData));
    }

    function onError(err) {
        console.log(err);
        console.log(
            "Also check if: \n1. Wifibot is running\n2. Proxy server is running\n3.You are connected to the right network"
        );
        setProxyIsConnected(false);
    }

    function commandRobot(commandToSend) {
        // updateSpeedIndicator(commandToSend);
        updateSpeedIndicator(commandToSend);
        if (stompClient !== null) {
            stompClient.send(
                "/app/commandRobot",
                {},
                JSON.stringify({ message: commandToSend })
            );
        } else {
            console.log("Cant send, Not connected!");
        }
    }

    // Function for setting some other property on Proxy server
    // eg. robot speed, fetching speed, robot port etc.
    async function setPropertyOnProxy(property, value) {
        if (stompClient === null) {
            return Promise.reject("Stomp client is not connected!");
        }

        return new Promise((resolve, reject) => {
            stompClient.send(
                "/app/setProperty",
                {},
                JSON.stringify({
                    message: `${property}:${value}`,
                })
            );

            // Register a one-time subscription for the setProperty response
            const subscription = stompClient.subscribe(
                "/topic/setPropertyResponse",
                (payload) => {
                    let payloadData = JSON.parse(payload.body);
                    console.log("[Server] result is: " + payloadData.message);

                    if (!payloadData.message) {
                        reject(`setProperty on proxy failed `);
                    } else {
                        // If succesful then set property value here in react
                        resolve(payloadData);
                    }

                    subscription.unsubscribe();
                }
            );
        });
    }

    function setProperty(property, value) {
        setPropertyOnProxy(property, value)
            .then((response) => {
                switch (property) {
                    case "speed":
                        setProxyProperties((prevState) => ({
                            ...prevState, // copy existing object
                            speed: value, // update speed
                        }));
                        break;
                    case "sendingInterval":
                        setProxyProperties((prevState) => ({
                            ...prevState, // copy existing object
                            sendingInterval: value, // update sendingInterval
                        }));
                        break;
                    case "fetchingInterval":
                        setProxyProperties((prevState) => ({
                            ...prevState, // copy existing object
                            fetchingInterval: value, // update fetching
                        }));
                        break;
                    default:
                        break;
                }
                console.log(
                    `Property '${property}' set to value '${value}' - '${response}'`
                );
            })
            .catch((error) => {
                console.error(error);
            });
    }

    // --- KEY LISTENERS ---------------------------------------------------------------------------------
    function detectKeyDown(e) {
        console.log("Clicked: " + e.key);
        if (e.key === "w") commandRobot("forward");
        else if (e.key === "s") commandRobot("backward");
        else if (e.key === "a") commandRobot("left");
        else if (e.key === "d") commandRobot("right");
    }

    function detectKeyUp(e) {
        console.log("Lifted: " + e.key);
        setSpeed({
            left: 0,
            right: 0,
        });
        if (e.key === "w") commandRobot("nothing");
        else if (e.key === "s") commandRobot("nothing");
        else if (e.key === "a") commandRobot("nothing");
        else if (e.key === "d") commandRobot("nothing");
    }

    const [speed, setSpeed] = useState({
        left: 0,
        right: 0,
    });

    function updateSpeedIndicator(command) {
        // console.log("CommandToSend: " + command);
        switch (command) {
            case "forward":
                setSpeed({
                    left: proxyProperties.speed,
                    right: proxyProperties.speed,
                });
                break;
            case "backward":
                setSpeed({
                    left: -proxyProperties.speed,
                    right: -proxyProperties.speed,
                });
                break;
            case "left":
                setSpeed({
                    left: -proxyProperties.speed,
                    right: proxyProperties.speed,
                });
                break;
            case "right":
                setSpeed({
                    left: proxyProperties.speed,
                    right: -proxyProperties.speed,
                });
                break;
        }
    }

    // --- OTHER METHODS ------------------------------------------------------------------------------------

    useEffect(() => {
        if (proxyIsConnected) {
            window.addEventListener("keydown", detectKeyDown, true);
            window.addEventListener("keyup", detectKeyUp, true);
        }

        return () => {
            window.removeEventListener("keyup", detectKeyUp, true);
            window.removeEventListener("keydown", detectKeyDown, true);
        };
    }, [proxyIsConnected]);

    function handleProxyChange() {
        proxyIsConnected ? disconnectProxySocket() : connectProxySocket();
        setProxyIsConnected(!proxyIsConnected);
    }

    function handleVideoChange() {
        setVideoIsConnected(!videoIsConnected);
    }

    function handleLocalFirstPropertiesChange(typeOfProperty, value) {
        console.log("--- Local first properties has been changed ---");
        console.log(
            `--> Property '${typeOfProperty}' setting to value '${value}'`
        );
        setProxyProperties((prevState) => ({
            ...prevState, // copy existing object
            [typeOfProperty]: value,
        }));
    }
    function handleProxyFirstPropertiesChange(typeOfProperty, value, min, max) {
        // If it is not speed or any of the interval, we need to set the ustate var value immediately
        // Thnks to this, StompJS will be able to use these values on starting the connection
        console.log("--- Proxy first Properties has been changed ---");
        var temp = value;
        if (value < min) {
            console.log(
                " --->>> value is too small ('" +
                    value +
                    "'). Changing it to min '" +
                    min +
                    "'"
            );
            temp = min;
        } else if (value > max) {
            console.log(
                " --->>> value is too big ('" +
                    value +
                    "'). Changing it to max '" +
                    max +
                    "'"
            );
            temp = max;
        }

        setProperty(typeOfProperty, temp);
    }

    // --- RETURN ------------------------------------------------------------------------------------
    return (
        <div className="bodyWrapper">
            <Controls
                ipAdress={proxyProperties.robotIP}
                videoIsConnected={videoIsConnected}
                setVideoIsConnected={setVideoIsConnected}
                proxyIsConnected={proxyIsConnected}
                connectProxySocket={() => connectProxySocket()}
                disconnectProxySocket={() => disconnectProxySocket()}
                handleProxyChange={handleProxyChange}
                handleVideoChange={handleVideoChange}
                // ---
                proxyProperties={proxyProperties}
                handleProxyFirstPropertiesChange={
                    handleProxyFirstPropertiesChange
                }
                handleLocalFirstPropertiesChange={
                    handleLocalFirstPropertiesChange
                }
            />

            <Info
                speedL={data.speedFrontLeft}
                speedR={data.speedFrontRight}
                battery={data.batLevel}
                current={data.current}
                odometryLeft={data.odometryLeft}
                odometryRight={data.odometryRight}
            />

            <Ir name="ir1" data={data.irLeftFront} />

            <Video
                robotIP={proxyProperties.robotIP}
                data={data}
                videoIsConnected={videoIsConnected}
            />

            <Ir name="ir2" data={data.irRightFront} />
        </div>
    );
}
