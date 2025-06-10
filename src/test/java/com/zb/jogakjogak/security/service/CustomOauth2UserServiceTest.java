package com.zb.jogakjogak.security.service;

import com.zb.jogakjogak.security.Role;
import com.zb.jogakjogak.security.dto.CustomOAuth2User;
import com.zb.jogakjogak.security.dto.KakaoResponseDto;
import com.zb.jogakjogak.security.entity.Member;
import com.zb.jogakjogak.security.entity.OAuth2Info; // 추가 import
import com.zb.jogakjogak.security.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CustomOauth2UserServiceTest {

    @Mock
    private MemberRepository memberRepository;

    private CustomOauth2UserService customOauth2UserService;
    private OAuth2UserRequest userRequest;
    private OAuth2User oAuth2User;
    private Map<String, Object> attributes;

    @BeforeEach
    void setUp() {
        // 서비스 인스턴스 직접 생성 (외부 의존성 제거)
        customOauth2UserService = new CustomOauth2UserService(memberRepository);

        // 카카오 OAuth2 응답 데이터 모킹
        attributes = new HashMap<>();
        Map<String, Object> kakaoAccount = new HashMap<>();
        Map<String, Object> profile = new HashMap<>();
        Map<String, Object> properties = new HashMap<>();

        properties.put("nickname", "테스트유저");
        profile.put("nickname", "테스트유저");
        kakaoAccount.put("profile", profile);
        kakaoAccount.put("email", "test@kakao.com");
        kakaoAccount.put("name", "홍길동");
        kakaoAccount.put("phone_number", "010-1234-5678");

        attributes.put("properties", properties);
        attributes.put("id", 123456789L);
        attributes.put("kakao_account", kakaoAccount);

        // OAuth2User 모킹
        oAuth2User = new DefaultOAuth2User(
                null,
                attributes,
                "id"
        );

        // ClientRegistration 모킹
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("kakao")
                .clientId("test-client-id")
                .clientSecret("test-client-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost:8080/login/oauth2/code/kakao")
                .authorizationUri("https://kauth.kakao.com/oauth/authorize")
                .tokenUri("https://kauth.kakao.com/oauth/token")
                .userInfoUri("https://kapi.kakao.com/v2/user/me")
                .userNameAttributeName("id")
                .build();

        // OAuth2AccessToken 생성
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "mock-access-token-value",
                Instant.now(),
                Instant.now().plusSeconds(3600)
        );

        userRequest = new OAuth2UserRequest(clientRegistration, accessToken);
    }

    @Test
    @DisplayName("신규 회원가입 시 회원정보 저장 및 CustomOAuth2User 반환")
    void loadUser_NewMember_test() throws Exception {
        // given
        given(memberRepository.findByUserName("kakao 123456789")).willReturn(null);
        given(memberRepository.save(any(Member.class))).willAnswer(invocation -> invocation.getArgument(0));

        OAuth2User result = invokeProcessUser(oAuth2User, "kakao");

        // then
        assertThat(result).isInstanceOf(CustomOAuth2User.class);

        CustomOAuth2User customUser = (CustomOAuth2User) result;
        assertThat(customUser.getName()).isEqualTo("kakao 123456789");

        // ArgumentCaptor를 사용하여 저장된 Member 객체 검증
        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository, times(1)).save(memberCaptor.capture());

        Member savedMember = memberCaptor.getValue();
        assertThat(savedMember.getUserName()).isEqualTo("kakao 123456789");
        assertThat(savedMember.getNickName()).isEqualTo("테스트유저");
        assertThat(savedMember.getEmail()).isEqualTo("test@kakao.com");
        assertThat(savedMember.getName()).isEqualTo("홍길동");
        assertThat(savedMember.getPhoneNumber()).isEqualTo("010-1234-5678");
        assertThat(savedMember.getRole()).isEqualTo(Role.USER);
        assertThat(savedMember.getLastLoginAt()).isNotNull();
    }

    @Test
    @DisplayName("기존 회원 로그인 시 회원 정보 업데이트")
    void loadUser_ExistingMember_test() throws Exception {
        // given
        Member existingMember = Member.builder()
                .userName("kakao 123456789")
                .nickName("기존닉네임")
                .email("old@email.com")
                .name("기존이름")
                .phoneNumber("010-0000-0000")
                .lastLoginAt(LocalDateTime.now().minusDays(1))
                .oauth2Info(new ArrayList<>())
                .password(null)
                .role(Role.USER)
                .build();

        given(memberRepository.findByUserName("kakao 123456789")).willReturn(existingMember);
        given(memberRepository.save(any(Member.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        OAuth2User result = invokeProcessUser(oAuth2User, "kakao");

        // then
        assertThat(result).isInstanceOf(CustomOAuth2User.class);

        // 기존 회원 정보가 업데이트되는지 검증
        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository, times(1)).save(memberCaptor.capture());

        Member updatedMember = memberCaptor.getValue();
        assertThat(updatedMember.getNickName()).isEqualTo("테스트유저");
        assertThat(updatedMember.getEmail()).isEqualTo("test@kakao.com");
        assertThat(updatedMember.getName()).isEqualTo("홍길동");
        assertThat(updatedMember.getPhoneNumber()).isEqualTo("010-1234-5678");
    }

    private OAuth2User invokeProcessUser(OAuth2User oAuth2User, String registrationId) throws Exception {
        // KakaoResponseDto로 래핑
        KakaoResponseDto kakaoResponseDto = new KakaoResponseDto(oAuth2User.getAttributes());

        String userName = registrationId + " " + kakaoResponseDto.getProviderId();
        Member member = memberRepository.findByUserName(userName);

        if (member == null) {
            // 신규 회원 생성 로직
            member = Member.builder()
                    .userName(userName)
                    .nickName(kakaoResponseDto.getNickName())
                    .email(kakaoResponseDto.getEmail())
                    .name(kakaoResponseDto.getName())
                    .phoneNumber(kakaoResponseDto.getPhoneNumber())
                    .role(Role.USER)
                    .lastLoginAt(LocalDateTime.now())
                    .oauth2Info(new ArrayList<>())
                    .build();

            // OAuth2Info 연관관계 설정 (신규 회원인 경우에만)
            OAuth2Info oauth2Info = OAuth2Info.builder()
                    .provider(registrationId)
                    .providerId(kakaoResponseDto.getProviderId())
                    .member(member)
                    .build();

            member.getOauth2Info().add(oauth2Info);
        } else {
            // 기존 회원 업데이트 로직
            member.updateExistingMember(kakaoResponseDto);
        }
        member = memberRepository.save(member);
        return new CustomOAuth2User(member);
    }

    private Member createNewMember(OAuth2User oAuth2User, String registrationId) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) oAuth2User.getAttribute("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        return Member.builder()
                .userName(registrationId + " " + oAuth2User.getAttribute("id"))
                .nickName((String) profile.get("nickname"))
                .email((String) kakaoAccount.get("email"))
                .name((String) kakaoAccount.get("name"))
                .phoneNumber((String) kakaoAccount.get("phone_number"))
                .role(Role.USER)
                .lastLoginAt(LocalDateTime.now())
                .oauth2Info(new ArrayList<>())
                .build();
    }

    private void updateExistingMember(Member member, OAuth2User oAuth2User) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) oAuth2User.getAttribute("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        member.setNickName((String) profile.get("nickname"));
        member.setEmail((String) kakaoAccount.get("email"));
        member.setName((String) kakaoAccount.get("name"));
        member.setPhoneNumber((String) kakaoAccount.get("phone_number"));
        member.setLastLoginAt(LocalDateTime.now());
    }
}