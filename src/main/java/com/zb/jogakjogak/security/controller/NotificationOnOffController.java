package com.zb.jogakjogak.security.controller;


import com.zb.jogakjogak.global.HttpApiResponse;
import com.zb.jogakjogak.security.dto.CustomOAuth2User;
import com.zb.jogakjogak.security.service.NotificationOnOffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "회원의 전체 알림 on/off 기능 API", description = "회원이 모든 채용공고의 이메일 알림을 일괄적으로 변경할 수 있는 API")
@RestController
@RequestMapping("/member/notification/on-off")
@RequiredArgsConstructor
public class NotificationOnOffController {

    private final NotificationOnOffService notificationOnOffService;

    @Operation(summary = "이메일 전체 알림 수정", description = "회원의 모든 채용공고 알림을 일괄적으로 변경할 수 있습니다.")
    @PostMapping
    public ResponseEntity<HttpApiResponse<Boolean>> switchAllJdsNotification(@AuthenticationPrincipal CustomOAuth2User customOAuth2User){

        String username = customOAuth2User.getName();
        boolean notificationOnOff = notificationOnOffService.switchAllJdsNotification(username);
        return ResponseEntity.ok()
                .body(
                        new HttpApiResponse<>(notificationOnOff,
                                "회원의 전체 이메일 알림기능 " + notificationOnOff + "로 수정 완료",
                                HttpStatus.OK)
                );
    }
}
