package com.zb.jogakjogak.security.controller;


import com.zb.jogakjogak.global.HttpApiResponse;
import com.zb.jogakjogak.security.dto.CustomOAuth2User;
import com.zb.jogakjogak.security.service.NotificationOnOffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/member/notification/on-off")
@RequiredArgsConstructor
public class NotificationOnOffController {

    private final NotificationOnOffService notificationOnOffService;
    @PostMapping
    public ResponseEntity<HttpApiResponse<?>> switchAllJdsNotification(@AuthenticationPrincipal CustomOAuth2User customOAuth2User){

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
