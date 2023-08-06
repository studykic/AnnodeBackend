package com.ikchi.annode.service.Util.user;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMessage.RecipientType;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

// 메일 전송 서비스
@Service
@RequiredArgsConstructor
public class RegisterMailService {


    private final JavaMailSender emailSender;


    private String ePw;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${app.invitationSuccess}")
    private int invitationSuccess;


    // 메일을 발송한뒤 인증코드를 반환한다
    // sendSimpleMessage 의 매개변수로 들어온 to 는 곧 이메일 주소가 되고,
    // MimeMessage 객체 안에 내가 전송할 메일의 내용을 담는다.
    // 그리고 bean 으로 등록해둔 javaMail 객체를 사용해서 이메일 send
    public Optional<String> sendSimpleMessage(String email) {
        try {

            ePw = createKey(); // 랜덤 인증번호 생성

            MimeMessage message = createMessage(email); // 메일 발송

            emailSender.send(message);
            System.out.println("인증번호 dlswmdqjsgh = " + ePw);
            return Optional.of(ePw); // 메일로 보냈던 인증 코드를 서버로 반환
        } catch (Exception e) {
            throw new IllegalStateException("인증 메일 전송에 실패했습니다");
        }

    }

    public void sendUserEventMailMessage(String nickName, String email, String eventName) {
        try {

            MimeMessage message = createUserEventMailMessage(nickName, email, eventName);

            emailSender.send(message);

        } catch (Exception e) {
            throw new IllegalStateException("인증 메일 전송에 실패했습니다");
        }
    }


    // 초대권 로직 - 실사용은 추후 고려
    public void sendInvitationMailMessage(List<String> invitationCodeList, String email) {
        try {

            MimeMessage message = createInvationMailMessage(invitationCodeList, email);

            emailSender.send(message);

        } catch (Exception e) {
            throw new IllegalStateException("인증 메일 전송에 실패했습니다");
        }
    }

    // 초대권 로직 - 실사용은 추후 고려
    public void sendInviterSucceseMailMessage(String inviterEmail) {
        try {

            MimeMessage message = createSuccessfulEventMailMessage(inviterEmail);

            emailSender.send(message);

        } catch (Exception e) {
            throw new IllegalStateException("인증 메일 전송에 실패했습니다");
        }
    }

    // 메일 내용 작성
    public MimeMessage createMessage(String to)
        throws MessagingException, UnsupportedEncodingException {

        MimeMessage message = emailSender.createMimeMessage();

        message.addRecipients(RecipientType.TO, to);// 보내는 대상
        message.setSubject("Pospace 이메일 인증");// 제목

        String msgg = "";
        msgg += "<html><head><style>";
        msgg += "body { font-family: Arial, sans-serif; margin: 0; padding: 40px; }";
        msgg += "h2 { color: #333; }";
        msgg += "h3 { color: #1E88E5; }";
        msgg += ".container { background-color: #F5F5F5; border: 1px solid #ddd; border-radius: 4px; padding: 20px; }";
        msgg += ".code { background-color: #fff; border: 1px solid #1E88E5; border-radius: 4px; font-size: 130%; padding: 10px; text-align: center; }";
        msgg += "</style></head><body>";
        msgg += "<div class='container'>";
        msgg += "<h2>안녕하세요</h2>";
        msgg += "<h2>Pospace입니다</h2>";
        msgg += "<br>";
        msgg += "<p>아래 코드를 입력하면됩니다<p>";
        msgg += "<div class='code'>";
        msgg += "<strong>";
        msgg += ePw + "</strong></div><br/> "; // 메일에 인증번호 넣기
        msgg += "</div>";
        msgg += "</body></html>";
        message.setText(msgg, "utf-8", "html");// 내용, charset 타입, subtype
        // 보내는 사람의 이메일 주소, 보내는 사람 이름
        message.setFrom(new InternetAddress("kic1219@naver.com", "Pospace"));// 보내는 사람

        return message;
    }


