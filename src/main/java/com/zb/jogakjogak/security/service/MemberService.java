package com.zb.jogakjogak.security.service;

import com.zb.jogakjogak.global.exception.AuthException;
import com.zb.jogakjogak.global.exception.MemberErrorCode;
import com.zb.jogakjogak.security.config.NicknameCreator;
import com.zb.jogakjogak.security.dto.MemberResponseDto;
import com.zb.jogakjogak.security.dto.UpdateMemberRequestDto;
import com.zb.jogakjogak.security.entity.Member;
import com.zb.jogakjogak.security.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final NicknameCreator nicknameCreator;


    public MemberResponseDto getMember(String username){
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new AuthException(MemberErrorCode.NOT_FOUND_MEMBER));

        return MemberResponseDto.builder()
                .nickname(member.getNickname())
                .email(member.getEmail())
                .isNotificationEnabled(member.isNotificationEnabled())
                .build();
    }

    @Transactional
    public MemberResponseDto updateMember(String username, UpdateMemberRequestDto updateMemberRequestDto){
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new AuthException(MemberErrorCode.NOT_FOUND_MEMBER));
        if(memberRepository.existsByNickname(updateMemberRequestDto.getNickname())){
           throw new AuthException(MemberErrorCode.ALREADY_EXISTING_NICKNAME);
        }
        member.updateMember(updateMemberRequestDto);
        return MemberResponseDto.builder()
                .nickname(member.getNickname())
                .isNotificationEnabled(member.isNotificationEnabled())
                .build();
    }
}
