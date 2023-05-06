package com.company.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.out;

public class ReadingThread extends Thread {
    InputStream inputStream;
    private Boolean shouldIRead;

    public ReadingThread(InputStream inputStream) {

        this.inputStream = inputStream;
        shouldIRead = true;
        out.println("[Reading thread made up]");
    }

    @Override
    public void run() {
        out.println("[Receiving]");

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

                    System.out.print("Received longs: " + dataInLongs + "\r");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
