package com.ikchi.annode.config;

import com.ikchi.annode.security.JwtProvider;
import com.ikchi.annode.websocket.RedisMessageSubscriber;
import com.ikchi.annode.websocket.SocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;


@Configuration
@EnableWebSocket // 웹소켓에 대해 자동설정을 한다
@Transactional
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final SocketHandler socketHandler;
    private final JwtProvider jwtProvider;
    private final RedisMessageSubscriber redisMessageSubscriber;


    // 핸들러에서 클라이언트 연결과 관련해 메소드를 정의해놓고 이를 WebSocketConfig에서 핸들러로 등록한다
    //    클라이언트가 접속 , close , 메세지를 했을때 메소드 발동
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(socketHandler, "/ws")
            .setAllowedOrigins(
                "https://loadbalancer.annode-kic.com",
                "http://loadbalancer.annode-kic.com",
                "https://annode-kic.com",
                "http://annode-kic.com"
//                "http://localhost:3000"
            )
            .withSockJS();

    }

}
