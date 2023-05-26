package com.company.proxy.controller;

import com.company.proxy.CustomContextAware;
import com.company.proxy.Main;
import com.company.proxy.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class ClientController {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;


    @MessageMapping("/connectToRobot") // = app/connectToRobot ---> Receiving
    @SendTo("/topic/bot") // = /topic/bot ---> Sending
    public Message clientRequestForConnectingToRobot() throws IOException {
        System.out.println("[Client] Connect to the robot");
        Main main = CustomContextAware.getContext().getBean(Main.class);
        boolean result = main.connectToRobot();
        String returnMessage = "[Server] [Robot ";
        if (result) returnMessage += "connected]";
        else returnMessage += "not connected]";

        return new Message(returnMessage);
    }

    @MessageMapping("/disconnectFromRobot")
    @SendTo("/topic/bot")
    public Message clientRequestFroDisconnectingFromRobot() throws IOException {
        System.out.println("[Client] Disconnect from robot");
        Main main = CustomContextAware.getContext().getBean(Main.class);
        main.disconnectFromRobot();
        return new Message("[Server] [Robot disconnected]");
    }

    //    Specific robot commands
    @MessageMapping("/commandRobot")
    @SendTo("/topic/bot")
    public Message clientCommand(Message message) throws IOException {
        Main main = CustomContextAware.getContext().getBean(Main.class);
        main.commandRobot(message.getMessage());
        return new Message("[Server] ");
    }

    @MessageMapping("/setProperty")
    @SendTo("/topic/setPropertyResponse")
    public Message clientChangeProperty(Message message) {
        String messageInString = message.getMessage();
        String property = "";
        boolean success = false;
        Main main = CustomContextAware.getContext().getBean(Main.class);

        Pattern pattern = Pattern.compile("\\b(\\w+):(\\w+)\\b");
        Matcher matcher = pattern.matcher(messageInString);

        while (matcher.find()) {
            if (!matcher.group(1).equals("robotIP")) {
                property = matcher.group(1);
                String value = matcher.group(2);
                success = main.setProperty(property, value);
            } else {
                Pattern patternIP = Pattern.compile("\\b(\\w+):(([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3}))\\b");
                Matcher matcherIP = patternIP.matcher(messageInString);
                while (matcherIP.find()) {
                    property = matcherIP.group(1);
                    String valueInString = matcherIP.group(2);
                    success = main.setProperty(property, valueInString);
                }
            }
        }
        return new Message(String.valueOf(success));
    }


    public void sendRobotDataToClient(Map<String, Object> data) {
        simpMessagingTemplate.convertAndSend("/topic/bot", data);
    }
}





















