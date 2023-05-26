package com.company.proxy;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import static com.company.proxy.Main.print;
import static com.company.proxy.Main.printAdvice;

public class SendingThread extends Thread{
    private int sendingPort;
    private int sendingInterval;
    private boolean shouldISend;
    private DatagramSocket socket;
    private InetAddress address;

    private byte[] dataToSend;
    private int ROBOTSPEED = 130;

    public void setUpThread(DatagramSocket socket, InetAddress address, int sendingPort, int sendingInterval) {
        print("Setting up Fetching thread...");
        this.socket = socket;
        this.address = address;
        this.sendingPort = sendingPort;
        this.sendingInterval = sendingInterval;

        this.dataToSend = giveMeNothing();
        print("Sending thread set up!");
    }

    // Main thread method to be run in cycle
    @Override
    public void run(){
        while (shouldISend){
            try {
                // Send the command
                DatagramPacket commandPacket = new DatagramPacket(dataToSend, dataToSend.length, address, sendingPort);
                socket.send(commandPacket);

                // Sleep the interval time
                Thread.sleep(sendingInterval);
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
            setShouldISend(false);
            socket.disconnect();
        }
        // Re-initialize the thread
        shouldISend = false;
        socket = null;
        address = null;
    }

    // Robot commands
    public void forward() {
        byte[] newCommand = new byte[9];
        newCommand[0] = (byte) 0xff;
        newCommand[1] = (byte) 0x07;
        newCommand[2] = (byte) ROBOTSPEED;
        newCommand[3] = (byte) (ROBOTSPEED >> 8);
        newCommand[4] = (byte) ROBOTSPEED;
        newCommand[5] = (byte) (ROBOTSPEED >> 8);
        newCommand[6] = (byte) 0x53;

        CRC16 crc = new CRC16();
        for (int i = 1; i < 7; i++) {
            crc.update(newCommand[i]);
        }

        newCommand[7] = (byte) crc.getValue();
        newCommand[8] = (byte) (crc.getValue() >> 8);
        setDataToSend(newCommand);
    }

    public void backward() {
        byte[] newCommand = new byte[9];
        newCommand[0] = (byte) 0xff;
        newCommand[1] = (byte) 0x07;
        newCommand[2] = (byte) ROBOTSPEED;
        newCommand[3] = (byte) (ROBOTSPEED >> 8);
        newCommand[4] = (byte) ROBOTSPEED;
        newCommand[5] = (byte) (ROBOTSPEED >> 8);
        newCommand[6] = (byte) 0x03;

        CRC16 crc = new CRC16();
        for (int i = 1; i < 7; i++) {
            crc.update(newCommand[i]);
        }

        newCommand[7] = (byte) crc.getValue();
        newCommand[8] = (byte) (crc.getValue() >> 8);
        setDataToSend(newCommand);
    }

    public void direction(String dir) {
        byte[] newCommand = new byte[9];
        newCommand[0] = (byte) 0xff;
        newCommand[1] = (byte) 0x07;
        newCommand[2] = (byte) ROBOTSPEED;
        newCommand[3] = (byte) (ROBOTSPEED >> 8);
        newCommand[4] = (byte) ROBOTSPEED;
        newCommand[5] = (byte) (ROBOTSPEED >> 8);
        if (dir.equals("right")) {
            newCommand[6] = (byte) 0x43;
        } else {
            newCommand[6] = (byte) 0x13;
        }

        CRC16 crc = new CRC16();
        for (int i = 1; i < 7; i++) {
            crc.update(newCommand[i]);
        }

        newCommand[7] = (byte) crc.getValue();
        newCommand[8] = (byte) (crc.getValue() >> 8);
        setDataToSend(newCommand);
    }

    public void nothing() {
        byte[] newCommand = new byte[9];
        newCommand[0] = (byte) 0xff;
        newCommand[1] = (byte) 0x07;
        newCommand[2] = (byte) 0x00;
        newCommand[3] = (byte) 0x00;
        newCommand[4] = (byte) 0x00;
        newCommand[5] = (byte) 0x00;
        newCommand[6] = (byte) 0x53;

        CRC16 crc = new CRC16();
        for (int i = 1; i < 7; i++) {
            crc.update(newCommand[i]);
        }

        newCommand[7] = (byte) crc.getValue();
        newCommand[8] = (byte) (crc.getValue() >> 8);
        setDataToSend(newCommand);
    }

    public byte[] giveMeNothing() {
        byte[] newCommand = new byte[9];
        newCommand[0] = (byte) 0xff;
        newCommand[1] = (byte) 0x07;
        newCommand[2] = (byte) 0x00;
        newCommand[3] = (byte) 0x00;
        newCommand[4] = (byte) 0x00;
        newCommand[5] = (byte) 0x00;
        newCommand[6] = (byte) 0x53;

        CRC16 crc = new CRC16();
        for (int i = 1; i < 7; i++) {
            crc.update(newCommand[i]);
        }

        newCommand[7] = (byte) crc.getValue();
        newCommand[8] = (byte) (crc.getValue() >> 8);
        return newCommand;
    }


    public void commandRobot(String message) {
        if (message.equals("nothing")) nothing();
        else if (message.equals("forward")) forward();
        else if (message.equals("backward")) backward();
        else if (message.equals("left")) direction("left");
        else if (message.equals("right")) direction("right");
    }


    // Setters
    public void setShouldISend(boolean shouldISend) {
        this.shouldISend = shouldISend;
    }

    public void setDataToSend(byte[] dataToSend) {
        this.dataToSend = dataToSend;
    }

    public boolean setSendingInterval(int newInterval) {
        this.sendingInterval = newInterval;
        return true;
    }

    public boolean setRobotSpeed(int ROBOTSPEED) {
        this.ROBOTSPEED = ROBOTSPEED;
        return true;
    }
}
