package com.ikchi.annode.config;

import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig {

    @Value("${mail.sender.username}")
    private String senderUsername;

    @Value("${mail.sender.password}")
    private String senderPassword;

    @Value("${mail.sender.port}")
    private int senderPort;

    @Bean
    public JavaMailSender javaMailService() {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();

        javaMailSender.setHost("smtp.naver.com"); // 메인 도메인 서버 주소 => 정확히는 smtp 서버 주소

        javaMailSender.setUsername(senderUsername);// 메일 아이디
        javaMailSender.setPassword(senderPassword);// 메일 비밀번호
        javaMailSender.setPort(senderPort);// smtp 메일 인증서버 포트
        javaMailSender.setJavaMailProperties(getMailProperties()); // 메일 인증서버 정보 가져오기

        return javaMailSender;
    }

    private Properties getMailProperties() {
        Properties properties = new Properties();
        properties.setProperty("mail.transport.protocol", "smtp"); // 프로토콜 설정
        properties.setProperty("mail.smtp.auth", "true"); // smtp 인증
        properties.setProperty("mail.smtp.starttls.enable", "true"); // smtp strattles 사용
        properties.setProperty("mail.debug", "true"); // 디버그 사용
        properties.setProperty("mail.smtp.ssl.trust",
            "smtp.naver.com"); // ssl 인증 서버는 smtp.naver.com
        properties.setProperty("mail.smtp.ssl.enable", "true"); // ssl 사용
        return properties;
    }
}
