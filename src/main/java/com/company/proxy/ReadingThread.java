package com.company.proxy;

import com.company.proxy.controller.ClientController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.out;

public class ReadingThread extends Thread {
    InputStream inputStream;
    private Boolean shouldIRead;
    private ClientController clientController;

    private int FETCHINGINTERVAL = 800;

    @Autowired
    private ApplicationContext context;

    public ReadingThread(InputStream inputStream) {

        this.inputStream = inputStream;
        //  Adding a reference to (already existing) Client controller to be able to pass the robot data to client
        this.clientController = CustomContextAware.getContext().getBean(ClientController.class);
        shouldIRead = true;
        out.println("[Server] Reading thread made up");
    }

    @Override
    public void run() {
        out.println("[Server] Receiving"+shouldIRead);

        while (shouldIRead) {
            try {
                // Receiving data
                if (inputStream.available() > 0) {
                    byte[] buffer = new byte[21];
                    int bytesRead = inputStream.read(buffer, 0, buffer.length);

                    // convert binary data to hexadecimal strings
                    List<String> dataInBytes = new ArrayList<>();
                    for (byte b : buffer) {
                        dataInBytes.add(String.format("%02X", b));
                    }

                    // converting into longs
                    List<Long> dataInLongs = new ArrayList<>();
                    for (String string : dataInBytes) {
                        dataInLongs.add(Long.parseLong(string, 16));
                    }

                    System.out.println("[Server] Received longs: " + dataInLongs + "\r");
                    clientController.sendRobotDataToClient(dataInLongs);
                    Thread.sleep(FETCHINGINTERVAL);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
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
    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public void setShouldIRead(Boolean shouldIRead) {
        this.shouldIRead = shouldIRead;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public Boolean getShouldIRead() {
        return shouldIRead;
    }
}
