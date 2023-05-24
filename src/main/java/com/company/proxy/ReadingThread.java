package com.company.proxy;

import com.company.proxy.controller.ClientController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.System.out;

public class ReadingThread extends Thread {
    private Boolean shouldIRead;
    private ClientController clientController;
    private Main main;
    private RobotData rdata;
    private String robotIP;
    private int robotPort;
    private Socket socket;
    private InputStream fetchingInputStream;

    private int FETCHINGINTERVAL = 800;

    @Autowired
    private ApplicationContext context;

    public ReadingThread(Socket socket, String robotIP, int robotPort) {

        //  Adding a reference to (already existing) Client controller to be able to pass the robot data to client
        this.clientController = CustomContextAware.getContext().getBean(ClientController.class);
        this.main = CustomContextAware.getContext().getBean(Main.class);
        this.socket = socket;
        this.robotIP = robotIP;
        this.robotPort = robotPort;
        rdata = new RobotData();
        out.println("[Server] Reading thread made up, robot iP: " + this.robotIP + ", fetching interval: " +
                this.getFETCHINGINTERVAL() + "ms, socket existin: " + socket.toString());
    }

    public void setsocket(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        while (shouldIRead &&!Thread.currentThread().isInterrupted()) {
            out.println("I'm trying...");
            try {
                if (socket != null && socket.isConnected()) {
                    out.println("Creating a new input stream...");
                    fetchingInputStream = socket.getInputStream();
                } else {
                    out.println("Socket not existing or not connected!");
                    main.checkForDisconnectedSocket();
                    setShouldIRead(false);
                }

                // Receiving data
                byte[] buffer = new byte[21];
                int bytesRead = fetchingInputStream.read(buffer, 0, buffer.length);
                if (bytesRead == -1) {
                    // Connection closed
                    break;
                }
                rdata.setAllData(buffer);
                out.println(rdata.toString());

                clientController.sendRobotDataToClient(rdata.getAllData());
                Thread.sleep(FETCHINGINTERVAL);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                setShouldIRead(false);
                this.interrupt();
            } finally {
                if (fetchingInputStream != null) {
                    try {
                        fetchingInputStream.close();
                        main.checkForDisconnectedSocket();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
        }
    }

    public void stopThread() {
        shouldIRead = false;
        interrupt();
    }

    // SETTERS

    public int getFETCHINGINTERVAL() {
        return FETCHINGINTERVAL;
    }

    public Boolean setFETCHINGINTERVAL(int FETCHINGINTERVAL) {
        this.FETCHINGINTERVAL = FETCHINGINTERVAL;
        return true;
    }


    public void setShouldIRead(Boolean shouldIRead) {
        this.shouldIRead = shouldIRead;
    }


    public Boolean getShouldIRead() {
        return shouldIRead;
    }
}
