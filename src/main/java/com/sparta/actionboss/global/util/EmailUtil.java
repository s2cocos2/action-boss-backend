package com.sparta.actionboss.global.util;

import com.sparta.actionboss.domain.user.entity.User;
import com.sparta.actionboss.domain.post.entity.Post;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;

@Component
public class EmailUtil {

    @Value("${email.secret.id}")
    private String id;
    @Value("${email.secret.pw}")
    private String password;

    private final static String donePostNotificationTitle = "[행동대장] 게시글이 해결완료 되었습니다!";

    public String makeRandomNumber(){
        String randomStr = "";
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            randomStr+=random.nextInt(9);
        }
        return randomStr;
    }

    public void sendEmail(String email, String random) {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost("smtp.naver.com");
        javaMailSender.setUsername(id);
        javaMailSender.setPassword(password);
        javaMailSender.setPort(465);
        javaMailSender.setJavaMailProperties(getMailProperties());

        MimeMessage m = javaMailSender.createMimeMessage();
        MimeMessageHelper h = new MimeMessageHelper(m, "UTF-8");
        try {
            h.setFrom(id);
            h.setTo(email);
            h.setSubject("행동대장 회원가입을 위한 인증메일입니다.");

            String message = "저희 홈페이지를 방문해주셔서 감사합니다."+
                    "<br><br>" +
                    "인증번호는 " + random + " 입니다." +
                    "<br><br>" +
                    "해당 인증번호를 인증번호 확인란에 기입하여 주시기 바랍니다.";
            h.setText(message, true);
        } catch (Exception e) {
            System.out.println(e);
        }
        javaMailSender.send(m);
    }

    public void sendDoneEmail(User user, Post post) {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost("smtp.naver.com");
        javaMailSender.setUsername(id);
        javaMailSender.setPassword(password);
        javaMailSender.setPort(465);
        javaMailSender.setJavaMailProperties(getMailProperties());

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(message, "UTF-8");
        try {
            InputStream inputStream = getClass()
                    .getResourceAsStream("/templates/sendDoneEmailTemplate.html");
            Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
            String template = scanner.hasNext() ? scanner.next() : "";

            String messageContent = template.replace("{username}", user.getNickname())
                    .replace("{postTitle}", post.getTitle());

            messageHelper.setFrom(id);
            messageHelper.setTo(user.getEmail());
            messageHelper.setSubject(donePostNotificationTitle);

            messageHelper.setText(messageContent, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        javaMailSender.send(message);
    }

    public Properties getMailProperties() {
        Properties properties = new Properties();
        properties.setProperty("mail.transport.protocol", "smtp");
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.starttls.enable", "true");
        properties.setProperty("mail.debug", "false");
        properties.setProperty("mail.smtp.ssl.trust", "smtp.naver.com");
        properties.setProperty("mail.smtp.ssl.enable", "true");
        return properties;
    }

}
