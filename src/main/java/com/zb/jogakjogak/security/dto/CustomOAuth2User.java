package com.zb.jogakjogak.security.dto;

import com.zb.jogakjogak.security.entity.Member;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;


@AllArgsConstructor
public class CustomOAuth2User implements OAuth2User {

    private final Member member;
    private String provider;

    @Override
    public Map<String, Object> getAttributes() {
        return Map.of();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return member.getRole().name();
            }
        });
        return collection;
    }

    @Override
    public String getName() {
        return member.getUsername();
    }

    public String getRealName(){
        return member.getName();
    }

    public String getEmail() {
        return member.getEmail();
    }

    public String getPhoneNumber(){
        return member.getPhoneNumber();
    }

    public String getRole(){
        return member.getRole().name();
    }

    public String getNickName(){
        return member.getNickname();
    }

    public Member getMember() {
        return member;
    }

    public String getProvider() {
        return provider;
    }
}
