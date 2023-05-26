package com.company.proxy;

import com.company.proxy.controller.ClientController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.*;
import java.net.*;

import static java.lang.System.out;

@SpringBootApplication
public class Main {

    private String robotIP = "192.168.1.106";

    private int fetchingPort = 15010;
    private int fetchingInterval = 250;

    private int sendingPort = 15000;
    private int sendingInterval = 50;

    public static ConfigurableApplicationContext context;

    static DatagramSocket socket;
    static DatagramSocket sendingSocket;
    static InetAddress address;

    // Threads
    static FetchingThread fetchingThread;
    static SendingThread sendingThread;

    public Boolean connectToRobot() {
        boolean result = false;
        try {
            // --- SENDING THREAD ---
            if (!sendingThread.isAlive()) {
                sendingThread = null;
                sendingThread = new SendingThread();
            }
            sendingSocket = new DatagramSocket();
            address = InetAddress.getByName(robotIP);

            sendingThread.setUpThread(sendingSocket, address, sendingPort, sendingInterval);
            sendingThread.setShouldISend(true);
            sendingThread.start();

            // --- FETCHING THREAD ---
            if (!fetchingThread.isAlive()) {
                fetchingThread = null;
                fetchingThread = new FetchingThread();
            }
            socket = new DatagramSocket();
            socket.setSoTimeout(3000);

            // Prepare the data to send
            String initMessage = "init";
            byte[] initData = initMessage.getBytes();

            // Send the "init" message
            DatagramPacket initPacket = new DatagramPacket(initData, initData.length, address, fetchingPort);
            socket.send(initPacket);

            // Wait for the "ok" response
            byte[] okBuffer = new byte[2];
            DatagramPacket okPacket = new DatagramPacket(okBuffer, okBuffer.length);
            socket.receive(okPacket);

            // Check if the received message is "ok"
            if (new String(okBuffer).equals("ok")) {
                // Initialization successful
                print("Initialization of UDP socket successful, 'OK' received, communication begins...");

                // Initialize thread
                fetchingThread.setUPThread(socket, address, fetchingPort, fetchingInterval);
                fetchingThread.setShouldICommunicate(true);
                fetchingThread.start();
                print("Connected! Socket made, Streams and Threads initialized");
                result = true;
            }

        } catch (SocketTimeoutException e) {
            // Timeout occurred while waiting for the "ok" response
            print("Timeout occurred while waiting for 'OK' response.");
            result = false;
        } catch (IOException e) {
            e.printStackTrace();
            print("\nCheck if:  \n   1. You are connected to the right network \n   2. The robot is ON\n   3. The robot has not booted yet(in that case the green light blinking)");
            if (fetchingThread != null) {
                fetchingThread.setShouldICommunicate(false);
                fetchingThread.interrupt();
                socket.close();
            }
            result = false;
        }
        return result;
    }


    public void disconnectFromRobot() throws IOException {
        if (sendingThread != null) {
            sendingThread.terminateConnection();
        }

        if (fetchingThread != null) {
            fetchingThread.setShouldICommunicate(false);
            fetchingThread.interrupt();
        }
        if (socket != null) socket.close();
        print("Socket closed");
    }

    public void commandRobot(String message) {
        if (message.equals("nothing")) sendingThread.nothing();
        else if (message.equals("forward")) sendingThread.forward();
        else if (message.equals("backward")) sendingThread.backward();
        else if (message.equals("left")) sendingThread.direction("left");
        else if (message.equals("right")) sendingThread.direction("right");
    }

    public Boolean setProperty(String property, String value) {
        print("property:  " + property + ", value " + value);
        Boolean result = false;
        if (property.equals("robotIP")) {
            result = setRobotIP(value);
        } else if (property.equals("robotSendingPort")) {
            result = setSendingPort(Integer.parseInt(value));
        } else if (property.equals("robotFetchingPort")) {
            result = setFetchingPort(Integer.parseInt(value));
        // Intervals
        } else if (property.equals("sendingInterval")) {
            result = setSendingInterval(Integer.parseInt(value));
            if (sendingThread != null) {
                result = sendingThread.setSendingInterval(Integer.parseInt(value));
            }
        } else if (property.equals("fetchingInterval")) {
            result = setFetchingInterval(Integer.parseInt(value));
            if (fetchingThread != null) {
                result = setFetchingInterval(Integer.parseInt(value));
            }
        } else if (property.equals("speed")) {
            if (sendingThread != null) {
                result = sendingThread.setRobotSpeed(Integer.parseInt(value));
            }
        }
        return result;
    }

    public static void print(String message) {
        out.println("[Server] " + message);
    }

    public static void printAdvice(){
        print("\nCheck if:  \n   1. You are connected to the right network \n   2. The robot is ON\n   3. The robot has not booted yet(in that case the green light blinking)");
    }

    // SETTERS
    public Boolean setRobotIP(String robotIp) {
        this.robotIP = robotIp;
        return true;
    }

    public Boolean setFetchingPort(int fetchingPort) {
        this.fetchingPort = fetchingPort;
        return true;
    }

    public boolean setFetchingInterval(int fetchingInterval) {
        this.fetchingInterval = fetchingInterval;
        return true;
    }

    public boolean setSendingPort(int sendingPort) {
        this.sendingPort = sendingPort;
        return true;
    }

    public boolean setSendingInterval(int sendingInterval) {
        this.sendingInterval = sendingInterval;
        return true;
    }

    // MAIN
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
        sendingThread = new SendingThread();
        fetchingThread = new FetchingThread();
    }


}
