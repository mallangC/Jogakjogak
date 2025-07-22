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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOauth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String accessToken = userRequest.getAccessToken().getTokenValue();

        OAuth2ResponseDto oAuth2ResponseDto;
        if(registrationId.equals("kakao")){
            oAuth2ResponseDto = new KakaoResponseDto(oAuth2User.getAttributes());
        }else if(registrationId.equals("google")){
            oAuth2ResponseDto = new GoogleResponseDto(oAuth2User.getAttributes());
        } else{
            throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId);
        }
        String username = oAuth2ResponseDto.getProvider() + " " + oAuth2ResponseDto.getProviderId();
        Optional<Member> existMember = memberRepository.findByUsernameWithOauth2Info(username);
        Member member;
        if (existMember.isEmpty()) {
            member = Member.builder()
                    .username(username)
                    .nickname(oAuth2ResponseDto.getNickname())
                    .email(oAuth2ResponseDto.getEmail())
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

            if(oAuth2ResponseDto.getProvider().equals("google")){
                oAuth2Info.setAccessToken(accessToken);
            }
            member.getOauth2Info().add(oAuth2Info);
            member = memberRepository.save(member);
            return new CustomOAuth2User(member);
        } else{
            member = existMember.get();
            member.updateExistingMember(oAuth2ResponseDto);

            for (OAuth2Info info : member.getOauth2Info()) {
                if (info.getProvider().equals("google")) {
                    info.setAccessToken(accessToken);
                }
            }
            memberRepository.save(member);
            return new CustomOAuth2User(member);
        }
    }
}
