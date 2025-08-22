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
@Transactional
@RequiredArgsConstructor
public class NotificationOnOffService {

    private final MemberRepository memberRepository;
    private final JDRepository jdRepository;

    public boolean switchAllJdsNotification(String username) {

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new AuthException(MemberErrorCode.NOT_FOUND_MEMBER));
        List<JD> jds = jdRepository.findAllByMember(member);

        if(member.isNotificationOnOff()){
            member.setNotificationOnOff(false);
            for(JD jd : jds){
                jd.setAlarmOn(false);
            }
        }else{
            member.setNotificationOnOff(true);
            for(JD jd : jds){
                jd.setAlarmOn(true);
            }
        }

        return member.isNotificationOnOff();
    }
}
