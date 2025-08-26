package com.zb.jogakjogak.security.service;

import com.zb.jogakjogak.global.exception.AuthException;
import com.zb.jogakjogak.global.exception.MemberErrorCode;
import com.zb.jogakjogak.security.config.NicknameCreator;
import com.zb.jogakjogak.security.dto.MemberResponseDto;
import com.zb.jogakjogak.security.dto.UpdateMemberRequestDto;
import com.zb.jogakjogak.security.entity.Member;
import com.zb.jogakjogak.security.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private NicknameCreator nicknameCreator;

    @InjectMocks
    private MemberService memberService;

    private Member testMember;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testMember = Member.builder()
                .id(1L)
                .username("testUser")
                .email("test@test.com")
                .nickname("oldNickname")
                .isNotificationEnabled(true)
                .build();
    }

    @Test
    @DisplayName("getMember - 정상 조회")
    void getMember_success() {
        // given
        when(memberRepository.findByUsername("testUser")).thenReturn(Optional.of(testMember));

        // when
        MemberResponseDto result = memberService.getMember("testUser");

        // then
        assertThat(result.getNickname()).isEqualTo("oldNickname");
        assertThat(result.getEmail()).isEqualTo("test@test.com");
        assertThat(result.isNotificationEnabled()).isTrue();
    }

    @Test
    @DisplayName("getMember - 회원이 존재하지 않으면 예외 발생")
    void getMember_notFound() {
        // given
        when(memberRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.getMember("unknown"))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining(MemberErrorCode.NOT_FOUND_MEMBER.getMessage());
    }

    @Test
    @DisplayName("updateMember - 닉네임 중복 없을 때 업데이트 성공")
    void updateMember_success() {
        // given
        UpdateMemberRequestDto dto = UpdateMemberRequestDto.builder()
                .nickname("newNickname")
                .isNotificationEnabled(false)
                .build();

        when(memberRepository.findByUsername("testUser")).thenReturn(Optional.of(testMember));
        when(memberRepository.existsByNickname("newNickname")).thenReturn(false);

        // when
        MemberResponseDto result = memberService.updateMember("testUser", dto);

        // then
        assertThat(result.getNickname()).isEqualTo("newNickname");
        assertThat(result.isNotificationEnabled()).isFalse();
    }

    @Test
    @DisplayName("updateMember - 닉네임 중복일 경우 예외 발생")
    void updateMember_duplicateNickname() {
        // given
        UpdateMemberRequestDto dto = UpdateMemberRequestDto.builder()
                .nickname("duplicateNickname")
                .isNotificationEnabled(true)
                .build();

        when(memberRepository.findByUsername("testUser")).thenReturn(Optional.of(testMember));
        when(memberRepository.existsByNickname("duplicateNickname")).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> memberService.updateMember("testUser", dto))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining(MemberErrorCode.ALREADY_EXISTING_NICKNAME.getMessage());
    }
}
