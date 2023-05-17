package com.company.proxy.controller;

import com.company.proxy.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.List;

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

    public void sendRobotDataToClient(List<Long> data) {
        simpMessagingTemplate.convertAndSend("/topic/bot", data);
    }
}
