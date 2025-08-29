package com.zb.jogakjogak.security.service;

import com.zb.jogakjogak.security.config.NicknameCreator;
import com.zb.jogakjogak.security.entity.Member;
import com.zb.jogakjogak.security.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NicknameInitializer implements ApplicationRunner {

    private final MemberRepository memberRepository;
    private final NicknameCreator nicknameCreator;

    @Override
    public void run(ApplicationArguments args) {
        List<Member> membersWithoutNickname = memberRepository.findByNicknameIsNull();
        for (Member member : membersWithoutNickname) {
            member.setNickname(nicknameCreator.createNickname());
        }
        memberRepository.saveAll(membersWithoutNickname);
    }
}
