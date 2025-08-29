package com.zb.jogakjogak.security.controller;


import com.zb.jogakjogak.global.HttpApiResponse;
import com.zb.jogakjogak.security.dto.CustomOAuth2User;
import com.zb.jogakjogak.security.service.WithdrawalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "회원 탈퇴 API", description = "사용자 회원 탈퇴 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/member/withdrawal")
public class WithdrawalController {

    private final WithdrawalService withdrawalService;

    @Operation(summary = "회원 탈퇴", description = "로그인된 사용자가 회원을 탈퇴합니다")
    @DeleteMapping
    public ResponseEntity<HttpApiResponse<?>> oauth2Withdrawal(HttpServletResponse response, @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new HttpApiResponse<>(null,
                            "회원 탈퇴 요청 실패: 인증되지 않은 사용자입니다.",
                            HttpStatus.UNAUTHORIZED));
        }
        withdrawalService.withdrawMember(customOAuth2User.getName());
        clearCookie(response);
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok()
                .body(new HttpApiResponse<>(null,
                        "회원탈퇴 완료",
                        HttpStatus.OK));
    }

    private void clearCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refresh", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        response.addCookie(cookie);
    }
}
