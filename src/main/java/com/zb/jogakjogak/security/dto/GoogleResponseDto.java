package com.zb.jogakjogak.security.dto;


import lombok.AllArgsConstructor;

import java.util.Map;

@AllArgsConstructor
public class GoogleResponseDto implements OAuth2ResponseDto{

    private final Map<String, Object> attribute;

    @Override
    public String getProvider() {
        return "google";
    }

    @Override
    public String getProviderId() {
        return attribute.get("sub").toString();
    }

    @Override
    public String getEmail() {
        return attribute.get("email").toString();
    }

    @Override
    public String getName() { return attribute.get("name").toString();
    }

    @Override
    public String getPhoneNumber() {
        return "";
    }

    @Override
    public String getNickname(){
        return "";
    }
}
