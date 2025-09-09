package com.zb.jogakjogak.security.config;


import com.zb.jogakjogak.security.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;


@Component
@RequiredArgsConstructor
public class NicknameCreator {

    private final MemberRepository memberRepository;

    private static final Random RANDOM = new Random();
    private static final List<String> ADJECTIVE = List.of(
            "즐거운", "행복한", "용감한", "빠른", "귀여운", "취업한", "착한", "긍정적인",
            "기쁜", "희망적인"
    );

    private static final List<String> ANIMAL = List.of(
            "호랑이", "사자", "거북이", "토끼", "고양이", "강아지", "늑대", "여우",
            "참새", "양"
    );

    public String createNickname(){
        String nickname;

        do {
            String adjective = ADJECTIVE.get(RANDOM.nextInt(ADJECTIVE.size()));
            String animal = ANIMAL.get(RANDOM.nextInt(ANIMAL.size()));
            int number = 1000 + RANDOM.nextInt(9000);

            nickname = adjective + " " + animal + number;

        } while (memberRepository.existsByNickname(nickname));

        return nickname;
    }
}
