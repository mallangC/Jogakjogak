package com.zb.jogakjogak.event.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventType {
    NEW_MEMBER("새 이용자 이벤트");
    private final String description;
}
