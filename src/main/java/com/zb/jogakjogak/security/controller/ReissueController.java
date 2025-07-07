package com.zb.jogakjogak.security.controller;


import com.zb.jogakjogak.global.HttpApiResponse;
import com.zb.jogakjogak.security.dto.ReissueResultDto;
import com.zb.jogakjogak.security.service.ReissueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Access Token 관리 API", description = "새로운 access token 발급 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class ReissueController {

    private final ReissueService reissueService;

    /**
     * 클라이언트로부터 refresh토큰을 받아 새로운 access토큰 + refresh토큰을 재발급하는 api
     */
    @Operation(summary = "Access Token 재발급 API", description = "새로운 access token을 refresh token으로 재발급 받습니다")
    @PostMapping("/reissue")
    public ResponseEntity reissue(HttpServletRequest request, HttpServletResponse response) {

        String refreshToken = extractRefreshTokenFromCookie(request.getCookies());
        ReissueResultDto reissueResultDto = reissueService.reissue(refreshToken);

        response.setHeader("Authorization", "Bearer " + reissueResultDto.getNewAccessToken());
        response.addCookie(createCookie("refresh", reissueResultDto.getNewRefreshToken()));
        return ResponseEntity.ok()
                .body(
                        new HttpApiResponse<>(null,
                                "refresh token 재발급 완료",
                                HttpStatus.OK));
    }

    @GetMapping("/favicon.ico")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void ignoreFavicon() {}

    private String extractRefreshTokenFromCookie(Cookie[] cookies) {
            if (cookies == null) return null;
            for (Cookie cookie : cookies) {
                if ("refresh".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
            return null;
    }

    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24 * 60 * 60 * 7);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        return cookie;
    }
}
