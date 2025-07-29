package com.zb.jogakjogak.notification.service;

import com.zb.jogakjogak.ga.service.GaMeasurementProtocolService;
import com.zb.jogakjogak.global.util.HashingUtil;
import com.zb.jogakjogak.jobDescription.entity.JD;
import com.zb.jogakjogak.jobDescription.repository.ToDoListRepository;
import com.zb.jogakjogak.notification.dto.NotificationDto;
import com.zb.jogakjogak.notification.dto.EmailTemplateDto;
import com.zb.jogakjogak.notification.dto.JdEmailDto;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private static final String EMAIL_TITLE = "[조각조각]에서 알림이 도착했습니다.";
    private static final String EMAIL_TEMPLATE_NAME = "notification-email"; // .html 확장자 제외

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;
    private final ToDoListRepository toDoListRepository;
    private final GaMeasurementProtocolService gaService;

    @Async("taskExecutor")
    public void sendNotificationEmail(NotificationDto notificationDto) throws MessagingException {
        String email = notificationDto.getMember().getEmail();
        String userId = notificationDto.getMember().getId().toString();
        String hashedEmail = HashingUtil.sha256(email);

        // 만료된 JD 제거 및 정렬
        notificationDto.getJdList().removeIf(jd -> jd.getEndedAt().isBefore(LocalDateTime.now()));
        notificationDto.getJdList().sort((jd1, jd2) -> jd1.getEndedAt().compareTo(jd2.getEndedAt()));

        String emailType = "notification_jd_reminder";
        String campaignName = "jd_deadline_reminder";

        try {
            MimeMessage message = javaMailSender.createMimeMessage();

            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true, "UTF-8");

            messageHelper.setSubject(EMAIL_TITLE);
            messageHelper.setTo(email);

            String htmlContent = buildEmailHtml(notificationDto);
            messageHelper.setText(htmlContent, true);
            javaMailSender.send(message);

            // GA 이벤트 전송 (성공)
            sendGaSuccessEvent(emailType, campaignName, hashedEmail, userId, notificationDto.getJdList().size());
            log.info("Thymeleaf HTML 메일 전송 성공: {}", email);

        } catch (Exception e) {
            // GA 이벤트 전송 (실패)
            sendGaFailureEvent(emailType, campaignName, hashedEmail, userId, e.getMessage());
            log.warn("메일전송 실패 - 사유: {}", e.getMessage());
            throw new MessagingException("이메일 전송 실패", e);
        }
    }

    /**
     * Thymeleaf 템플릿으로 HTML 이메일 생성
     */
    private String buildEmailHtml(NotificationDto notificationDto) {
        // 1. 템플릿에 전달할 데이터
        EmailTemplateDto templateData = prepareTemplateData(notificationDto);

        // 2. Thymeleaf Context 생성
        Context context = new Context();
        context.setVariable("userName", templateData.getUserName());
        context.setVariable("idleDays", 3);
        context.setVariable("jdList", templateData.getJdList());
        context.setVariable("ctaLink", templateData.getLink());
        context.setVariable("dailyMessage", templateData.getDailyMessage());

        return templateEngine.process(EMAIL_TEMPLATE_NAME, context);
    }

    /**
     * 템플릿에 전달할 데이터를 준비하는 메서드
     */
    private EmailTemplateDto prepareTemplateData(NotificationDto notificationDto) {
        String userName = notificationDto.getMember().getNickname();

        // JD 목록을 이메일용 데이터로 변환
        List<JdEmailDto> jdEmailList = new ArrayList<>();
        for (JD jd : notificationDto.getJdList()) {

            Integer completedCount = toDoListRepository.countByIsDoneTrueAndJd_Id(jd.getId());
            Integer totalCount = toDoListRepository.countByJd_Id(jd.getId());

            // JD 데이터를 이메일용으로 변환
            JdEmailDto jdEmailDto = JdEmailDto.from(jd, completedCount, totalCount);
            jdEmailList.add(jdEmailDto);
        }

        return EmailTemplateDto.builder()
                .userName(userName)
                .jdList(jdEmailList)
                .link("https://jogakjogak.com/dashboard")
                .dailyMessage(getDailyMotivationMessage())
                .build();
    }

    /**
     * 동기부여 메시지
     */
    private String getDailyMotivationMessage() {
        String[] messages = {
                "오늘도 한 조각씩, 꾸준히 나아가는 당신을 응원합니다.",
                "작은 조각들이 모여 큰 성취를 만들어갑니다.",
                "멈춰있던 시간도 괜찮아요. 지금 다시 시작하면 됩니다.",
                "취업 준비는 마라톤이에요. 오늘 할 수 있는 것부터 차근차근 해봐요.",
                "완벽하지 않아도 괜찮습니다. 진전이 있다면 그것으로 충분해요."
        };

        int randomIndex = (int) (Math.random() * messages.length);
        return messages[randomIndex];
    }

    /**
     * GA 성공 이벤트 전송
     */
    private void sendGaSuccessEvent(String emailType, String campaignName, String hashedEmail,
                                    String userId, int jdCount) {
        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put("email_type", emailType);
        eventParams.put("campaign_name", campaignName);
        eventParams.put("send_status", "success");
        eventParams.put("recipient_email", hashedEmail);
        eventParams.put("recipient_user_id", userId);
        eventParams.put("num_jds_notified", jdCount);

        String gaClientId = "backend_notification_" + UUID.randomUUID();
        gaService.sendGaEvent(gaClientId, userId, "email_sent", eventParams).subscribe();
    }

    /**
     * GA 실패 이벤트 전송
     */
    private void sendGaFailureEvent(String emailType, String campaignName, String hashedEmail,
                                    String userId, String errorMessage) {
        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put("email_type", emailType);
        eventParams.put("campaign_name", campaignName);
        eventParams.put("send_status", "failure");
        eventParams.put("recipient_email", hashedEmail);
        eventParams.put("recipient_user_id", userId);
        eventParams.put("error_message_summary",
                errorMessage != null ? errorMessage.substring(0, Math.min(errorMessage.length(), 250)) : "Unknown email error");
        eventParams.put("error_code_custom", "EMAIL_SEND_FAILED");

        String gaClientId = "backend_notification_error_" + UUID.randomUUID();
        gaService.sendGaEvent(gaClientId, userId, "email_send_failed", eventParams).subscribe();
    }
}