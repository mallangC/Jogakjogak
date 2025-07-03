package com.zb.jogakjogak.notification.service;


import com.zb.jogakjogak.jobDescription.entity.JD;
import com.zb.jogakjogak.jobDescription.repository.ToDoListRepository;
import com.zb.jogakjogak.notification.dto.NotificationDto;
import com.zb.jogakjogak.notification.entity.Notification;
import com.zb.jogakjogak.security.entity.Member;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private static final String EMAIL_TITLE = "[조각조각]에서 알림이 도착했습니다.";

    private final JavaMailSender javaMailSender;

    private final ToDoListRepository toDoListRepository;

    @Async("taskExecutor")
    public void sendNotificationEmail(NotificationDto notificationDto) throws MessagingException{
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(message);
            messageHelper.setSubject(EMAIL_TITLE);
            messageHelper.setTo(notificationDto.getMember().getEmail());
            String text = " ";

            for (JD jd : notificationDto.getJdList()) {
                String title = jd.getTitle();
                Integer completedTodoListCount = toDoListRepository.countByIsDoneTrueAndJd_Id(jd.getId());
                Integer notCompletedTodoListCount = toDoListRepository.countByIsDoneFalseAndJd_Id(jd.getId());
                text +=  "========================================================\n\n" +
                        title + "의 ToDoList가 3일 동안 진행된 조각이 없었어요.\n\n" +
                        "완료한 조각: " + completedTodoListCount + "개\n\n" +
                        "미완성 조각: " + notCompletedTodoListCount + "개\n\n" +
                        "시간이 부족했을 수도 있고,\n" +
                        "딱히 손이 안 갔을 수도 있어요.\n" +
                        "그럴 땐 '내가 왜 멈췄을까'를\n" +
                        "잠깐 들여다보는 것도 방법이에요.\n\n" +
                        "조각조각이 막힌 이유에 맞게 도움을 드릴게요.\n" +
                        "오늘 할 수 있는 것부터 천천히 시작해봐요.\n\n" +
                        "조각조각이 함께 이어드릴게요.\n\n" +
                        "========================================================\n\n";
            }
            messageHelper.setText(text, false);
            javaMailSender.send(message);
        }catch(MessagingException e){
            log.warn("메일전송 실패 - JD ID: {}, 사유: {}", 1, e.getMessage());
            }
    }
}
