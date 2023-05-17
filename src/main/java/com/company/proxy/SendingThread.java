package com.company.proxy;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

import static com.company.proxy.Main.connectToRobot;
import static java.lang.System.out;

public class SendingThread extends Thread {
    byte[] dataToSend;
    private int ROBOTSPEED = 150;
    private int SENDINGINTERVAL = 25;

    OutputStream outputStream;
    private Boolean shouldISend;

    private String command;
    private Socket socket;

    //    Parameter constructor
    public SendingThread(OutputStream outputStream, Socket socket) {
        this.outputStream = outputStream;
        this.shouldISend = true;
        this.command = "nothing";
        this.dataToSend = giveMeNothing();
        this.socket = socket;
        out.println("[Server] Sending thread made up");

    }

    // Main sending logic inside Run() method. It only sends if the command is not "nothing"
    @Override
    public void run() {
        while (shouldISend) {
            try {
                // Sending data
                outputStream.write(dataToSend);
                outputStream.flush();
//                out.println("[Server] Sending: " + command + ", " + Arrays.toString(dataToSend));
                Thread.sleep(SENDINGINTERVAL);
            }
            // Check for the disconnection of the robot
            catch (SocketException e) {
                if (e.getMessage().equals("[Server] Broken pipe")) {
                    // handle Broken pipe error
                    System.out.println("[Server] Broken pipe error occurred; Reconnecting");
                    try {
                        connectToRobot();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    // handle other SocketException errors
                    e.printStackTrace();
                }
            } catch (Exception e) {
                // handle other exceptions
                e.printStackTrace();
                setShouldISend(false);
                out.println("[Server] Ending sending");
            }
        }
    }

    // Robot commands
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


    // Getters and Setter
    public int getROBOTSPEED() {
        return ROBOTSPEED;
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
    public byte[] getDataToSend() {
        return dataToSend;
    }

    public void setDataToSend(byte[] dataToSend) {
        this.dataToSend = dataToSend;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public Boolean getShouldISend() {
        return shouldISend;
    }

    public void setShouldISend(Boolean shouldISend) {
        this.shouldISend = shouldISend;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
        out.println("[Server] Command now is: " + this.command + "; shouldISend is: " + shouldISend);
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}
