package com.zb.jogakjogak.event.service;

import com.zb.jogakjogak.event.domain.responseDto.EventResponseDto;
import com.zb.jogakjogak.event.entity.Event;
import com.zb.jogakjogak.event.repository.EventRepository;
import com.zb.jogakjogak.event.type.EventType;
import com.zb.jogakjogak.global.exception.EventException;
import com.zb.jogakjogak.security.entity.Member;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.zb.jogakjogak.global.exception.EventErrorCode.NOT_FOUND_EVENT_CODE;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    /**
     * 회원이 처음 등록할 때 생성된 이벤트를 조회하는 서비스 메서드
     * 두번째 조회하는 경우 isFirst를 false로 반환 (팝업을 띄울 대상인지 아닌지 확인하기 위해)
     *
     * @param member 회원 객체
     * @return 이벤트 Dto
     * @throws EventException 이벤트를 찾을 수 없는 경우 발생하는 예외
     */
    @Transactional
    public EventResponseDto getNewMemberEvent(Member member) {
        Event event = eventRepository.findByMemberIdAndType(member.getId(), EventType.NEW_MEMBER)
                .orElseThrow(() -> new EventException(NOT_FOUND_EVENT_CODE));

        if (event.getIsFirst()) {
            event.notFirst();
            return EventResponseDto.builder()
                    .id(event.getId())
                    .code(event.getCode())
                    .type(event.getType())
                    .isFirst(!event.getIsFirst())
                    .build();
        }

        return EventResponseDto.of(event);
    }
}
