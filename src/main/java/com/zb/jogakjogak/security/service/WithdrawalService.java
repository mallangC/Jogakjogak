package com.zb.jogakjogak.security.service;


import com.zb.jogakjogak.global.exception.AuthException;
import com.zb.jogakjogak.global.exception.MemberErrorCode;
import com.zb.jogakjogak.security.entity.Member;
import com.zb.jogakjogak.security.entity.OAuth2Info;
import com.zb.jogakjogak.security.repository.MemberRepository;
import com.zb.jogakjogak.security.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WithdrawalService {
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final KakaoWithdrawalService kakaoWithdrawalService;
    private final GoogleWithdrawalService googleWithdrawalService;

    public void withdrawMember(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new AuthException(MemberErrorCode.NOT_FOUND_MEMBER));

        OAuth2Info oAuth2Info = member.getOauth2Info().stream().findFirst()
                .orElseThrow(() -> new AuthException(MemberErrorCode.NOT_FOUND_OAUTH_PROVIDER));
        String provider = oAuth2Info.getProvider();
        String providerId = oAuth2Info.getProviderId();

        if(provider.equalsIgnoreCase("kakao")){
            kakaoWithdrawalService.unlinkKakaoMember(providerId);
        }else{
            String accessToken = oAuth2Info.getAccessToken();
            googleWithdrawalService.unlinkGoogleMember(accessToken);
        }

        refreshTokenRepository.deleteByUsername(username);
        memberRepository.delete(member);
    }
}
