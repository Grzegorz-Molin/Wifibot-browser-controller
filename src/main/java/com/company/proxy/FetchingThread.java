package com.company.proxy;

import com.company.proxy.controller.ClientController;

import java.io.*;
import java.net.*;
import java.util.Arrays;

import static com.company.proxy.Main.print;

public class FetchingThread extends Thread {
    private String ROBOT_IP = "192.168.1.106";
    private int PORT = 15010;

    private int FETCHINGINTERVAL = 200;
    private boolean shoulICommunicate;
    private DatagramSocket socket;
    private InetAddress address;
    private RobotData rdata;

    // Sending
    private byte[] dataToSend;
    private String command;
    private int ROBOTSPEED = 150;

    private ClientController clientController;

    public FetchingThread(DatagramSocket socket, InetAddress address) throws IOException {
        print("Creating new Fetching thread...");

        //  Adding a reference to (already existing) Client controller to be able to pass the robot data to client
        this.clientController = CustomContextAware.getContext().getBean(ClientController.class);

        this.socket = socket;
        this.address = address;
        this.rdata = new RobotData();

        print("[Server] Reading thread made up");
    }

    @Override
    public void run() {
        while (shoulICommunicate) {
            try {
                // Send the "data" message
                byte[] dataMessage = "data".getBytes();
                DatagramPacket dataMessagePacket = new DatagramPacket(dataMessage, dataMessage.length, address, PORT);
                socket.send(dataMessagePacket);

                // Recceive data
                byte[] buffer = new byte[21];
                DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(receivePacket);

                // Process data
                rdata.setAllData(buffer);
//                print(rdata.toString());
                clientController.sendRobotDataToClient(rdata.getAllData());

                // Sleep the interval time
                Thread.sleep(FETCHINGINTERVAL);
            } catch (IOException e) {
                e.printStackTrace();
                print("\nCheck if:  \n   1. You are connected to the right network \n   2. The robot is ON\n   3. The robot has not booted yet(in that case the green light blinking)");
                terminateConnection();
                break;
            } catch (InterruptedException e) {
                print("Communicating thread Interrupted");
                e.printStackTrace();
                terminateConnection();
                break;
            }
        }
    }

    public void terminateConnection(){
        if (socket != null) {
            setShouldICommunicate(false);
            socket.disconnect();
        }
    }




    public void pingRobot() {
        String host = "http://" + ROBOT_IP;
        try {
            InetAddress inetAddress = InetAddress.getByName(host);

            print("Pinging " + host + " [" + inetAddress.getHostAddress() + "]");

            if (inetAddress.isReachable(5000)) {
                print("Host is reachable.");
            } else {
                print("Host is not reachable.");
            }
        } catch (UnknownHostException e) {
            print("Unknown host: " + host);
        } catch (IOException e) {
            print("Error while pinging the host: " + e.getMessage());
        }
    }


    public Boolean setProperty(String property, String value) {
        print("[Server] property string:  " + property + ", value " + value);
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

    public void setShouldICommunicate(boolean b) {
        this.shoulICommunicate = b;
    }


    public Boolean setROBOTSPEED(int ROBOTSPEED) {
        this.ROBOTSPEED = ROBOTSPEED;
        return true;
    }

    public void setROBOT_IP(String ROBOT_IP) {
        this.ROBOT_IP = ROBOT_IP;
    }

    public void setDataToSend(byte[] dataToSend) {
        this.dataToSend = dataToSend;
    }

}
