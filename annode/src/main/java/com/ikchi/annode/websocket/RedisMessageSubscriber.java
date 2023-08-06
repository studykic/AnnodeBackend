package com.ikchi.annode.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;

@Component
@RequiredArgsConstructor
public class RedisMessageSubscriber implements MessageListener {

    private final RedisTemplate<String, Integer> redisTemplate;
    private final ApplicationEventPublisher eventPublisher;


    // Message를 받아서 서버에서 사용할수있도록 처리한뒤
    // SocketHandler의 handleRedisMessageReceivedEvent에 Map으로 변환한 데이터를 전달함
    @Override
    public void onMessage(Message message, byte[] pattern) {

        String pospaceToken = new String(message.getChannel());

        String payload = new String(message.getBody());

        ObjectMapper mapper = new ObjectMapper();

        try {

            // messageMapKeys에 있는 key들을 메세지를 읽은 jsonNode에서 찾아서 존재할경우 messageMap에 넣는다
            
            Map<String, String> messageMap = new HashMap<>();
            JsonNode intermediateNode = mapper.readTree(payload);
            String payload2 = intermediateNode.asText();
            JsonNode finalNode = mapper.readTree(payload2);

            List<String> messageMapKeys = Arrays.asList("event", "data", "jwt", "mediatorUseSid",
                "senderSId", "userIdentifier");

            for (String key : messageMapKeys) {
                if (!finalNode.has(key)) {
                    continue;
                }

                JsonNode valueNode = finalNode.get(key);
                String value;

                if (valueNode.isObject()) {
                    // JSON 객체를 문자열로 직렬화합니다.
                    value = mapper.writeValueAsString(valueNode);
                } else {
                    // JSON 노드의 문자열 값을 얻습니다.
                    value = valueNode.asText();
                }

                messageMap.put(key, value);
            }

            eventPublisher.publishEvent(
                new RedisMessageReceivedEvent(this, messageMap, pospaceToken));


        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }


    public TextMessage jwtRemoveMsg(TextMessage originalMessage) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            ObjectNode messageJson = objectMapper.readValue(originalMessage.getPayload(),
                ObjectNode.class);
            messageJson.remove("jwt");
            String updatedMessage = objectMapper.writeValueAsString(messageJson);
            TextMessage resultMessage = new TextMessage(updatedMessage);
            return resultMessage;
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("TextMessage를 변환하는중 문제가 발생하였습니다.", e);
        }
    }


}
