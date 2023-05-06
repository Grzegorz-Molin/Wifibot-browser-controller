package com.company.proxy;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;

import static java.lang.System.out;

public class SendingThread extends Thread implements KeyListener {
    byte[] dataToSend = new byte[]{(byte) 0xFF, (byte) 0x07, (byte) 0xC8, (byte) 0x00, (byte) 0xC8, (byte) 0x00, (byte) 0x53, (byte) 0x21, (byte) 0xBF};
    private final int SPEED = 100;
    OutputStream outputStream;
    private Boolean shouldISend;

    private String command;
    private Socket socket;

    public SendingThread(OutputStream outputStream, Socket socket) {
        this.outputStream = outputStream;
        this.shouldISend = true;
        this.command = "nothing";
        this.socket = socket;
        out.println("[Sending thread made up]");
    }

    // Main sending logic inside Run() method. It only sends if the command is not "nothing"
    @Override
    public void run() {
        while (shouldISend) {
            if (!command.equals("nothing")) {
                System.out.println("[Sending]");
                try {
                    // Sending data
                    outputStream.write(dataToSend);
                    outputStream.flush();
                    out.print("Sending: " + Arrays.toString(dataToSend));
                    Thread.sleep(50);
                }
                // Check for the disconnection of the robot
                catch (SocketException e) {
                    if (e.getMessage().equals("Broken pipe")) {
                        // handle Broken pipe error
                        System.out.println("Broken pipe error occurred; Reconnecting");
                    } else {
                        // handle other SocketException errors
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    // handle other exceptions
                    e.printStackTrace();
                    setShouldISend(false);
                    out.println("Ending sending");
                }
            }
            if (command.equals("nothing")) {
                System.out.println("[Nothing]");
                try {
                    // Sending data
                    outputStream.write(giveMeNothing());
                    outputStream.flush();
                    out.print("(nothing): " + Arrays.toString(giveMeNothing()));
                    Thread.sleep(900);
                } catch (Exception e) {
                    // handle other exceptions
                    e.printStackTrace();
                    setShouldISend(false);
                    out.println("Ending sending");
                }
            }
        }

    }

    // Command methods
    public void forward() {
        System.out.println("Forward: " + SPEED);
        byte[] newCommand = new byte[9];
        newCommand[0] = (byte) 0xff;     //255 (= -1)
        newCommand[1] = (byte) 0x07;     //
        newCommand[2] = (byte) SPEED;
        newCommand[3] = (byte) (SPEED >> 8);
        newCommand[4] = (byte) SPEED;
        newCommand[5] = (byte) (SPEED >> 8);
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
        System.out.println("Backward: " + SPEED);
        byte[] newCommand = new byte[9];
        newCommand[0] = (byte) 0xff;        //255
        newCommand[1] = (byte) 0x07;        //size
        newCommand[2] = (byte) SPEED;    //left SPEED
        newCommand[3] = (byte) (SPEED >> 8);
        newCommand[4] = (byte) SPEED;    //right SPEED
        newCommand[5] = (byte) (SPEED >> 8);
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
        System.out.println("Rotate " + dir + ", SPEED: " + SPEED);
        byte[] newCommand = new byte[9];
        newCommand[0] = (byte) 0xff;
        newCommand[1] = (byte) 0x07;
        newCommand[2] = (byte) SPEED;
        newCommand[3] = (byte) (SPEED >> 8);
        newCommand[4] = (byte) SPEED;
        newCommand[5] = (byte) (SPEED >> 8);
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
        out.println("Command now is: " + this.command + "; shouldISend is: " + shouldISend);
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
