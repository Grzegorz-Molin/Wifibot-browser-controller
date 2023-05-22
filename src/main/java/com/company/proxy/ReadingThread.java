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
    private RobotData rdata;
    private String robotIP;
    private int robotPort;
    private Socket fetchingSocket;
    static InputStream fetchingInputStream;

    private int FETCHINGINTERVAL = 800;

    @Autowired
    private ApplicationContext context;

    public ReadingThread(String robotIP, int robotPort) {

        //  Adding a reference to (already existing) Client controller to be able to pass the robot data to client
        this.clientController = CustomContextAware.getContext().getBean(ClientController.class);

        this.robotIP = robotIP;
        this.robotPort = robotPort;
        rdata = new RobotData();
        out.println("[Server] Reading thread made up, robot iP: " + this.robotIP + ", fetching interval: " + this.getFETCHINGINTERVAL() + "ms");
    }

    @Override
    public void run() {
        while (shouldIRead) {
            try {
                fetchingSocket = new Socket();
                fetchingSocket.connect(new InetSocketAddress(robotIP, robotPort), 3000);
                fetchingInputStream = fetchingSocket.getInputStream();

                // Receiving data
                byte[] buffer = new byte[21];
                out.println("Bytes:"+Arrays.toString(buffer));

                int bytesRead = fetchingInputStream.read(buffer, 0, buffer.length);
                out.println("Bytes2:"+ Arrays.toString(buffer));
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
            } finally {
                if (fetchingSocket != null) {
                    try {
                        fetchingSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

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
