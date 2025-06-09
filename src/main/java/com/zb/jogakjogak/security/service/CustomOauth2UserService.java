package com.zb.jogakjogak.security.service;


import com.zb.jogakjogak.security.Role;
import com.zb.jogakjogak.security.dto.CustomOAuth2User;
import com.zb.jogakjogak.security.dto.KakaoResponseDto;
import com.zb.jogakjogak.security.entity.Member;
import com.zb.jogakjogak.security.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOauth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    /**
     * Oauth2 리소스서버에서 받을 유저정보
     * @param userRequest
     * @return
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        KakaoResponseDto kakaoResponseDto = new KakaoResponseDto(oAuth2User.getAttributes());

        String userName = kakaoResponseDto.getProvider() + " " + kakaoResponseDto.getProviderId();

        Member existMember = memberRepository.findByUserName(userName);
        Member member;

        if (existMember == null) {
            member = Member.builder()
                    .userName(userName)
                    .name(kakaoResponseDto.getName())
                    .email(kakaoResponseDto.getEmail())
                    .password(null)
                    .provider(registrationId)
                    .role(Role.USER)
                    .build();
            memberRepository.save(member);
            return new CustomOAuth2User(member);
        } else{
            existMember.setUserName(userName);
            existMember.setEmail(kakaoResponseDto.getEmail());
            existMember.setName(kakaoResponseDto.getName());
            memberRepository.save(existMember);
            return new CustomOAuth2User(existMember);
        }
    }
}
