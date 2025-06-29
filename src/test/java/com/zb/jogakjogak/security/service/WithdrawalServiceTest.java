package com.zb.jogakjogak.security.service;

import com.zb.jogakjogak.security.entity.Member;
import com.zb.jogakjogak.security.repository.MemberRepository;
import com.zb.jogakjogak.security.repository.RefreshTokenRepository;
import net.datafaker.Faker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WithdrawalServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private KakaoService kakaoService;

    @InjectMocks
    private WithdrawalService withdrawalService;

    private final Faker faker = new Faker();

    @Test
    @DisplayName("회원탈퇴 성공 - 카카오 연동 해제 및 데이터 삭제")
    void withdrawMember_success_test() {
        // given
        String kakaoId = faker.number().digits(10);
        String userName = "kakao " + kakaoId;

        Member mockMember = createMockMember(userName);

        when(memberRepository.findByUsername(userName)).thenReturn(Optional.of(mockMember));

        // when
        withdrawalService.withdrawMember(userName);

        // then
        verify(memberRepository, times(1)).findByUsername(userName);
        verify(kakaoService, times(1)).unlinkKakaoMember(kakaoId);
        verify(refreshTokenRepository, times(1)).deleteByUsername(userName);
        verify(memberRepository, times(1)).delete(mockMember);
    }

    @Test
    @DisplayName("회원탈퇴 성공 - 카카오 ID 추출 및 정확한 언링크")
    void withdrawMember_kakaoId_extraction_test() {
        // given
        String expectedKakaoId = "1234567890";
        String userName = "kakao " + expectedKakaoId;

        Member mockMember = createMockMember(userName);

        when(memberRepository.findByUsername(userName)).thenReturn(Optional.of(mockMember));

        // when
        withdrawalService.withdrawMember(userName);

        // then
        verify(kakaoService, times(1)).unlinkKakaoMember(expectedKakaoId);
    }

    private Member createMockMember(String userName) {
        Member member = mock(Member.class);
        when(member.getUsername()).thenReturn(userName);
        return member;
    }
}