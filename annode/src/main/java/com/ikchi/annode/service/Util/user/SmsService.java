package com.ikchi.annode.service.Util.user;

import java.util.Random;
import lombok.RequiredArgsConstructor;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class SmsService {

    private final DefaultMessageService messageService;

    @Value("${app.sms.sender.number}")
    private String senderNumber;


    public String sendSms(String toNumber) {

        String epw = createKey();

        net.nurigo.sdk.message.model.Message message = new Message();
        // 발신번호 및 수신번호는 반드시 01012345678 형태로 입력되어야 합니다.
        message.setFrom(senderNumber);
        message.setTo(toNumber);
        message.setText(
            " [ Pospace 인증번호 알림 ] , 돌아가셔서 인증번호를 입력해주세요. " + epw);

        SingleMessageSentResponse response = this.messageService.sendOne(
            new SingleMessageSendingRequest(message));

        return epw;
    }


    public String createKey() {
        StringBuilder authCode = new StringBuilder();
        Random rnd = new Random();

        for (int i = 0; i < 6; i++) {
            int index = rnd.nextInt(2); // 0~1 까지 랜덤, rnd 값에 따라서 아래 switch 문이 실행됨

            switch (index) {
                case 0:
                    authCode.append((char) ((int) (rnd.nextInt(26)) + 65));
                    // A~Z
                    break;
                case 1:
                    authCode.append(rnd.nextInt(10));
                    // 0~9
                    break;
            }
        }

        return authCode.toString();
    }

}
