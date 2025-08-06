package com.example.service.demo.services;

import java.io.IOException;

import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;

import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Slf4j
@Component
public class slackPush {
    
    public static final String URL = "https://slack.com/api/chat.scheduleMessage";

    public static final String token = "xoxb-9302455157472-9301204605953-OZ7WSy7CcxqzFgl5c2tS9rHI";
    public static final String channel = "D098V60PGE5";

    
    @KafkaListener(topics = "slack-messages-queue", groupId = "slack-group")
    public void kafkaConsumer(String message){
        try{
            String push = message;
            publishMessage(channel,push);
        }catch (Exception e){
            log.error("could not send messaget to user");
        }
    }



     public static void publishMessage(String channel, String text) throws  IOException, SlackApiException{
        var client = Slack.getInstance().methods();
        try {
            var result = client.chatPostMessage(r -> r
                .token(token)
                .channel(channel)
                .text(text)

            );
            log.info("result {}", result);
        } catch (IOException | SlackApiException e) {
            log.error("error: {}", e.getMessage(), e);
        }
     }
    
}
