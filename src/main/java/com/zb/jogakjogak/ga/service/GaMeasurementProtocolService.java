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

    @Value("${google.analytics.measurementId:}")
    private String measurementId;

    @Value("${google.analytics.apiSecret:}")
    private String apiSecret;

    private boolean gaEnabled;

    public GaMeasurementProtocolService(WebClient.Builder webClientBuilder) {
        // Measurement Protocol Endpoint
        this.webClient = webClientBuilder.baseUrl("https://www.google-analytics.com/mp/collect").build();
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        this.gaEnabled = measurementId != null && !measurementId.trim().isEmpty() 
                        && apiSecret != null && !apiSecret.trim().isEmpty();
        if (!gaEnabled) {
            System.out.println("Google Analytics is disabled: measurementId or apiSecret is empty");
        } else {
            System.out.println("Google Analytics is enabled with measurementId: " + measurementId);
        }
    }

    /**
     * GA4 Measurement Protocol을 사용하여 이벤트를 전송합니다.
     * 비동기로 처리하여 백엔드 API 성능에 영향을 주지 않도록 합니다.
     */
    public Mono<String> sendGaEvent(String clientId,
                                    String userId,
                                    String eventName,
                                    Map<String, Object> eventParams
    ) {
        // GA가 비활성화된 경우 즉시 성공으로 반환
        if (!gaEnabled) {
            return Mono.just("GA is disabled");
        }
        
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

        // 비동기 HTTP POST 요청 전송
        return webClient.post()
                .uri(uriBuilder -> {
                    uriBuilder.queryParam("measurement_id", measurementId)
                            .queryParam("api_secret", apiSecret);
                    return uriBuilder.build();
                })
                .bodyValue(payload) // 요청 본문에 payload (JSON) 삽입
                .retrieve() // 응답 수신
                .bodyToMono(String.class); // 응답 본문을 String으로 변환
    }

}
