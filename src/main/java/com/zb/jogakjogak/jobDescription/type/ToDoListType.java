package com.zb.jogakjogak.jobDescription.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ToDoListType {

    STRUCTURAL_COMPLEMENT_PLAN("구조적 보완 계획"),
    CONTENT_EMPHASIS_REORGANIZATION_PROPOSAL("내용 강조/재구성 제안(표현 및 피드백 기반)"),
    EMPLOYMENT_SCHEDULE_RELATED("취업 일정 관련");

    private final String koreanDescription;

    @JsonCreator
    public static ToDoListType fromKoreanDescription(String koreanDescription) {
        for (ToDoListType type : ToDoListType.values()) {
            if (type.koreanDescription.equals(koreanDescription)) {
                return type;
            }
        }
        // 유효하지 않은 타입 문자열이 들어왔을 때 예외 처리
        throw new IllegalArgumentException("Unknown ToDoListType: " + koreanDescription);
    }

    @JsonValue
    public String getKoreanDescription() {
        return koreanDescription;
    }
}
