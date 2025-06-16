package com.zb.jogakjogak.security.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.zb.jogakjogak.global.exception.AuthException;
import com.zb.jogakjogak.global.exception.MemberErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class KakaoService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kakao.admin-key}")
    private String kakaoAdminKey;
    private static final String KAKAO_UNLINK_URL = "https://kapi.kakao.com/v1/user/unlink";

    public void unlinkKakaoMember(String kakaoId) {
        HttpEntity<MultiValueMap<String, String>> request = unlinkRequest(kakaoId);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(KAKAO_UNLINK_URL, request, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new AuthException(MemberErrorCode.MEMBER_WITHDRAWAL_FAIL);
            }
        } catch (Exception e) {
            throw new AuthException(MemberErrorCode.MEMBER_WITHDRAWAL_FAIL);
        }
    }

    private HttpEntity<MultiValueMap<String, String>> unlinkRequest(String kakaoId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "KakaoAK " + kakaoAdminKey);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("target_id_type", "user_id");
        body.add("target_id", kakaoId);
        return new HttpEntity<>(body, headers);
    }
}
