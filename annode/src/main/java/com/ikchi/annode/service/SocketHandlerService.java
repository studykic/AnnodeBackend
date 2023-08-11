package com.ikchi.annode.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ikchi.annode.Enum.PospaceExceptionMessage;
import com.ikchi.annode.Enum.SocketHandlerMessage;
import com.ikchi.annode.Enum.UserExceptionMessage;
import com.ikchi.annode.domain.entity.Pospace;
import com.ikchi.annode.domain.entity.User;
import com.ikchi.annode.repository.PospaceRepository;
import com.ikchi.annode.repository.ReportUserRepository;
import com.ikchi.annode.repository.UserRepository;
import com.ikchi.annode.security.AESUtil;
import com.ikchi.annode.security.JwtProvider;
import com.ikchi.annode.service.Util.user.UserInfo;
import com.ikchi.annode.websocket.RedisMessageSubscriber;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class SocketHandlerService {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final PospaceRepository pospaceRepository;
    private final ReportUserRepository reportUserRepository;
    private final UserService userService;

    private final RedisMessageSubscriber redisMessageSubscriber;

    private final RedisTemplate<String, Integer> redisTemplate;
    private ValueOperations<String, Integer> valOps;

    @PostConstruct
    private void init() {
        valOps = redisTemplate.opsForValue();
    }


    private static final Map<String, List<WebSocketSession>> sessionMap = new ConcurrentHashMap<>();


    private static final Map<String, UserInfo> sessionToRoomUserInfoMap = new ConcurrentHashMap<>();


    // 세션이 처음연결되면 입장하려는 pospace를 찾아 적절한 Redis채널을 생성하거나 참가를하게한뒤
    //  서버내 sessionMap에도 채널을 생성하거나 List<WebSocketSession>에 참가를 시킨다
    //  또한 웹소켓세션 Attributes에 index가 추가된 채널명을 pospaceToken으로 등록해놓는다
    @Transactional
    public void handleAfterConnectionEstablished(WebSocketSession session) {

        String pospaceToken = sendRedisMessage(session);

        Pospace pospace = pospaceRepository.findByToken(pospaceToken)
            .orElseThrow(
                () -> new NoSuchElementException(
                    PospaceExceptionMessage.NO_SUCH_POST.getMessage()));

        String currentPospaceToken = increaseAndReturnChannelCount(pospaceToken, pospace);

        List<WebSocketSession> getWebSocketSessions = sessionMap.get(currentPospaceToken);

        // 웹소켓세션의 동시성 문제에 방지
        WebSocketSession concurrentSession = new ConcurrentWebSocketSessionDecorator(session,
            5000,
            65536);

        concurrentSession.getAttributes().put("pospaceToken", currentPospaceToken);

        if (getWebSocketSessions == null) {

            List<WebSocketSession> newWebSocketSessions = Collections.synchronizedList(
                new ArrayList<>());

            newWebSocketSessions.add(concurrentSession);
            sessionMap.put(currentPospaceToken, newWebSocketSessions);
            subscribeToChannel(currentPospaceToken);


        } else {

            getWebSocketSessions.add(concurrentSession);
        }

        String sessionEmail = concurrentSession.getAttributes().get("email").toString();
        User user = userService.findUserByMail(sessionEmail);
        user.setJoinedRoom(currentPospaceToken);
    }

    public void sendRedisMessage(String pospaceToken, TextMessage originalMessage) {

        redisTemplate.convertAndSend(pospaceToken, originalMessage.getPayload());
    }


    public void subscribeToChannel(String channelName) {

        // 채널 구독
        redisTemplate.getConnectionFactory().getConnection()
            .subscribe(redisMessageSubscriber, channelName.getBytes());
    }

    // 입장할 채널을 찾거나 개설한뒤 Redis상의 Value에서 인원수 +1까지 수행함


    // 게시글의 여러 채널중 적절한 채널의 입장을 시키거나 채널을 새로 생성시킨다
    @Transactional
    public String increaseAndReturnChannelCount(String pospaceToken, Pospace pospace) {

        Set<String> keys = redisTemplate.keys("channel:" + pospaceToken + "*");

        List<String> keyList = new ArrayList<>();
        List<Integer> valueList = new ArrayList<>();

        if (keys.isEmpty()) {

            String uniqueID = generateUniqueId();

            String newKey = "channel:" + pospaceToken + uniqueID;

            valOps.set(newKey, 1);
            return newKey;

        } else {

            for (String key : keys) {
                Integer value = valOps.get(key);
                valueList.add(Integer.valueOf(value));
                keyList.add(key);
            }
        }

        int minValue = Collections.min(valueList);

        int minIndex = valueList.indexOf(minValue);
        String minKey = keyList.get(minIndex);

        if (minValue >= pospace.getMaxAnnode()) {

            String uniqueID = generateUniqueId();

            String newKey = "channel:" + pospaceToken + uniqueID;

            valOps.set(newKey, 1);
            return newKey;
        } else {
            valOps.increment(minKey, 1);
            return minKey;
        }
    }

    public String generateUniqueId() {
        String uuid = UUID.randomUUID().toString();
        uuid = uuid.replace("-", "");
        return uuid.substring(0, 6);
    }

    @Transactional
    public void handleTextMessageStart_call(TextMessage jsonMessageMap,
        String senderSId, String pospaceToken) {

        List<WebSocketSession> webSocketSessions = getWebSocketSessions(pospaceToken);

        broadcastEvent(webSocketSessions, jsonMessageMap, senderSId);

    }


    // 556 Candidate
    @Transactional
    public void handleTextMessageCandidate(TextMessage jsonMessageMap,
        String mediatorUseSid, String pospaceToken) {

        List<WebSocketSession> webSocketSessions = getWebSocketSessions(pospaceToken);

        mediatorEvent(webSocketSessions, jsonMessageMap, mediatorUseSid);
    }


    // 556 Offer
    @Transactional
    public void handleTextMessageOffer(TextMessage jsonMessageMap,
        String mediatorUseSid, String pospaceToken) {

        List<WebSocketSession> webSocketSessions = getWebSocketSessions(pospaceToken);

        mediatorEvent(webSocketSessions, jsonMessageMap, mediatorUseSid);

    }

    // 556 Answer
    @Transactional
    public void handleTextMessageAnswer(TextMessage jsonMessageMap,
        String mediatorUseSid, String pospaceToken) {

        List<WebSocketSession> webSocketSessions = getWebSocketSessions(pospaceToken);

        mediatorEvent(webSocketSessions, jsonMessageMap, mediatorUseSid);
    }


    // 퇴장하는 세션으로부터 메세지 전파받음
    @Transactional
    public void handleTextMessageBroadcast_GoodBye(TextMessage jsonMessageMap, String senderSId,
        String pospaceToken) {

        // 같은 채널에있던 세션들에게 브로드퇴장메세지 보내기

        List<WebSocketSession> webSocketSessions = getWebSocketSessions(pospaceToken);
        broadcastEvent(webSocketSessions, jsonMessageMap, senderSId);

    }


    // 퇴장하는 세션의 퇴장 로직수행
    @Transactional
    public void afterConnectionClosedHandler(WebSocketSession session, String pospaceToken) {

        String email = session.getAttributes().get("email").toString();

        User user = userRepository.findUserByMailWithLock(email)
            .orElseThrow(() -> new NoSuchElementException(
                UserExceptionMessage.NON_EXISTENT_USER.getMessage()));
        user.setJoinedRoom(null);

        List<WebSocketSession> webSocketSessions = getWebSocketSessions(pospaceToken);

        if (webSocketSessions != null && webSocketSessions.size() == 1) {
            sessionMap.remove(pospaceToken);
        } else if (webSocketSessions != null && webSocketSessions.size() > 1) {

            webSocketSessions.removeIf(
                item -> item.getAttributes().get("email").toString().equals(email));
        }

        Integer channelUserCount = valOps.get(pospaceToken);

        if (channelUserCount != null) {
            if (channelUserCount == 1) {
                // 채널에 사용자가 더 이상 없으므로 Redis에서 제거
                valOps.getOperations().delete(pospaceToken);
            } else {
                // 사용자가 퇴장으로 판단하고 1만큼 줄임
                valOps.decrement(pospaceToken);
            }
        }


    }


    public void createRoom(String roomToken) {
        // roomToken으로 만들어진 방이 없다면 방을 생성한다
        if (!sessionMap.containsKey(roomToken)) {
            sessionMap.put(roomToken, Collections.synchronizedList(new ArrayList<>()));
        } else {
            throw new IllegalArgumentException(SocketHandlerMessage.ROOM_TOKEN_EXISTS.getMessage());
        }

    }

    public void removeRoom(String roomToken) {
        sessionMap.remove(roomToken);
    }


    // 웹소켓세션 첫 연결시 pospaceToken를 이메일로 디코딩함
    public String sendRedisMessage(WebSocketSession session) {

        try {
            // encryptedRoomToken를 요청 파라미터에서 추출한다
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUri(session.getUri());
            UriComponents uriComponents = uriBuilder.build();
            MultiValueMap<String, String> sessionUriMap = uriComponents.getQueryParams();
            String encryptedRoomToken = sessionUriMap.getFirst("pospaceToken");
            String jwt = sessionUriMap.getFirst("anonode_jwt");

            User user = jwtToUser(jwt);

            // 인코딩된 roomToken을 디코딩하고 UserInfo에 할당한다
            String decryptedRoomToken = AESUtil.decrypt(encryptedRoomToken, user.getEmail());
            session.getAttributes().put("email", user.getEmail());

            return decryptedRoomToken;
        } catch (Exception e) {
            throw new RuntimeException(SocketHandlerMessage.USER_CREATION_FAILED.getMessage());
        }

    }


    // 수정하려는 메세지와 수정할 속성을 받아서 TextMessage 를 반환
    public TextMessage modifiedMessage(
        TextMessage message,
        String propKey,
        WebSocketSession session) {
        try {

            // Read the message as a JSON object
            ObjectMapper objectMapper = new ObjectMapper();

            ObjectNode messageJson = null;

            switch (propKey) {

                // 퇴장 메세지는 예상대로 클라이언트에서 퇴장한다고 시그널링 서버에게 소켓 퇴장 메세지을 보장하지않기에 연결이 끊어지면 서버가 직접 해당 유저의 퇴장 메세지를 생성함
                case "exit": {

                    String sessionEmail = session.getAttributes().get("email").toString();

                    User user = userRepository.findUserByMail(sessionEmail).orElseThrow(
                        () -> new NoSuchElementException("로그, 퇴장을 수행하려는 유저가 존재하지않음"));

                    messageJson = objectMapper.createObjectNode();

                    // 메시지를 요소 할당
                    messageJson.put("event", "broadcast_GoodBye");
                    messageJson.put("userIdentifier", user.getUserIdentifier());
                    messageJson.put("senderSId", session.getId());

                    break;
                }
                default:
                    return message;
            }

            String updatedMessage = objectMapper.writeValueAsString(messageJson);
            TextMessage resultMessage = new TextMessage(updatedMessage);

            return resultMessage;
        } catch (Exception e) {

            throw new IllegalArgumentException(
                SocketHandlerMessage.MESSAGE_MODIFICATION_FAILED.getMessage());
        }
    }


    public Optional<UserInfo> getUserInfo(String email) {
        UserInfo userInfo = sessionToRoomUserInfoMap.get(email);
        return Optional.ofNullable(userInfo);
    }


    public List<WebSocketSession> getWebSocketSessions(String roomToken) {
        return sessionMap.get(roomToken);
    }


    // 브로드캐스트 방식으로 메세지전송
    public void broadcastEvent(List<WebSocketSession> webSocketSessions, TextMessage jsonMessageMap,
        String senderSId) {
        try {
            if (webSocketSessions != null && jsonMessageMap != null) {

                for (WebSocketSession wsSession : webSocketSessions) {
                    if (!senderSId.equals(wsSession.getId())) {

                        wsSession.sendMessage(jsonMessageMap);

                    }

                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(SocketHandlerMessage.SIGNALING_ERROR.getMessage());
        }

    }

    // mediatorUseSid를 가진 소켓에게만 메세지전송
    // 만약 보낼 메세지가 없다면 mediatorUseSid를 가진 소켓을 반환하는 작업만 수행한다
    public void mediatorEvent(List<WebSocketSession> webSocketSessions, TextMessage jsonMessageMap,
        String mediatorUseSid) {
        try {
            if (webSocketSessions != null && jsonMessageMap != null) {
                for (WebSocketSession wsSession : webSocketSessions) {
                    if (mediatorUseSid.equals(wsSession.getId())) {
                        wsSession.sendMessage(jsonMessageMap);
                        break;
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(SocketHandlerMessage.SIGNALING_ERROR.getMessage());
        }
    }

    // 좋아요를 눌러서 user의 likeCount를 증가시킨다 이떄 synchronized를 사용했기때문에
    // 여러명이 동시에 요청해도 한번에 한 스레드만 처리하여 순차적으로 대기시켰다가 좋아요 요청이 처리된다
//    public void addLike(String email) {
//        User user = userRepository.findUserByMailWithLock(email)
//            .orElseThrow(() -> new NoSuchElementException("유저가 존재하지 않습니다"));
//
//        user.setLikeCount(user.getLikeCount() + 1);
//    }

    public User jwtToUser(String jwt) {
        try {

            String sessionEmail = jwtProvider.getEmailFromToken(jwt);

            User user = userService.findUserByMail(sessionEmail);
            return user;
        } catch (Exception e) {
            throw new NoSuchElementException(UserExceptionMessage.NON_EXISTENT_USER.getMessage());
        }

    }
}




