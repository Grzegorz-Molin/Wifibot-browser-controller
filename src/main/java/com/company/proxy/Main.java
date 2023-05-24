package com.company.proxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.*;

import static java.lang.System.out;

@SpringBootApplication
public class Main {


    public static ConfigurableApplicationContext context;


    // Threads
    public static FetchingThread communicatingThread;


    public static void communicateWithRobot() throws IOException {
        out.println("Communicating");
        communicatingThread.setShouldIRead(true);
        communicatingThread.start();


            /* ConnectToRobot() method could have been called by the sending threads after "Broken pipe" error has been
            invoked. Now we have to check, if this is tha case, and if so, recall last called command.
            This behaviour is safe because in the beginning, actualCommand is initialized with "nothing". So if it not
            "nothing", it had to be changed by some other command.
            */
//			if (!actualCommand.equals("nothing")) {
//				out.println("Socket has fallen, reconfiguring sending command");
//				if (actualCommand.equals("forward")) sendingThread.forward();
//				else if (actualCommand.equals("backward")) sendingThread.backward();
//				else if (actualCommand.equals("left")) sendingThread.direction("left");
//				else if (actualCommand.equals("right")) sendingThread.direction("right");
//			}
    }


    public static void main(String[] args) throws IOException {
        SpringApplication.run(Main.class, args);
        communicatingThread = new FetchingThread();
//        communicatingThread.pingRobot();
    }


}