    public MimeMessage createUserEventMailMessage(String nickName, String email, String eventName)
        throws MessagingException, UnsupportedEncodingException {

        MimeMessage message = emailSender.createMimeMessage();

        message = emailSender.createMimeMessage();

        message.addRecipients(RecipientType.TO, adminEmail);
        message.setSubject("유저 활동 알림");

        String msgg = "";
        msgg += "<html><head><style>";
        msgg += "body { font-family: Arial, sans-serif; margin: 0; padding: 40px; }";
        msgg += "h2 { color: #333; }";
        msgg += "h3 { color: #1E88E5; }";
        msgg += ".container { background-color: #F5F5F5; border: 1px solid #ddd; border-radius: 4px; padding: 20px; }";
        msgg += ".code { background-color: #fff; border: 1px solid #1E88E5; border-radius: 4px; font-size: 130%; padding: 10px; text-align: center; }";
        msgg += "</style></head><body>";
        msgg += "<div class='container'>";
        msgg += "<h2>운영진 메세지,</h2>";
        msgg += "<br>";
        msgg += "<p>회원이 " + eventName + " 이벤트를 수행하였습니다.</p>";
        msgg += "<p>신규회원 nickname :" + nickName + "</p>";
        msgg += "<p>신규회원 email :" + email + "</p>";
        msgg += "</div>";
        msgg += "</body></html>";
        message.setText(msgg, "utf-8", "html");

        message.setFrom(new InternetAddress("kic1219@naver.com", "Annode_KIC"));

        return message;

    }


    public MimeMessage createInvationMailMessage(List<String> invitationCodeList, String to)
        throws MessagingException, UnsupportedEncodingException {

        MimeMessage message = emailSender.createMimeMessage();

        message.addRecipients(RecipientType.TO, to);
        message.setSubject("Pospace 신규 초대권");

        String msgg = "";
        msgg += "<html><head><style>";
        msgg += "body { font-family: Arial, sans-serif; margin: 0; padding: 40px; }";
        msgg += "h2 { color: #333; }";
        msgg += "h3 { color: #1E88E5; }";
        msgg += ".container { background-color: #F5F5F5; border: 1px solid #ddd; border-radius: 4px; padding: 20px; }";
        msgg += ".code { background-color: #fff; border: 1px solid #1E88E5; border-radius: 4px; font-size: 130%; padding: 10px; text-align: center; margin: 5px 0; display: inline-block; }";
        msgg += "</style></head><body>";
        msgg += "<div class='container'>";
        msgg += "<h2>안녕하세요</h2>";
        msgg += "<h2>Pospace입니다</h2>";
        msgg += "<br>";
        msgg += "<p>가입이 성공적으로 진행되었습니다. 이제 Pospace의 모든 기능을 사용할 수 있습니다.</p>";
        msgg += "<br>";
        msgg += "<p>더 나아가, 다음은 귀하가 공유할 수 있는 신규 초대권 코드입니다:</p>";
        msgg += "<div style='display: flex; flex-wrap: wrap; justify-content: space-between;'>";

        for (String code : invitationCodeList) {
            msgg += "<div class='code'>" + code + "</div>";
        }

        msgg += "</div>";
        msgg += "<br>";
        msgg += "<h3>감사합니다.</h3>";
        msgg += "</div>";
        msgg += "</body></html>";
        message.setText(msgg, "utf-8", "html");

        message.setFrom(new InternetAddress("kic1219@naver.com", "Annode_KIC"));

        return message;
    }

    public MimeMessage createSuccessfulEventMailMessage(String to)
        throws MessagingException, UnsupportedEncodingException {

        MimeMessage message = emailSender.createMimeMessage();

        message.addRecipients(RecipientType.TO, to);
        message.setSubject("Pospace 초대 이벤트");

        String msgg = "";
        msgg += "<html><head><style>";
        msgg += "body { font-family: Arial, sans-serif; margin: 0; padding: 40px; }";
        msgg += "h2 { color: #333; }";
        msgg += "h3 { color: #1E88E5; }";
        msgg += ".container { background-color: #F5F5F5; border: 1px solid #ddd; border-radius: 4px; padding: 20px; }";
        msgg += ".code { background-color: #fff; border: 1px solid #1E88E5; border-radius: 4px; font-size: 130%; padding: 10px; text-align: center; margin: 5px 0; display: inline-block; }";
        msgg += "</style></head><body>";
        msgg += "<div class='container'>";
        msgg += "<h2>안녕하세요</h2>";
        msgg += "<h2>Pospace입니다</h2>";
        msgg += "<br>";
        msgg += "<p>귀하가 보낸 초대권이 모두 성공적으로 사용되었습니다.</p>";
        msgg += "<p>초대한 보람을 느끼시길 바라고자 작은 보답이 있습니다.</p>";
        msgg += "<br>";
        msgg += "<h3>귀하의 노력에 감사하며, 1만원권을 지급합니다</h3>";
        msgg += "<br>";
        msgg += "<h3>앞으로도 즐겁게 서비스를 이용하시길 기원합니다!</h3>";
        msgg += "</div>";
        msgg += "</body></html>";
        message.setText(msgg, "utf-8", "html");

        message.setFrom(new InternetAddress("kic1219@naver.com", "Annode_KIC"));

        return message;
    }


    // 인증코드 6자리 숫자와 대문자의 조합으로 코드 생성
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
