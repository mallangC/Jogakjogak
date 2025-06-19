package com.zb.jogakjogak.security.dto;

import lombok.AllArgsConstructor;

import java.util.Map;

@AllArgsConstructor
public class KakaoResponseDto implements OAuth2ResponseDto {

    private final Map<String, Object> attribute;

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getProviderId() {
        return attribute.get("id").toString();
    }

    @Override
    public String getEmail() {
        return (String) ((Map) attribute.get("kakao_account")).get("email");
    }

    @Override
    public String getNickname() {
        return (String) ((Map) attribute.get("properties")).get("nickname");
    }

    @Override
    public String getName() {
        return (String) ((Map<?, ?>) attribute.get("kakao_account")).get("name");
    }

    @Override
    public String getPhoneNumber() {
        return (String) ((Map<?, ?>) attribute.get("kakao_account")).get("phone_number");
    }
}
