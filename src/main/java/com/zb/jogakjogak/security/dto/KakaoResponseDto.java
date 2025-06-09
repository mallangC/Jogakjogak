package com.zb.jogakjogak.security.dto;

import lombok.AllArgsConstructor;

import java.util.Map;

@AllArgsConstructor
public class KakaoResponseDto {

    private final Map<String, Object> attribute;

    public String getProvider() {
        return "kakao";
    }

    public String getProviderId() {
        return attribute.get("id").toString();
    }

    public String getEmail() {
        return (String) ((Map) attribute.get("kakao_account")).get("email");
    }

    public String getNickName() {
        return (String) ((Map) attribute.get("properties")).get("nickname");
    }

    public String getName() {
        return (String) ((Map<?, ?>) attribute.get("kakao_account")).get("name");
    }

    public String getPhoneNumber() {
        return (String) ((Map<?, ?>) attribute.get("kakao_account")).get("phone_number");
    }
}
