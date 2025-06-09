package com.zb.jogakjogak.jobDescription.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ToDoListType {

    STRUCTURAL_COMPLEMENT_PLAN("구조적 보완 계획"),
    CONTENT_EMPHASIS_REORGANIZATION_PROPOSAL("내용 강조/ 재구성 재안(표현 및 피드백 기반"),
    EMPLOYMENT_SCHEDULE_RELATED("취업 일정 관련");

    private final String koreanDescription;

    // Enum 값을 문자열로 변환하거나, 문자열을 Enum 값으로 변환하는 유틸리티 메서드
    public static ToDoListType fromKoreanDescription(String koreanDescription) {
        for (ToDoListType type : ToDoListType.values()) {
            if (type.koreanDescription.equals(koreanDescription)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown ToDoItemType: " + koreanDescription);
    }
}
