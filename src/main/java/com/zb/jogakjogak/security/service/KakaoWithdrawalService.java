package com.zb.jogakjogak.security.service;

import com.zb.jogakjogak.global.exception.AuthException;
import com.zb.jogakjogak.global.exception.MemberErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
@RequiredArgsConstructor
public class KakaoWithdrawalService {

    @Value("${kakao.admin-key}")
    private String kakaoAdminKey;

    private static final String KAKAO_UNLINK_URL = "https://kapi.kakao.com/v1/user/unlink";

    public void unlinkKakaoMember(String kakaoId) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(KAKAO_UNLINK_URL))
                    .POST(HttpRequest.BodyPublishers.ofString("target_id_type=user_id&target_id=" + kakaoId))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Authorization", "KakaoAK " + kakaoAdminKey)
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            if (statusCode == 200) {
            } else {
                throw new AuthException(MemberErrorCode.MEMBER_WITHDRAWAL_FAIL);
            }

        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthException(MemberErrorCode.MEMBER_WITHDRAWAL_FAIL);
        }
    }
}
