package com.ikchi.annode.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final FirebaseMessaging firebaseMessaging;
    private static final Logger logger = LogManager.getLogger(NotificationService.class);


    public void sendNotification(String fcmToken, String body, String messageUrl) {
        try {
            if (fcmToken != null && fcmToken != "") {

                Notification notification = Notification.builder()
                    .setTitle("Pospace") // 알람의 제목을 입력받음
                    .setBody(body) // 알람의 내용을 입력받음
                    .build();

                Message.Builder messageBuilder = Message.builder()
                    .setNotification(notification)
                    .setToken(fcmToken);

                if (messageUrl != null && !messageUrl.isEmpty()) {
                    messageBuilder.putData("url", messageUrl);
                }

                Message message = messageBuilder.build();

                String response = firebaseMessaging.send(message);


            }

        } catch (FirebaseMessagingException e) {
            logger.error("로그, Firebase메세지 알림을 보내던중 FirebaseMessagingException 발생", e);
        }
    }

}
