package com.company.proxy;

import com.company.proxy.controller.ClientController;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.*;
import java.util.Arrays;

import static com.company.proxy.Main.print;
import static com.company.proxy.Main.printAdvice;

public class FetchingThread extends Thread {
    private int fetchingPort;

    private int fetchingInterval;
    private boolean shoulICommunicate;
    private DatagramSocket socket;
    private InetAddress address;
    private RobotData rdata;

    private ClientController clientController;

    public void setUPThread(DatagramSocket socket, InetAddress address, int fetchingPort, int fetchingInterval) throws IOException {
        print("Creating new Fetching thread...");

        //  Adding a reference to (already existing) Client controller to be able to pass the robot data to client
        this.clientController = CustomContextAware.getContext().getBean(ClientController.class);
        this.fetchingPort = fetchingPort;
        this.fetchingInterval = fetchingInterval;
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
                DatagramPacket dataMessagePacket = new DatagramPacket(dataMessage, dataMessage.length, address, fetchingPort);
                socket.send(dataMessagePacket);

                // Receive data
                byte[] buffer = new byte[21];
                DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(receivePacket);

                // Process data
                rdata.setAllData(buffer);
                // print(rdata.toString());
                clientController.sendRobotDataToClient(rdata.getAllData());

                // Sleep the interval time
                Thread.sleep(fetchingInterval);
            } catch (IOException e) {
                e.printStackTrace();
                printAdvice();
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

    public void terminateConnection() {
        if (socket != null) {
            setShouldICommunicate(false);
            socket.disconnect();
        }
    }

    public void setShouldICommunicate(boolean b) {
        this.shoulICommunicate = b;
    }

}
