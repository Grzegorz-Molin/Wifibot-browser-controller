import "./App.css";
import Video from "./components/Video";
import Controls from "./components/Controls";
import Info from "./components/Info";
import Ir from "./components/Ir";
import { useEffect, useState } from "react";
import SockJS from "sockjs-client";
import { over } from "stompjs";

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

    // ---------------------------------------------------------------------------------------

    function parseResponse(response) {
        const odometryLeftBigger =
            response.odometryLeft > data.odometryLeft
                ? response.odometryLeft
                : data.odometryLeft;
        const odometryRightBigger =
            response.odometryRight > data.odometryRight
                ? response.odometryRight
                : data.odometryRight;
        setData((prevData) => ({
            ...prevData,
            speedFrontLeft: response.speedFrontLeft,
            speedFrontRight: response.speedFrontRight,
            batLevel: response.batLevel,
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

    // --- SOCK JS and CONNECTIVITY------------------------------------------------------------------------------------
    // Connecting first to the proxy
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

    // Disconnecting from proxy (and for safety also from the robot)
    function disconnectProxySocket() {
        if (stompClient !== null) {
            disconnectFromRobot();
            stompClient.disconnect();
        }
        setProxyIsConnected(false);
    }

    // Proxy is connected, now starting communication with the robot
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

    // Stopping communication with robot, proxy connections stays
    function disconnectFromRobot() {
        stompClient.send("/app/disconnectFromRobot", {});
    }

    // Next 3 callback functions are invoked on specific events
    // onConnected() is invoked after establishming connection to the proxy server
    function onConnected() {
        setProxyIsConnected(true);
        connectToRobot();
        stompClient.subscribe("/topic/bot", onMessageReceived);
    }

    // Invoked when message from proxy arrives
    function onMessageReceived(payload) {
        let payloadData = JSON.parse(payload.body);

        if (payloadData.message != null) {
            if (payloadData.message === "[Server] [Robot not connected]") {
                disconnectProxySocket();
            } else if (payloadData.message === "[Server] [Robot connected]") {
                // do nothing
            }
        } else {
            parseResponse(payloadData);
        }
    }

    // Invoked on any error of the frontend - proxy server connection
    function onError(err) {
        console.log(err);
        console.log(
            "Also check if: \n1. Wifibot is running\n2. Proxy server is running\n3.You are connected to the right network"
        );
        setProxyIsConnected(false);
    }

    // Commanding robot forward, backward, etc.
    function commandRobot(commandToSend) {
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

    // Setting useState property here in App.js
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
            })
            .catch((error) => {
                console.log(error);
            });
    }

    // --- KEY LISTENERS ---------------------------------------------------------------------------------
    function detectKeyDown(e) {
        if (e.key === "w") commandRobot("forward");
        else if (e.key === "s") commandRobot("backward");
        else if (e.key === "a") commandRobot("left");
        else if (e.key === "d") commandRobot("right");
    }

    function detectKeyUp(e) {
        if (e.key === "w") commandRobot("nothing");
        else if (e.key === "s") commandRobot("nothing");
        else if (e.key === "a") commandRobot("nothing");
        else if (e.key === "d") commandRobot("nothing");
    }

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

    // --- OTHER METHODS ------------------------------------------------------------------------------------

    function handleProxyChange() {
        proxyIsConnected ? disconnectProxySocket() : connectProxySocket();
        setProxyIsConnected(!proxyIsConnected);
    }

    function handleVideoChange() {
        setVideoIsConnected(!videoIsConnected);
    }

    // Changing "proxy" properties first here - these that need to be set BEFORE connection with the robot starts
    function handleLocalFirstPropertiesChange(typeOfProperty, value) {
        setProxyProperties((prevState) => ({
            ...prevState, // copy existing object
            [typeOfProperty]: value,
        }));
    }

    // Setting propertie right on the proxy - these that are changed DURING the connection with the robot
    function handleProxyFirstPropertiesChange(typeOfProperty, value, min, max) {
        var temp = value;
        if (value < min) {
            temp = min;
        } else if (value > max) {
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
