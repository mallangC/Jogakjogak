package com.zb.jogakjogak.notification.service;

import com.zb.jogakjogak.ga.service.GaMeasurementProtocolService;
import com.zb.jogakjogak.global.util.HashingUtil;
import com.zb.jogakjogak.jobDescription.entity.JD;
import com.zb.jogakjogak.jobDescription.repository.ToDoListRepository;
import com.zb.jogakjogak.notification.dto.EmailTemplateDto;
import com.zb.jogakjogak.notification.dto.JdEmailDto;
import com.zb.jogakjogak.notification.dto.NotificationDto;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDateTime;
import java.util.*;

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
        context.setVariable("nickname", templateData.getNickname());
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
        String nickname = notificationDto.getMember().getNickname();
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
                .nickname(nickname)
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
                "시간이 부족했을 수도 있고, 딱히 손이 안 갔을 수도 있어요. 그럴 땐 ‘내가 왜 멈췄을까’를 잠깐 들여다보는 것도 방법이에요. 조각조각이 막힌 이유에 맞게 도움을 드릴게요. 오늘 할 수 있는 것부터 천천히 시작해 봐요.\uD83E\uDDE9",
                "잠시 쉬고 있었다면 지금이 다시 시작하기 좋은 순간이에요. 할 일을 전부 한 번에 끝낼 필요는 없어요. 조각조각에서 오늘 할 수 있는 가장 쉬운 할 일부터 시작해 봐요.\uD83D\uDD25",
                "막막했던 건 다음에 무엇을 해야 할지 보이지 않아서인지도 몰라요. 목표가 선명해지면 발걸음은 훨씬 가벼워집니다. 오늘은 조각조각에서 할 일 하나를 선택해 바로 시작해 보세요.\uD83C\uDFAF",
                "하루가 지날수록 준비는 미뤄지고, 마음은 무거워질 수 있어요. 그 무게를 줄이는 가장 간단한 방법은 시작하는 거예요. 지금 조각조각에서 할 일 하나를 완료해 보세요.\uD83D\uDE4C",
                "멈춰 있는 사이, 기회는 계속 흘러가고 있어요. 오늘 한 번에 다 하지 않아도 돼요. 조각조각에서 남은 할 일 중 하나만 끝내 보세요.\uD83D\uDCAA",
                "멈춘 이유가 무엇이든 괜찮아요. 지금 조각조각에서 이력서 한 줄이라도 업데이트해 보세요. 그 한 줄이 다음 지원서 제출을 훨씬 빨리 만들어 줄 거예요.\uD83D\uDE80"
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