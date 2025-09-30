package com.zb.jogakjogak.event.domain.responseDto;

import com.zb.jogakjogak.event.entity.Event;
import com.zb.jogakjogak.event.type.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventResponseDto {
    private Long id;
    private String code;
    private EventType type;
    private Boolean isFirst;

    public static EventResponseDto of(Event event) {
        return EventResponseDto.builder()
                .id(event.getId())
                .code(event.getCode())
                .type(event.getType())
                .isFirst(event.getIsFirst())
                .build();
    }
}
