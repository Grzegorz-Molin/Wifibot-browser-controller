package com.company.proxy;

import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext;
import com.company.proxy.controller.ClientController;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

import static com.company.proxy.Main.print;

public class SendingThread extends Thread{
    private String ROBOT_IP = "192.168.1.106";
    private int PORT = 15000;

    private int SENDINGINTERVAL = 50;
    private boolean shouldISend;
    private DatagramSocket socket;
    private InetAddress address;

    // Sending
    private byte[] dataToSend;
    private String command;
    private int ROBOTSPEED = 150;

    private ClientController clientController;

    public SendingThread(DatagramSocket socket, InetAddress address) throws IOException {
        print("Creating new Fetching thread...");

        //  Adding a reference to (already existing) Client controller to be able to pass the robot data to client
        this.clientController = CustomContextAware.getContext().getBean(ClientController.class);

        this.socket = socket;
        this.address = address;
        this.command = "nothing";

        this.dataToSend = giveMeNothing();
        print("Sending thread made up!");
    }

    @Override
    public void run(){
        while (shouldISend){
            try {
                // Send the command
                print("Sending: "+command+" "+ Arrays.toString(dataToSend));
                DatagramPacket commandPacket = new DatagramPacket(dataToSend, dataToSend.length, address, PORT);
                socket.send(commandPacket);

                // Sleep the interval time
                Thread.sleep(SENDINGINTERVAL);
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

    //    ROBOT COMMANDS
    public void forward() {
//        System.out.println("Forward: " + ROBOTSPEED);
        byte[] newCommand = new byte[9];
        newCommand[0] = (byte) 0xff;     //255 (= -1)
        newCommand[1] = (byte) 0x07;     //
        newCommand[2] = (byte) ROBOTSPEED;
        newCommand[3] = (byte) (ROBOTSPEED >> 8);
        newCommand[4] = (byte) ROBOTSPEED;
        newCommand[5] = (byte) (ROBOTSPEED >> 8);
        newCommand[6] = (byte) 0x53;     //forward

        CRC16 crc = new CRC16();
        for (int i = 1; i < 7; i++) {
            crc.update(newCommand[i]);
        }

        newCommand[7] = (byte) crc.getValue();
        newCommand[8] = (byte) (crc.getValue() >> 8);
        setDataToSend(newCommand);
        setCommand("forward");
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void backward() {
//        System.out.println("Backward: " + ROBOTSPEED);
        byte[] newCommand = new byte[9];
        newCommand[0] = (byte) 0xff;        //255
        newCommand[1] = (byte) 0x07;        //size
        newCommand[2] = (byte) ROBOTSPEED;    //left ROBOTSPEED
        newCommand[3] = (byte) (ROBOTSPEED >> 8);
        newCommand[4] = (byte) ROBOTSPEED;    //right ROBOTSPEED
        newCommand[5] = (byte) (ROBOTSPEED >> 8);
        newCommand[6] = (byte) 0x03;        //backward

        CRC16 crc = new CRC16();
        for (int i = 1; i < 7; i++) {
            crc.update(newCommand[i]);
        }

        newCommand[7] = (byte) crc.getValue();        //crc
        newCommand[8] = (byte) (crc.getValue() >> 8);
        setDataToSend(newCommand);
        setCommand("backward");
    }

    public void direction(String dir) {
//        System.out.println("Rotate " + dir + ", ROBOTSPEED: " + ROBOTSPEED);
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
        setCommand(dir);
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
        setCommand("nothing");
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

    // Other methods

    public void commandRobot(String message) {
        if (message.equals("nothing")) nothing();
        else if (message.equals("forward")) forward();
        else if (message.equals("backward")) backward();
        else if (message.equals("left")) direction("left");
        else if (message.equals("right")) direction("right");
    }

    public void terminateConnection(){
        if (socket != null) {
            setShouldISend(false);
            socket.disconnect();
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

    public void setShouldISend(boolean shouldISend) {
        this.shouldISend = shouldISend;
    }

    public boolean setRobotIp(String ROBOT_IP) {
        this.ROBOT_IP = ROBOT_IP;
        return true;
    }

    public boolean setPort(int PORT) {
        this.PORT = PORT;
        return true;
    }

    public void setDataToSend(byte[] dataToSend) {
        this.dataToSend = dataToSend;
    }

    public boolean setSendingInterval(int SENDINGINTERVAL) {
        this.SENDINGINTERVAL = SENDINGINTERVAL;
        return true;
    }

    public boolean setRobotSpeed(int ROBOTSPEED) {
        this.ROBOTSPEED = ROBOTSPEED;
        return true;
    }
}
