package com.zb.jogakjogak.security.service;


import com.zb.jogakjogak.global.exception.AuthException;
import com.zb.jogakjogak.global.exception.MemberErrorCode;
import com.zb.jogakjogak.security.entity.Member;
import com.zb.jogakjogak.security.repository.MemberRepository;
import com.zb.jogakjogak.security.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WithdrawalService {
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final KakaoService kakaoService;

    public void withdrawMember(String userName) {
        Member member = memberRepository.findByUserName(userName)
                .orElseThrow(() -> new AuthException(MemberErrorCode.NOT_FOUND_MEMBER));
        if(member == null){
            throw new AuthException(MemberErrorCode.NOT_FOUND_MEMBER);
        }
        String kakaoId = member.getUserName().split(" ")[1];
        kakaoService.unlinkKakaoMember(kakaoId);

        refreshTokenRepository.deleteByUsername(userName);
        memberRepository.delete(member);
    }
}
