package com.zb.jogakjogak.security.service;


import com.zb.jogakjogak.global.exception.AuthException;
import com.zb.jogakjogak.global.exception.MemberErrorCode;
import com.zb.jogakjogak.jobDescription.entity.JD;
import com.zb.jogakjogak.jobDescription.repository.JDRepository;
import com.zb.jogakjogak.security.entity.Member;
import com.zb.jogakjogak.security.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationOnOffService {

    private final MemberRepository memberRepository;
    private final JDRepository jdRepository;

    @Transactional
    public boolean switchAllJdsNotification(String username) {

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new AuthException(MemberErrorCode.NOT_FOUND_MEMBER));
        List<JD> jds = jdRepository.findAllByMember(member);

        boolean isNotificationOn = !member.isNotificationEnabled();
        member.setNotificationEnabled(isNotificationOn);
        if(!member.isNotificationEnabled()){
            for(JD jd : jds){
                jd.setAlarmOn(isNotificationOn);
            }
        }

        return member.isNotificationEnabled();
    }
}
