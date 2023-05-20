package com.company.proxy.controller;

import com.company.proxy.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.company.proxy.Main.*;

@Controller
public class ClientController {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;


    @MessageMapping("/connectToRobot") // = app/connectToRobot ---> Receiving
    @SendTo("/topic/bot") // = /topic/bot ---> Sending
    public Message clientRequestForConnectingToRobot() {
        System.out.println("[Client] Connect to the robot");
        try {
            if (connectToRobot()) {
                return new Message("[Server] [Robot connected]");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Message("[Server] [Robot not connected]");
    }

    @MessageMapping("/disconnectFromRobot")
    @SendTo("/topic/bot")
    public Message clientRequestFroDisconnectingFromRobot() throws IOException {
        System.out.println("[Client] Disconnect from robot");
        disconnectFromRobot();
        return new Message("[Server] [Robot disconnected]");
    }

    //    Specific robot commands
    @MessageMapping("/commandRobot")
    @SendTo("/topic/bot")
    public Message clientCommand(Message message) throws IOException {
        System.out.println("[Client] Forward");
        commandRobot(message.getMessage());
        return new Message("[Server] Forward");
    }

    @MessageMapping("/setProperty")
    @SendTo("/topic/setPropertyResponse")
    public Message clientChangeProperty(Message message) {
        System.out.println("--- changin property ---");
        String messageInString = message.getMessage();
        System.out.println("Message in strng: " + messageInString);
        String property = "";
        boolean success = false;

        Pattern pattern = Pattern.compile("\\b(\\w+):(\\w+)\\b");
        Matcher matcher = pattern.matcher(messageInString);

        while (matcher.find()) {
            if (!matcher.group(1).equals("robotIP")) {
                property = matcher.group(1);
                int value = Integer.parseInt(matcher.group(2));
                success = setProperty(property, value); // Call the setProperty method
            } else {
                System.out.println("Looks like it is IP address...");
                Pattern patternIP = Pattern.compile("\\b(\\w+):(([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3}))\\b");
                Matcher matcherIP = patternIP.matcher(messageInString);
                while (matcherIP.find()) {
                    property = matcherIP.group(1);
                    String valueInString = matcherIP.group(2);
                    success = setProperty(property, valueInString);
                }
            }
        }

        System.out.println("Seting property [int] '" + property + "' has been '" + success + "'\n");
        return new Message(String.valueOf(success));
    }


    public void sendRobotDataToClient(List<Long> data) {
        simpMessagingTemplate.convertAndSend("/topic/bot", data);
    }
}
