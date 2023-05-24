package com.company.proxy;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import static java.lang.System.out;

public class SendingThread extends Thread{
    private byte[] dataToSend;
    private String command;
    private int ROBOTSPEED = 150;
    private int SENDINGINTERVAL = 25;


    private boolean shouldISend;

    private Socket sendingSocket;
    private OutputStream sendingOutputStream;


    public SendingThread(String ROBOT_IP, int PORT) throws IOException {
        sendingSocket = new Socket();
        sendingSocket.connect(new InetSocketAddress(ROBOT_IP, PORT), 3000);
        sendingSocket.setKeepAlive(true);
        this.sendingOutputStream = sendingSocket.getOutputStream();
        this.command = "nothing";
        this.dataToSend = giveMeNothing();
        out.println("Sending thread made up, socket: +"+sendingSocket.toString());
    }

    public void setDataToSend(byte[] dataToSend) {
        this.dataToSend = dataToSend;
    }


    @Override
    public void run() {
        while(shouldISend){
            try {
                // Sending command
                sendingOutputStream.write(dataToSend);
                sendingOutputStream.flush();

                Thread.sleep(SENDINGINTERVAL);
            } catch (IOException e){
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }



    //    ROBOT COMMANDS
    public void forward() {
        System.out.println("Forward: " + ROBOTSPEED);
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
        System.out.println("Backward: " + ROBOTSPEED);
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
        System.out.println("Rotate " + dir + ", ROBOTSPEED: " + ROBOTSPEED);
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

    // SETTERS

    public void setShouldISend(boolean shouldISend) {
        this.shouldISend = shouldISend;
    }

    public Boolean setROBOTSPEED(int ROBOTSPEED) {
        this.ROBOTSPEED = ROBOTSPEED;
        return true;
    }

    public int getSENDINGINTERVAL() {
        return SENDINGINTERVAL;
    }

    public Boolean setSENDINGINTERVAL(int SENDINGINTERVAL) {
        this.SENDINGINTERVAL = SENDINGINTERVAL;
        return true;
    }
}
