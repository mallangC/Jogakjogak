package com.zb.jogakjogak.bizmessage.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class BizMessageService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${aligo.api-url}")
    private String apiUrl;
    @Value("${aligo.api-key}")
    private String apiKey;
    @Value("${aligo.user-id}")
    private String userId;
    @Value("${aligo.sender-key}")
    private String senderKey;
    @Value("${aligo.sender-phone}")
    private String senderPhone;
    @Value("${aligo.template-code}")
    private String templateCode;

    public void sendAlimtalk(String receiverPhone, String receiverName) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("apikey", apiKey);
        params.add("userid", userId);
        params.add("senderkey", senderKey);
        params.add("tpl_code", templateCode);
        params.add("sender", senderPhone);

        // 수신자 정보
        params.add("receiver_1", receiverPhone);
        params.add("recvname_1", receiverName);

        // 알림톡 제목/내용
        params.add("subject_1", "조각조각 알림톡");
        params.add("message_1", "이력서를 최신화 해 주세요!");

        // 실패 시 대체문자 설정
        params.add("failover", "N");

        // 테스트 모드 설정
        params.add("testMode", "Y");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);
            System.out.println("알림톡 응답: " + response.getBody());
        } catch (Exception e) {
            System.err.println("알림톡 전송 오류: " + e.getMessage());
        }
    }
}
