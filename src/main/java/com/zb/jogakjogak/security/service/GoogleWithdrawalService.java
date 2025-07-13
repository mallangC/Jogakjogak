package com.zb.jogakjogak.security.service;

import com.zb.jogakjogak.global.exception.AuthException;
import com.zb.jogakjogak.global.exception.MemberErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


@Service
public class GoogleWithdrawalService {

    private static final String GOOGLE_UNLINK_URL = "https://oauth2.googleapis.com/revoke?token=";

    public void unlinkGoogleMember(String accessToken) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GOOGLE_UNLINK_URL + accessToken))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            if (statusCode == 200) {
            } else if (statusCode == 401) {
                throw new AuthException(MemberErrorCode.EXPIRED_ACCESS_TOKEN);
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

