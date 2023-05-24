package com.company.proxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.*;
import java.net.*;

import static java.lang.System.out;

@SpringBootApplication
public class Main {

    private static String ROBOT_IP = "192.168.1.106";
    private static int FETCHING_PORT = 15010;

    public static ConfigurableApplicationContext context;

    // Status variables
    static Boolean botConnected = false;
    static String actualCommand = "nothing";

    static DatagramSocket socket;
    static DatagramSocket sendingSocket;
    static InetAddress address;
    static InetAddress sendingAddress;

    // Threads
    static FetchingThread fetchingThread;
    static SendingThread sendingThread;

    public static Boolean connectToRobot() throws IOException {
        print("Connecting...");
        boolean result = false;
        try {
            // --- SENDING THREAD ---
            sendingSocket = new DatagramSocket();
            sendingAddress = InetAddress.getByName(ROBOT_IP); // Replace with the desired IP address
            sendingThread = new SendingThread(sendingSocket, sendingAddress);
            sendingThread.setShouldISend(true);
            sendingThread.start();

            // --- FETCHING THREAD ---
            // Create a DatagramSocket for sending and receiving data
            socket = new DatagramSocket();
            // Prepare the data to send
            String initMessage = "init";
            byte[] initData = initMessage.getBytes();
            address = InetAddress.getByName(ROBOT_IP); // Replace with the desired IP address

            // Send the "init" message
            DatagramPacket initPacket = new DatagramPacket(initData, initData.length, address, FETCHING_PORT);
            socket.send(initPacket);

            // Wait for the "ok" response
            byte[] okBuffer = new byte[2];
            DatagramPacket okPacket = new DatagramPacket(okBuffer, okBuffer.length);
            socket.receive(okPacket);

            // Check if the received message is "ok"
            if (new String(okBuffer).equals("ok")) {
                // Initialization successful
                botConnected = true;
                print("Initialization of UDP socket successful, 'OK' received, communication begins...");

                // Initialize thread
                fetchingThread = new FetchingThread(socket, address);
                fetchingThread.setShouldICommunicate(true);
                fetchingThread.start();
                botConnected = true;
                print("Connected! Socket made, Streams and Threads initialized");
                result = true;
            }

            /* ConnectToRobot() method could have been called by the sending threads after "Broken pipe" error has been
            invoked. Now we have to check, if this is tha case, and if so, recall last called command.
            This behaviour is safe because in the beginning, actualCommand is initialized with "nothing". So if it not
            "nothing", it had to be changed by some other command.
            */
//			if (!actualCommand.equals("nothing")) {
//				out.println("Socket has fallen, reconfiguring sending command");
//				if (actualCommand.equals("forward")) sendingThread.forward();
//				else if (actualCommand.equals("backward")) sendingThread.backward();
//				else if (actualCommand.equals("left")) sendingThread.direction("left");
//				else if (actualCommand.equals("right")) sendingThread.direction("right");
//			}


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


    public static void disconnectFromRobot() throws IOException {
        actualCommand = "stop";
        botConnected = false;
        if (sendingThread != null) {
            sendingThread.setShouldISend(false);
            sendingThread.interrupt();
        }

        if (fetchingThread != null) {
            fetchingThread.setShouldICommunicate(false);
            fetchingThread.interrupt();
        }
        if (socket != null) socket.close();
        print("Socket closed");
    }

    public static void commandRobot(String message) {
        if (message.equals("nothing")) sendingThread.nothing();
        else if (message.equals("forward")) sendingThread.forward();
        else if (message.equals("backward")) sendingThread.backward();
        else if (message.equals("left")) sendingThread.direction("left");
        else if (message.equals("right")) sendingThread.direction("right");
    }

    public static Boolean setProperty(String property, int value) {
        print("property:  " + property + ", value " + value);
        Boolean result = false;
        if (property.equals("speed")) {
            if (sendingThread != null) {
                result = sendingThread.setRobotSpeed(value);
            }
        } else if (property.equals("fetchingInterval")) {
            if (fetchingThread != null) {
                result = fetchingThread.setFETCHINGINTERVAL(value);
            }
        } else if (property.equals("robotPort")) {
            result = setPORT(value);
        }

        return result;
    }


    public static void print(String message) {
        out.println("[Server] " + message);
    }

    public static Boolean setProperty(String property, String value) {
        print("property string:  " + property + ", value " + value);
        Boolean result = false;
        if (property.equals("robotIP")) {
            result = setRobotIp(String.valueOf(value));
        }
        return result;
    }

    // SETTERS
    public static Boolean setRobotIp(String robotIp) {
        ROBOT_IP = robotIp;
        return true;
    }

    public static Boolean setPORT(int FETCHING_PORT) {
        Main.FETCHING_PORT = FETCHING_PORT;
        return true;
    }


    // MAIN
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }


}
