package com.zb.jogakjogak.ga.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class GaMeasurementProtocolService {

    private final WebClient webClient;

    @Value("${google.analytics.measurementId}")
    private String measurementId;

    @Value("${google.analytics.apiSecret}")
    private String apiSecret;

    public GaMeasurementProtocolService(WebClient.Builder webClientBuilder) {
        // Measurement Protocol Endpoint
        this.webClient = webClientBuilder.baseUrl("https://www.google-analytics.com/mp/collect").build();
    }

    /**
     * GA4 Measurement Protocol을 사용하여 이벤트를 전송합니다.
     * 비동기로 처리하여 백엔드 API 성능에 영향을 주지 않도록 합니다.
     *
     * @param clientId    GA에서 사용자를 식별하는 ID. (필수)
     *                    프론트엔드에서 _ga 쿠키 값 (client_id)을 HTTP 헤더 등으로 전달받는 것이 가장 좋음.
     *                    없을 경우 UUID 등으로 임시 ID 생성.
     * @param userId      로그인된 사용자의 고유 ID (선택 사항). GA4의 User-ID 기능에 사용됩니다.
     * @param eventName   전송할 이벤트 이름 (예: "api_call", "backend_error", "email_sent")
     * @param eventParams 이벤트와 함께 보낼 매개변수 (Map<String, Object> 형태)
     * @return Mono<String> 비동기 처리 결과 (성공 시 응답 본문, 실패 시 에러)
     */
    public Mono<String> sendGaEvent(String clientId,
                                    String userId,
                                    String eventName,
                                    Map<String, Object> eventParams
    ) {
        if (clientId == null || clientId.isEmpty()) {
            clientId = UUID.randomUUID().toString();
            System.out.println("Warning: clientId is null or empty. Using generated UUID: " + clientId);
        }

        // GA Measurement Protocol 요청 본문 구성
        Map<String, Object> payload = new HashMap<>();
        payload.put("client_id", clientId);
        if (userId != null && !userId.isEmpty()) {
            payload.put("user_id", userId);
        }
        payload.put("events", Collections.singletonList(
                Map.of(
                        "name", eventName,
                        "params", eventParams
                )
        ));

        System.out.println("Sending GA Event: " + eventName
                + " for client: " + clientId
                + (userId != null ? ", user: " + userId : "")
                + ", params: " + eventParams);

        // 비동기 HTTP POST 요청 전송
        return webClient.post()
                .uri(uriBuilder -> {
                    uriBuilder.queryParam("measurement_id", measurementId)
                            .queryParam("api_secret", apiSecret);
                    return uriBuilder.build();
                })
                .bodyValue(payload) // 요청 본문에 payload (JSON) 삽입
                .retrieve() // 응답 수신
                .bodyToMono(String.class) // 응답 본문을 String으로 변환
                .doOnSuccess(response -> System.out.println("GA Event (" + eventName + ") 전송 성공"))
                .doOnError(error -> System.err.println("GA Event (" + eventName + ") 전송 실패: " + error.getMessage()));
    }

}
