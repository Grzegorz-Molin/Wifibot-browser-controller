package com.company.proxy.controller;

import com.company.proxy.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
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
    @SendTo("topic/bot")
    public Message clientRequestFroDisconnectingFromRobot() throws IOException {
        System.out.println("[Client] Disconnect from robot");
        disconnectFromRobot();
        return new Message("[Server] [Robot disconnected]");
    }

    //    Specific robot commands
    @MessageMapping("/commandRobot")
    @SendTo("topic/bot")
    public Message clientCommand(Message message) throws IOException {
        System.out.println("[Client] Forward");
        commandRobot(message.getMessage());
        return new Message("[Server] Forward");
    }

    @MessageMapping("/setProperty")
    @SendTo("topic/bot")
    public Message clientChangeProperty(Message message) {
        String messageInString = message.getMessage();
        String property = "";
        int value = 0;

        Pattern pattern = Pattern.compile("\\b(\\w+):(\\w+)\\b");
        Matcher matcher = pattern.matcher(messageInString);

        while (matcher.find()) {
            property = matcher.group(1);
            value = Integer.parseInt(matcher.group(2));
        }

        return new Message("[Server] Operation " + property + " successful: " + setProperty(property, value));
    }

    public void sendRobotDataToClient(List<Long> data) {
        simpMessagingTemplate.convertAndSend("/topic/bot", data);
    }
}
