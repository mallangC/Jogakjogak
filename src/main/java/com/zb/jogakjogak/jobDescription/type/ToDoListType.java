package com.zb.jogakjogak.jobDescription.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ToDoListType {

    STRUCTURAL_COMPLEMENT_PLAN("구조적 보완 계획"),
    CONTENT_EMPHASIS_REORGANIZATION_PROPOSAL("내용 강조/재구성 제안(표현 및 피드백 기반)"),
    EMPLOYMENT_SCHEDULE_RELATED("취업 일정 관련");

    private final String koreanDescription;

    public String getKoreanDescription() {
        return koreanDescription;
    }
}
