package com.company.proxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

import static java.lang.System.out;

@SpringBootApplication
public class Main {

    private static String ROBOT_IP = "192.168.1.106";
    private static int PORT = 15020;

    public static ConfigurableApplicationContext context;

    // Status variables
    static Boolean botConnected = false;
    static String actualCommand = "nothing";

    static Socket socket;
    static InputStream inputStream;
    static DataInputStream dataInputStream;
    static OutputStream outputStream;
    static DataOutputStream dataOutputStream;

    // Threads
    static ReadingThread readingThread;
    static SendingThread sendingThread;

    public static Boolean communicateWithRobot() throws IOException {
        try {
//Output stream for commands
            socket = new Socket();
            socket.connect(new InetSocketAddress(ROBOT_IP, PORT), 3000);
            socket.setKeepAlive(true);
            outputStream = socket.getOutputStream();
            sendingThread = new SendingThread(outputStream, socket);
            sendingThread.setShouldISend(true);
            sendingThread.start();

            // Input data thread

            readingThread = new ReadingThread(ROBOT_IP, PORT);
            readingThread.setShouldIRead(true);
            readingThread.start();

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

            botConnected = true;
            out.println("[Connected, Socket made, Streams and Threads initialized]");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            out.println("\nCheck if:  \n   1. You are connected to the right network \n   2. The robot is ON\n  " +
                    " 3. The robot has not booted yet(in that case the green light blinking) \n   The Access Point (router) is ON");
            out.println("You can also try to do following:\n   1. Restart Wifibot \n   2. Restart Access Point (router)" +
                    "\n   3. Restart your application");
            socket.close();
            if (sendingThread != null) sendingThread.interrupt();
            if (readingThread != null) readingThread.interrupt();
            return false;
        }
    }


    public static void disconnectFromRobot() throws IOException {
        try {
            actualCommand = "stop";
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
            if (socket != null) socket.close();
            botConnected = false;
            if (readingThread != null) readingThread.setShouldIRead(false);
            if (sendingThread != null) sendingThread.setShouldISend(false);
            out.println("Socket closed");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void commandRobot(String message) {
        if (message.equals("nothing")) sendingThread.nothing();
        else if (message.equals("forward")) sendingThread.forward();
        else if (message.equals("backward")) sendingThread.backward();
        else if (message.equals("left")) sendingThread.direction("left");
        else if (message.equals("right")) sendingThread.direction("right");
    }

    public static Boolean setProperty(String property, int value) {
//        out.println("[Server]property:  " + property + ", value " + value);
        Boolean result = false;
        if (property.equals("speed")) {
            if (sendingThread != null) {
                result = sendingThread.setROBOTSPEED(value);
            }
        } else if (property.equals("sendingInterval")) {
            if (sendingThread != null) {
                result = sendingThread.setSENDINGINTERVAL(value);
            }
        } else if (property.equals("fetchingInterval")) {
            if (readingThread != null) {
                result = readingThread.setFETCHINGINTERVAL(value);
            }
        } else if (property.equals("robotPort")) {
            result = setPORT(value);
        }

        return result;
    }

    public static Boolean setProperty(String property, String value) {
//        out.println("[Server] property string:  " + property + ", value " + value);
        Boolean result = false;
        if (property.equals("robotIP")) {
            result = setRobotIp(String.valueOf(value));
        }
        return result;
    }

    public static Boolean setRobotIp(String robotIp) {
        ROBOT_IP = robotIp;
        return true;
    }

    public static Boolean setPORT(int PORT) {
        Main.PORT = PORT;
        return true;
    }


    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }


}
