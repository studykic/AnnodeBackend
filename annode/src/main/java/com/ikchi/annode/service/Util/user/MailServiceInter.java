package com.ikchi.annode.service.Util.user;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Optional;

// mail 서비스 interface
public interface MailServiceInter {

    // 메일 내용 작성
    MimeMessage createMessage(String to) throws MessagingException, UnsupportedEncodingException;

    // 랜덤 인증 코드 전송
    String createKey();

    // 메일 발송
    Optional<String> sendSimpleMessage(String to) throws Exception;
}