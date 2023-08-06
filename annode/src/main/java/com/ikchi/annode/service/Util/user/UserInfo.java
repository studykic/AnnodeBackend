package com.ikchi.annode.service.Util.user;

import com.ikchi.annode.security.AESUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.util.MultiValueMap;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@Getter
@ToString
@NoArgsConstructor
public class UserInfo {

    private String email;
    private String roomToken;


    // 처음 auth 인증 이벤트를 받게되면 요청 세션을 가지고 UserInfo를 생성한다 이때 세션의 Uri에서 roomToken을 추출한뒤 Email로 디코딩한다
    public UserInfo(WebSocketSession session, String sessionEmail) {

        try {
            // encryptedRoomToken를 요청 파라미터에서 추출한다
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUri(session.getUri());
            UriComponents uriComponents = uriBuilder.build();
            MultiValueMap<String, String> sessionUriMap = uriComponents.getQueryParams();
            String encryptedRoomToken = sessionUriMap.getFirst("roomtoken");

            // 인코딩된 roomToken을 디코딩하고 UserInfo에 할당한다
            String decryptedRoomToken = AESUtil.decrypt(encryptedRoomToken, sessionEmail);

            this.roomToken = decryptedRoomToken;
            this.email = sessionEmail;
        } catch (Exception e) {
            throw new RuntimeException("유저 정보를 생성하는데 실패했습니다");
        }

    }


}