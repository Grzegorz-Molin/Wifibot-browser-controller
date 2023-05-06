package com.company.proxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import static java.lang.System.out;

@SpringBootApplication
public class ProxyApplication {
	private static String ROBOT_IP = "192.168.1.106";
	private static int PORT = 15020;

	// Status variables
	static Boolean botConnected = false;
	static String actualCommand = "nothing";

	static Socket socket;
	static InputStream inputStream;
	static DataInputStream dataInputStream;
	static OutputStream outputStream;
	static DataOutputStream dataOutputStream;

	// Threads
	static ReadingThread readingThread;
	static SendingThread sendingThread;

	// Client
	private static final int CLIENT_PORT = 3005;

	public static void connectToRobot() {
		out.println("Connecting");
		try {
			socket = new Socket();
			socket.connect(new InetSocketAddress(ROBOT_IP, PORT), 3000);
			socket.setKeepAlive(true);


			// Initialize Input a and Output streams
			inputStream = socket.getInputStream();
			dataInputStream = new DataInputStream(inputStream);
			outputStream = socket.getOutputStream();
			dataOutputStream = new DataOutputStream(outputStream);

			// Initialize threads
			sendingThread = new SendingThread(outputStream, socket);
			sendingThread.start();
			readingThread = new ReadingThread(inputStream);
			readingThread.start();

            /* ConnectToRobot() method could have been called by the sending threads after "Broken pipe" error has been
            invoked. Now we have to check, if this is tha case, and if so, recall last called command.
            This behaviour is safe because in the beginning, actualCommand is initialized with "nothing". So if it not
            "nothing", it had to be changed by some other command.
            */
			if (!actualCommand.equals("nothing")) {
				out.println("Socket has fallen, reconfiguring sending command");
				if (actualCommand.equals("forward")) sendingThread.forward();
				else if (actualCommand.equals("backward")) sendingThread.backward();
				else if (actualCommand.equals("left")) sendingThread.direction("left");
				else if (actualCommand.equals("right")) sendingThread.direction("right");
			}

			botConnected = true;
			out.println("[Connected, Socket made, Streams and Threads initialized]");
		} catch (IOException e) {
			e.printStackTrace();
			out.println("\nCheck if:  \n   1. You are connected to the right network \n   2. The robot is ON\n 3. The robot has not booted yet(in that case the green light blinking)");
		}
	}


	public static void disconnectFromRobot() throws IOException {
		try {
			readingThread.setShouldIRead(false);
			inputStream.close();
			outputStream.close();
			socket.close();
			botConnected = false;
			out.println("Socket closed");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public static void main(String[] args) {
		SpringApplication.run(ProxyApplication.class, args);

	}

}
