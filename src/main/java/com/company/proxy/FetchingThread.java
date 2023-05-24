package com.company.proxy;

import com.company.proxy.controller.ClientController;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import static java.lang.System.out;

public class FetchingThread extends Thread {
    private String ROBOT_IP = "192.168.1.106";
    private int PORT = 15020;
    private Boolean botConnected = false;

    private SendingThread sendingThread;

    private int FETCHINGINTERVAL = 1000;
    private boolean shouldIRead;
    private Socket socket;
    private InputStream inputStream;
    private RobotData rdata;

    private ClientController clientController;

    public FetchingThread() throws IOException {

        //  Adding a reference to (already existing) Client controller to be able to pass the robot data to client
        this.clientController = CustomContextAware.getContext().getBean(ClientController.class);
        this.rdata = new RobotData();
        this.sendingThread = new SendingThread(ROBOT_IP, PORT);
        this.sendingThread.setShouldISend(true);
        this.sendingThread.start();
        shouldIRead = true;
        out.println("[Server] Reading thread made up");
    }

    @Override
    public void run() {
        while (shouldIRead) {
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(ROBOT_IP, PORT), FETCHINGINTERVAL);
                socket.setKeepAlive(true);


                // Initialize Input a and Output streams
                inputStream = socket.getInputStream();

                // Reading data
                byte[] buffer = new byte[21];
                out.println("buffer  made...");
                int bytesRead = inputStream.read(buffer, 0, buffer.length);
                out.println("Bytes read...");
                rdata.setAllData(buffer);
                out.println(rdata.toString());
                clientController.sendRobotDataToClient(rdata.getAllData());

                botConnected = true;
                try {
                    if (inputStream != null) inputStream.close();
                    if (socket != null) socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Waiting the intarval time
                Thread.sleep(FETCHINGINTERVAL);
            } catch (IOException e) {
                e.printStackTrace();
                out.println("\nCheck if:  \n   1. You are connected to the right network \n   2. The robot is ON\n   3. The robot has not booted yet(in that case the green light blinking)");
                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }
    }

    public void pingRobot(){
        String host = "http://"+ROBOT_IP;
        try {
            InetAddress inetAddress = InetAddress.getByName(host);

            System.out.println("Pinging " + host + " [" + inetAddress.getHostAddress() + "]");

            if (inetAddress.isReachable(5000)) {
                System.out.println("Host is reachable.");
            } else {
                System.out.println("Host is not reachable.");
            }
        } catch (UnknownHostException e) {
            System.out.println("Unknown host: " + host);
        } catch (IOException e) {
            System.out.println("Error while pinging the host: " + e.getMessage());
        }
    }

    public void disconnectFromRobot() throws IOException {
        try {
            if (inputStream != null) inputStream.close();
            if (sendingThread != null){
                sendingThread.setShouldISend(false);
                sendingThread.interrupt();
            }
            if (socket != null) socket.close();
            botConnected = false;
            setShouldIRead(false);
            out.println("[Server] Communication with robot stopped");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void commandRobot(String message) {
        if (message.equals("nothing")) sendingThread.nothing();
        else if (message.equals("forward")) sendingThread.forward();
        else if (message.equals("backward")) sendingThread.backward();
        else if (message.equals("left")) sendingThread.direction("left");
        else if (message.equals("right")) sendingThread.direction("right");
    }

    public Boolean setProperty(String property, int value) {
        out.println("[Server]property:  " + property + ", value " + value);
        Boolean result = false;
        if (property.equals("speed")) {
            result = sendingThread.setROBOTSPEED(value);
        } else if (property.equals("FETCHINGINTERVAL")) {
            result = setFETCHINGINTERVAL(value);
        } else if (property.equals("robotPort")) {
            result = setPORT(value);
        }

        return result;
    }

    public Boolean setProperty(String property, String value) {
        out.println("[Server] property string:  " + property + ", value " + value);
        Boolean result = false;
        if (property.equals("robotIP")) {
            result = setRobotIp(String.valueOf(value));
        }
        return result;
    }

//    SETTERS

    public boolean setFETCHINGINTERVAL(int FETCHINGINTERVAL) {
        this.FETCHINGINTERVAL = FETCHINGINTERVAL;
        return true;
    }

    public Boolean setRobotIp(String robotIp) {
        this.ROBOT_IP = robotIp;
        return true;
    }

    public Boolean setPORT(int PORT) {
        this.PORT = PORT;
        return true;
    }

    public void setShouldIRead(boolean b) {
        this.shouldIRead = b;
    }
}
