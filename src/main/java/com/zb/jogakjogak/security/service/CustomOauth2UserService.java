package com.zb.jogakjogak.security.service;


import com.zb.jogakjogak.security.Role;
import com.zb.jogakjogak.security.dto.CustomOAuth2User;
import com.zb.jogakjogak.security.dto.GoogleResponseDto;
import com.zb.jogakjogak.security.dto.KakaoResponseDto;
import com.zb.jogakjogak.security.dto.OAuth2ResponseDto;
import com.zb.jogakjogak.security.entity.Member;
import com.zb.jogakjogak.security.entity.OAuth2Info;
import com.zb.jogakjogak.security.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOauth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2ResponseDto oAuth2ResponseDto = null;
        if(registrationId.equals("kakao")){
            oAuth2ResponseDto = new KakaoResponseDto(oAuth2User.getAttributes());
        }else if(registrationId.equals("google")){
            oAuth2ResponseDto = new GoogleResponseDto(oAuth2User.getAttributes());
        } else{
            return null;
        }
        String username = oAuth2ResponseDto.getProvider() + " " + oAuth2ResponseDto.getProviderId();
        Optional<Member> existMember = memberRepository.findByUsername(username);
        Member member;
        if (existMember.isEmpty()) {
            member = Member.builder()
                    .username(username)
                    .nickname(oAuth2ResponseDto.getNickname())
                    .email(oAuth2ResponseDto.getEmail())
                    .name(oAuth2ResponseDto.getName())
                    .phoneNumber(oAuth2ResponseDto.getPhoneNumber())
                    .lastLoginAt(LocalDateTime.now())
                    .oauth2Info(new ArrayList<>())
                    .jdList(new ArrayList<>())
                    .password("temp_password")
                    .role(Role.USER)
                    .build();
            OAuth2Info oAuth2Info = OAuth2Info.builder()
                    .member(member)
                    .provider(oAuth2ResponseDto.getProvider())
                    .providerId(oAuth2ResponseDto.getProviderId())
                    .build();
            member.getOauth2Info().add(oAuth2Info);
            memberRepository.save(member);
            return new CustomOAuth2User(member);
        } else{
            member = existMember.get();
            member.updateExistingMember(oAuth2ResponseDto);
            memberRepository.save(member);
            return new CustomOAuth2User(member);
        }
    }

}
