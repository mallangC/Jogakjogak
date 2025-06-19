package com.zb.jogakjogak.notification.service;


import com.zb.jogakjogak.jobDescription.entity.JD;
import com.zb.jogakjogak.notification.entity.Notification;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Component
@RequiredArgsConstructor
public class NotificationService {
    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

    private static final String EMAIL_TITLE = "[조각조각]에서 알림이 도착했습니다.";

    @Async("taskExecutor")
    public void sendNotificationEmail(String title, String email, int completedJogak, int notCompletedJogak) throws MessagingException{
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(message);
        messageHelper.setSubject(EMAIL_TITLE);
        messageHelper.setTo(email);
        String text = title + "의 ToDoList가 3일 동안 진행된 조각이 없었어요.\n\n" +
                "완료한 조각: " + completedJogak + "개\n\n" +
                "미완성 조각: " + notCompletedJogak + "개\n\n" +
                "시간이 부족했을 수도 있고,\n" +
                "딱히 손이 안 갔을 수도 있어요.\n" +
                "그럴 땐 '내가 왜 멈췄을까'를\n" +
                "잠깐 들여다보는 것도 방법이에요.\n\n" +
                "조각조각이 막힌 이유에 맞게 도움을 드릴게요.\n" +
                "오늘 할 수 있는 것부터 천천히 시작해봐요.\n\n" +
                "조각조각이 함께 이어드릴게요.";
        messageHelper.setText(text);
        javaMailSender.send(message);
    }
}
