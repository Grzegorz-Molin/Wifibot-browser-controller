package com.company.proxy;

import java.util.HashMap;
import java.util.Map;

public class RobotData {
    private int speedFrontLeft;
    private int batLevel;
    private int irLeftFront;
    private int irLeftBack;
    private long odometryLeft;

    private int speedFrontRight;
    private int irRightFront;
    private int irRightBack;
    private long odometryRight;

    private int current;
    private int version;

    //--------------------------------------------------------------------
    public RobotData() {
        System.out.println("[Server] Empty robot data created");
    }

    // Converting raw data from the robot (in the form of byte[] array) to specific properties of this class
    public void setAllData(byte[] buffer) {
        speedFrontLeft = ((buffer[1] << 8) | (buffer[0] & 0xFF));
        if (speedFrontLeft > 32767) {
            speedFrontLeft = speedFrontLeft - 65536;
        }
        batLevel = buffer[2] & 0xFF;
        irLeftFront = buffer[3] & 0xFF;
        irLeftBack = buffer[4] & 0xFF;
        odometryLeft = (((long) buffer[8] << 24) | ((long) buffer[7] << 16) | ((long) buffer[6] << 8) | (long) buffer[5]) & 0xFF;
        speedFrontRight = ((buffer[10] << 8) | (buffer[9] & 0xFF));
        if (speedFrontRight > 32767) {
            speedFrontRight = speedFrontRight - 65536;
        }
        irRightFront = buffer[11] & 0xFF;
        irRightBack = buffer[12] & 0xFF;
        odometryRight = (((long) buffer[16] << 24) | ((long) buffer[15] << 16) | ((long) buffer[14] << 8) | (long) buffer[13]) & 0xFF;
        current = buffer[17];
        version = buffer[18];
    }

    // Converting the data to Map<String, Object> for sending to the client and easier parsing on the frontend
    public Map<String, Object> getAllData() {
        Map<String, Object> dataMap = new HashMap<>();

        dataMap.put("speedFrontLeft", speedFrontLeft);
        dataMap.put("batLevel", batLevel);
        dataMap.put("irLeftFront", irLeftFront);
        dataMap.put("irLeftBack", irLeftBack);
        dataMap.put("odometryLeft", odometryLeft);
        dataMap.put("speedFrontRight", speedFrontRight);
        dataMap.put("irRightFront", irRightFront);
        dataMap.put("irRightBack", irRightBack);
        dataMap.put("odometryRight", odometryRight);
        dataMap.put("current", current);
        dataMap.put("version", version);

        return dataMap;
    }


    @Override
    public String toString() {
        return "" +
                "speedFrontLeft=" + speedFrontLeft +
                ", batLevel=" + batLevel +
                ", irLeftFront=" + irLeftFront +
                ", irLeftBack=" + irLeftBack +
                ", odometryLeft=" + odometryLeft +
                ", speedFrontRight=" + speedFrontRight +
                ", irRightFront=" + irRightFront +
                ", irRightBack=" + irRightBack +
                ", odometryRight=" + odometryRight +
                ", current=" + current +
                ", version=" + version;
    }
}
