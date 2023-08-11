package com.ikchi.annode.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ikchi.annode.domain.entity.User;
import com.ikchi.annode.security.JwtProvider;
import com.ikchi.annode.service.SocketHandlerService;
import com.ikchi.annode.service.UserService;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

// WebSocketHandler를 사용하는 경우, 표준 WebSocket session(JSR-356)은 동시 전송을 지원하지 않는다.
// 때문에 webSocketSession을 ConcurrentWebSocketSessionDecorator으로 Wrapping하여 웹소켓세션을 스레드 안전하게 만들며
// 여러 스레드가 동시에 같은 웹소켓세션에 접근하지 못하도록 만들어서 웹소켓 세션 객체의 일관성을 유지하고, 여러 스레드에서 동시에 웹소켓 세션 객체를 변경하는 것을 방지한다
@Component
public class SocketHandler extends TextWebSocketHandler {

    private final UserService userService;
    private final SocketHandlerService socketHandlerService;
    private final JwtProvider jwtProvider;

    @Autowired
    public SocketHandler(UserService userService, SocketHandlerService socketHandlerService,
        JwtProvider jwtProvider) {
        this.userService = userService;
        this.socketHandlerService = socketHandlerService;
        this.jwtProvider = jwtProvider;
    }

    // TextMessage를 받아서 Map으로 처리한다 이때 변환할 key에 해당하는 메세지값이 없으면 null을 할당한다
    private Map<String, String> parseMessage(TextMessage message) {
        try {
            // 메세지를 JsonNode로 변환한다
            Map<String, String> messageMap = new HashMap<>();

            ObjectMapper objectMapper = new ObjectMapper();

            JsonNode jsonNode = objectMapper.readTree(message.getPayload());

            List<String> messageMapKeys = Arrays.asList("event", "data", "jwt", "mediatorUseSid",
                "senderSId");

            // messageMapKeys에 있는 key들을 메세지를 읽은 jsonNode에서 찾아서 존재할경우 messageMap에 넣는다
            for (String key : messageMapKeys) {
                String value = jsonNode.get(key) != null ? jsonNode.get(key).asText() : null;
                messageMap.put(key, value);
            }

            return messageMap;
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("TextMessage를 변환하는중 문제가 발생하였습니다.", e);
        }
    }


    // 피어가 성공적으로 접속시 호출되는 메소드이며 웹소켓서버가 클라이언트와의 지속연결을 위해 메모리에 웹소켓세션을 등록완료했다는걸 의미하기도한다
    // 이때 클라가 보낸 웹소켓 서버접속에 들어갈 방정보를 파라미터든 바디든 담아서 함께 보내 특정방의 리스트로 넣어준다
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {

        System.out.println("접속하려는 세션 session = " + session);

        socketHandlerService.handleAfterConnectionEstablished(session);

    }

    // 피어가 접속 종료시 호출되는 메소드
    // p2p로 연결중이었던 피어들에게 유저의 접속을 종료시키는 GoodBye 브로드캐스트 메세지를 전달하여 p2p연결도 종료시킴

    @Override
    @Transactional
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {

        System.out.println(
            "퇴장하려는 세션 session = " + session.getAttributes().get("pospaceToken").toString());

        // Redis 채널로 퇴장메세지 보내기
        String pospaceToken = session.getAttributes().get("pospaceToken").toString();

        socketHandlerService.afterConnectionClosedHandler(session, pospaceToken);

        TextMessage exitMessage = socketHandlerService.modifiedMessage(null, "exit", session);
        socketHandlerService.sendRedisMessage(pospaceToken, exitMessage);


    }


    /**
     * 클라이언트로부터 메시지를 받으면 목록의 모든 클라이언트 세션을 반복하고 보낸 사람의 세션 ID를 비교하여 보낸 사람을 제외한 다른 모든 클라이언트에게 메시지를
     * 브로드캐스트한다. Client가 Offer하는 경우 실행 됨
     */
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage originalMessage) {
        String pospaceToken = session.getAttributes().get("pospaceToken").toString();

        //  1. Redis로 다른 클러스터 멤버들에게 메세지 전달한다

        socketHandlerService.sendRedisMessage(pospaceToken, originalMessage);
    }

    //  타 클러스터 멤버로부터 Redis를 통해 메세지를 구독받음
    // RedisMessageSubscriber 인스턴스와 협력하여 Event리스너로 데이터를 전달받음
    @EventListener
    public void handleRedisMessageReceivedEvent(RedisMessageReceivedEvent event) {
        Map<String, String> messageMap = event.getMessageMap();

        String channelName = event.getChannelName();
        extracted(messageMap, channelName);
    }

    // Redis로부터 메세지를 구독받으면 각 서버들은 자신이 관리하는 웹소켓세션에게 메세지를 분기처리로 전달함
    private void extracted(Map<String, String> parsedMessageMap, String pospaceToken) {
        try {

            String event = parsedMessageMap.get("event");
            String mediatorUseSid = parsedMessageMap.get("mediatorUseSid");
            String senderSId = parsedMessageMap.get("senderSId");
            String jwt = parsedMessageMap.get("jwt");

            if (jwt != null) {

                User user = socketHandlerService.jwtToUser(jwt);

                parsedMessageMap.put("nickName", user.getNickName());
                parsedMessageMap.put("userIdentifier", user.getUserIdentifier());
                parsedMessageMap.put("profileImgFileUrl", user.getProfileImgFileUrl());

                parsedMessageMap.remove("jwt");
            }

            ObjectMapper mapper = new ObjectMapper();
            TextMessage jsonMessageMap = new TextMessage(
                mapper.writeValueAsString(parsedMessageMap));

            switch (event) {

                case "start_call": {
//                    System.out.println("start_call 메세지를 받았습니다 ");
                    socketHandlerService.handleTextMessageStart_call(jsonMessageMap, senderSId,
                        pospaceToken);
                    break;
                }
                case "candidate": {
//                    System.out.println("candidate 메세지를 받았습니다");
                    socketHandlerService.handleTextMessageCandidate(jsonMessageMap,
                        mediatorUseSid, pospaceToken);
                    break;
                }
                case "offer": {
//                    System.out.println("offer 메세지를 받았습니다");
                    socketHandlerService.handleTextMessageOffer(jsonMessageMap,
                        mediatorUseSid, pospaceToken);
                    break;
                }
                case "answer": {
//                    System.out.println("answer 메세지를 받았습니다");
                    socketHandlerService.handleTextMessageAnswer(jsonMessageMap,
                        mediatorUseSid, pospaceToken);
                    break;
                }
                case "broadcast_GoodBye": {
//                    System.out.println("broadcast_GoodBye 메세지를 받았습니다");
                    socketHandlerService.handleTextMessageBroadcast_GoodBye(jsonMessageMap,
                        senderSId,
                        pospaceToken);
                    break;
                }
                default:
                    System.out.println("알수없는 이벤트 메세지 입니다");
                    break;
            }

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }


    // 클라이언트로부터 받은 소켓 메세지에서 jwt만 제거
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





