package com.zb.jogakjogak.event.service;

import com.zb.jogakjogak.event.domain.responseDto.EventResponseDto;
import com.zb.jogakjogak.event.entity.Event;
import com.zb.jogakjogak.event.repository.EventRepository;
import com.zb.jogakjogak.event.type.EventType;
import com.zb.jogakjogak.global.exception.EventErrorCode;
import com.zb.jogakjogak.global.exception.EventException;
import com.zb.jogakjogak.security.entity.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    private final Member mockMember = Member.builder()
            .id(1L)
            .username("testUser")
            .email("test@mail.com")
            .password("temp password")
            .build();

    private final Event mockEvent = Event.builder()
            .id(1L)
            .code("TEST01")
            .member(mockMember)
            .type(EventType.NEW_MEMBER)
            .isFirst(true)
            .build();

    @Test
    @DisplayName("새 사용자 이벤트 조회 성공 - isFirst가 true인 경우")
    void getNewMemberEvent_success1() {
        //given
        given(eventRepository.findByMemberIdAndType(anyLong(), any()))
                .willReturn(Optional.of(mockEvent));
        //when
        EventResponseDto result = eventService.getNewMemberEvent(mockMember);
        //then
        assertEquals(result.getCode(), mockEvent.getCode());
        assertEquals(result.getType(), mockEvent.getType());
        assertEquals(true, result.getIsFirst());
    }

    @Test
    @DisplayName("새 사용자 이벤트 조회 성공 - isFirst가 false인 경우")
    void getNewMemberEvent_success2() {
        //given
        given(eventRepository.findByMemberIdAndType(anyLong(), any()))
                .willReturn(Optional.of(Event.builder()
                        .id(1L)
                        .code("TEST01")
                        .member(mockMember)
                        .type(EventType.NEW_MEMBER)
                        .isFirst(false)
                        .build()));
        //when
        EventResponseDto result = eventService.getNewMemberEvent(mockMember);
        //then
        assertEquals(result.getCode(), mockEvent.getCode());
        assertEquals(result.getType(), mockEvent.getType());
        assertEquals(false, result.getIsFirst());
    }

    @Test
    @DisplayName("새 사용자 이벤트 조회 실패 - 이벤트가 존재하지 않음(이전 사용자)")
    void getNewMemberEvent_failure() {
        //given
        given(eventRepository.findByMemberIdAndType(anyLong(), any(EventType.class)))
                .willReturn(Optional.empty());
        //when
        EventException exception = assertThrows(EventException.class, () -> eventService.getNewMemberEvent(mockMember));

        //then
        assertEquals(EventErrorCode.NOT_FOUND_EVENT_CODE, exception.getErrorCode());
    }

}